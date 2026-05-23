package com.aerorescue.mission.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final PublicKey publicKey;

    // Load key in constructor — NOT @PostConstruct
    // @PostConstruct fails when used with Servlet Filters in Tomcat
    public JwtAuthFilter(@Value("${jwt.public-key-path}") Resource publicKeyResource) throws Exception {
        String keyContent = new String(
            publicKeyResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s", "")
            .replaceAll("\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(keyContent);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        this.publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);
        log.info("JwtAuthFilter: RSA public key loaded for {}", "mission");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7);
            Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            String role   = claims.get("role", String.class);
            String userId = claims.getSubject();
            String region = claims.get("region", String.class);
            String orgId  = claims.get("organization_id", String.class);
            Object cl     = claims.get("clearance_level");
            int clearance = cl != null ? ((Number) cl).intValue() : 0;

            request.setAttribute("userId",         userId);
            request.setAttribute("role",           role);
            request.setAttribute("region",         region != null ? region : "");
            request.setAttribute("clearanceLevel", clearance);
            request.setAttribute("organizationId", orgId != null ? orgId : "");
            request.setAttribute("allowedMissionTypes", claims.get("allowed_mission_types"));

            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                    userId, null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }
}
