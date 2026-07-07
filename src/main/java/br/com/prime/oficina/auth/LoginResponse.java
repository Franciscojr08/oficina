package br.com.prime.oficina.auth;

public record LoginResponse(
        String token,
        String tipo
) {
}