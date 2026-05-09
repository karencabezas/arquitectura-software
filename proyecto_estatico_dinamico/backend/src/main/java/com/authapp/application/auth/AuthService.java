package com.authapp.application.auth;

import com.authapp.domain.model.Usuario;
import com.authapp.domain.port.UsuarioRepositoryPort;
import com.authapp.infrastructure.security.JwtService;
import com.authapp.infrastructure.security.TotpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Caso de uso: Autenticación.
 * Depende de UsuarioRepositoryPort (domain.port), NO de JPA directamente.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UsuarioRepositoryPort usuarioPort;
    private final JwtService jwtService;
    private final TotpService totpService;
    private final PasswordEncoder passwordEncoder;

    public LoginResult login(String email, String password) {
        Usuario usuario = usuarioPort.findByEmail(email)
                .orElseThrow(() -> new AuthException("Credenciales inválidas"));

        if (!usuario.estaActivo()) throw new AuthException("Usuario inactivo");

        if (!passwordEncoder.matches(password, usuario.getPasswordHash()))
            throw new AuthException("Credenciales inválidas");

        String preAuthToken = jwtService.generatePreAuthToken(email);

        return usuario.tieneMfaConfigurado()
                ? LoginResult.mfaRequired(preAuthToken)
                : LoginResult.mfaNotConfigured(preAuthToken);
    }

    public AuthResult validateMfa(String preAuthToken, String totpCode) {
        validatePreAuthToken(preAuthToken);
        String email = jwtService.extractEmail(preAuthToken);
        Usuario usuario = usuarioPort.findByEmail(email)
                .orElseThrow(() -> new AuthException("Usuario no encontrado"));

        if (!totpService.verifyCode(usuario.getTotpSecret(), totpCode))
            throw new AuthException("Código TOTP inválido");

        return buildAuthResult(usuario);
    }

    public MfaSetupResult setupMfa(String preAuthToken) {
        validatePreAuthToken(preAuthToken);
        String email = jwtService.extractEmail(preAuthToken);
        Usuario usuario = usuarioPort.findByEmail(email)
                .orElseThrow(() -> new AuthException("Usuario no encontrado"));

        String secret = totpService.generateSecret();
        usuario.setTotpSecret(secret);
        usuario.setMfaEnabled(false);
        usuarioPort.save(usuario);

        return new MfaSetupResult(totpService.generateQrImageUri(secret, email), secret);
    }

    public AuthResult confirmMfaSetup(String preAuthToken, String totpCode) {
        validatePreAuthToken(preAuthToken);
        String email = jwtService.extractEmail(preAuthToken);
        Usuario usuario = usuarioPort.findByEmail(email)
                .orElseThrow(() -> new AuthException("Usuario no encontrado"));

        if (!totpService.verifyCode(usuario.getTotpSecret(), totpCode))
            throw new AuthException("Código inválido. Verifique la hora de su dispositivo.");

        usuario.setMfaEnabled(true);
        usuarioPort.save(usuario);
        return buildAuthResult(usuario);
    }

    public void register(String email, String password) {
        if (usuarioPort.existsByEmail(email))
            throw new AuthException("El email ya está registrado");

        Usuario nuevo = new Usuario();
        nuevo.setEmail(email);
        nuevo.setPasswordHash(passwordEncoder.encode(password));
        nuevo.setMfaEnabled(false);
        nuevo.setActivo(true);
        usuarioPort.save(nuevo);
    }

    private void validatePreAuthToken(String token) {
        if (!jwtService.isTokenValid(token) || !jwtService.isPreAuthToken(token))
            throw new AuthException("Token de pre-autenticación inválido o expirado");
    }

    private AuthResult buildAuthResult(Usuario usuario) {
        Set<String> roles    = usuario.obtenerNombresRoles();
        Set<String> permisos = usuario.obtenerNombresPermisos();
        String token = jwtService.generateFullToken(usuario.getEmail(), usuario.getId(), roles, permisos);
        return new AuthResult(token, usuario.getEmail(), roles, permisos);
    }

    public record LoginResult(String preAuthToken, boolean mfaRequired, String message) {
        static LoginResult mfaRequired(String t)      { return new LoginResult(t, true,  "Ingrese el código de su app autenticadora"); }
        static LoginResult mfaNotConfigured(String t) { return new LoginResult(t, false, "Configure su autenticador MFA"); }
    }
    public record AuthResult(String token, String email, Set<String> roles, Set<String> permissions) {}
    public record MfaSetupResult(String qrImageUri, String secret) {}
    public static class AuthException extends RuntimeException {
        public AuthException(String message) { super(message); }
    }
}
