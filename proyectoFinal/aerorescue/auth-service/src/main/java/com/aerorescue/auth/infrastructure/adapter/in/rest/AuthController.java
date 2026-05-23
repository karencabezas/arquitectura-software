package com.aerorescue.auth.infrastructure.adapter.in.rest;

import com.aerorescue.auth.domain.model.TokenPair;
import com.aerorescue.auth.domain.port.in.*;
import com.aerorescue.auth.infrastructure.adapter.in.rest.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, MFA y gestión de tokens")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final VerifyMfaUseCase verifyMfaUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final EnrollMfaUseCase enrollMfaUseCase;

    @PostMapping("/login")
    @Operation(summary = "Login con usuario y contraseña")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginUseCase.LoginResult result = loginUseCase.login(request.username(), request.password());

        if (result.mfaRequired()) {
            return ResponseEntity.ok(LoginResponse.mfaRequired(result.tempToken()));
        }

        TokenPair pair = result.tokenPair();
        return ResponseEntity.ok(LoginResponse.success(
            pair.getAccessToken(), pair.getRefreshToken(), pair.getAccessTokenExpiresIn()
        ));
    }

    @PostMapping("/verify-mfa")
    @Operation(summary = "Verificar código TOTP de Google Authenticator")
    public ResponseEntity<TokenResponse> verifyMfa(@Valid @RequestBody VerifyMfaRequest request) {
        TokenPair pair = verifyMfaUseCase.verifyMfa(request.tempToken(), request.totpCode());
        return ResponseEntity.ok(new TokenResponse(
            pair.getAccessToken(), pair.getRefreshToken(), pair.getAccessTokenExpiresIn()
        ));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar access token usando refresh token")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        TokenPair pair = refreshTokenUseCase.refresh(request.refreshToken());
        return ResponseEntity.ok(new TokenResponse(
            pair.getAccessToken(), pair.getRefreshToken(), pair.getAccessTokenExpiresIn()
        ));
    }

    @PostMapping("/enroll-mfa")
    @Operation(summary = "Registrar Google Authenticator — obtener QR")
    public ResponseEntity<EnrollMfaResponse> enrollMfa(@AuthenticationPrincipal UserDetails userDetails) {
        EnrollMfaUseCase.EnrollResult result = enrollMfaUseCase.enroll(userDetails.getUsername());
        return ResponseEntity.ok(new EnrollMfaResponse(result.secret(), result.qrCodeUrl()));
    }
}
