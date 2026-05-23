package com.aerorescue.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

@Slf4j
@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    @Value("${jwt.public-key-path}")
    private Resource publicKeyResource;

    private PublicKey publicKey;

    // RBAC: paths que solo ADMIN puede usar
    private static final List<String> ADMIN_ONLY = List.of("/audit");
    private static final List<String> VIEWER_FORBIDDEN_METHODS = List.of("POST", "PUT", "PATCH", "DELETE");

    public JwtAuthFilter() {
        super(Config.class);
    }

    @PostConstruct
    public void init() throws Exception {
        String keyContent = new String(publicKeyResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(keyContent);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        this.publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);
        log.info("API Gateway: JWT RS256 public key loaded");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return unauthorized(exchange, "Missing or invalid Authorization header");
            }

            try {
                String token = authHeader.substring(7);
                Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

                String role   = claims.get("role", String.class);
                String userId = claims.getSubject();
                String region = claims.get("region", String.class);
                String orgId  = claims.get("organization_id", String.class);
                int clearance = claims.get("clearance_level", Integer.class);
                String path   = exchange.getRequest().getPath().value();
                String method = exchange.getRequest().getMethod().name();

                // ── RBAC validation ──────────────────────────
                if (!isRbacAllowed(role, path, method)) {
                    return forbidden(exchange, "RBAC: role " + role + " not allowed on " + method + " " + path);
                }

                // ── Propagate security headers to downstream ──
                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id",         userId)
                    .header("X-User-Role",        role)
                    .header("X-User-Region",      region != null ? region : "")
                    .header("X-Clearance-Level",  String.valueOf(clearance))
                    .header("X-Organization-Id",  orgId != null ? orgId : "")
                    .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (Exception e) {
                log.warn("JWT validation failed: {}", e.getMessage());
                return unauthorized(exchange, "Invalid or expired token");
            }
        };
    }

    private boolean isRbacAllowed(String role, String path, String method) {
        // VIEWER no puede escribir
        if ("VIEWER".equals(role) && VIEWER_FORBIDDEN_METHODS.contains(method)) return false;
        // Solo ADMIN y SUPERVISOR pueden ver auditoría
        if (path.startsWith("/audit") && !"ADMIN".equals(role) && !"SUPERVISOR".equals(role)) return false;
        // Solo ADMIN y DRONE_TECH pueden registrar drones
        if (path.startsWith("/drones") && "POST".equals(method)
                && !"ADMIN".equals(role) && !"DRONE_TECH".equals(role)) return false;
        return true;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        log.warn("Unauthorized: {}", message);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        log.warn("Forbidden: {}", message);
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }

    public static class Config {}
}
