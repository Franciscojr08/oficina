package br.com.prime.oficina.cliente.application;

import java.time.LocalDateTime;

public record ClienteResponse(
        Long id,
        String nome,
        String cpfCnpj,
        String telefone,
        String email,
        Boolean ativo,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
}
