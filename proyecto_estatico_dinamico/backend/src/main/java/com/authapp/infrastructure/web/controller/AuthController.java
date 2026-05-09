package com.authapp.infrastructure.web.controller;

import com.authapp.application.auth.AuthService;
import com.authapp.infrastructure.security.AppUserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterReq req) {
        authService.register(req.email, req.password);
        return ResponseEntity.ok(Map.of("message", "Usuario registrado exitosamente"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginReq req) {
        AuthService.LoginResult result = authService.login(req.email, req.password);
        return ResponseEntity.ok(Map.of(
            "preAuthToken", result.preAuthToken(),
            "mfaRequired", result.mfaRequired(),
            "message", result.message()
        ));
    }

    @PostMapping("/mfa/validate")
    public ResponseEntity<?> validateMfa(@Valid @RequestBody MfaValidateReq req) {
        AuthService.AuthResult result = authService.validateMfa(req.preAuthToken, req.totpCode);
        return ResponseEntity.ok(Map.of(
            "token", result.token(),
            "email", result.email(),
            "roles", result.roles(),
            "permissions", result.permissions()
        ));
    }

    @PostMapping("/mfa/setup")
    public ResponseEntity<?> setupMfa(@RequestHeader("Authorization") String authHeader) {
        String preAuthToken = authHeader.replace("Bearer ", "");
        AuthService.MfaSetupResult result = authService.setupMfa(preAuthToken);
        return ResponseEntity.ok(Map.of(
            "qrImageUri", result.qrImageUri(),
            "secret", result.secret(),
            "message", "Escanee el QR con Google Authenticator y confirme con un código"
        ));
    }

    @PostMapping("/mfa/setup/confirm")
    public ResponseEntity<?> confirmMfaSetup(@RequestHeader("Authorization") String authHeader,
                                              @RequestBody MfaConfirmReq req) {
        String preAuthToken = authHeader.replace("Bearer ", "");
        AuthService.AuthResult result = authService.confirmMfaSetup(preAuthToken, req.totpCode);
        return ResponseEntity.ok(Map.of(
            "token", result.token(),
            "email", result.email(),
            "roles", result.roles(),
            "permissions", result.permissions(),
            "message", "MFA configurado exitosamente"
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(Map.of(
            "id", principal.getId(),
            "email", principal.getEmail(),
            "roles", principal.getRoles(),
            "permissions", principal.getPermissions()
        ));
    }

    // Request bodies
    @Data static class RegisterReq {
        @Email @NotBlank public String email;
        @NotBlank public String password;
    }
    @Data static class LoginReq {
        @Email @NotBlank public String email;
        @NotBlank public String password;
    }
    @Data static class MfaValidateReq {
        @NotBlank public String preAuthToken;
        @NotBlank public String totpCode;
    }
    @Data static class MfaConfirmReq {
        @NotBlank public String totpCode;
    }
}
