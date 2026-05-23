package com.aerorescue.auth.infrastructure.adapter.in.rest.dto;

public record EnrollMfaResponse(String secret, String qrCodeUrl) {}
