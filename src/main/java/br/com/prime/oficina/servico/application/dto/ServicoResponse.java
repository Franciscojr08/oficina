package br.com.prime.oficina.servico.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServicoResponse(
        Long id,
        String nome,
        String descricao,
        BigDecimal valor,
        Boolean ativo,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
}