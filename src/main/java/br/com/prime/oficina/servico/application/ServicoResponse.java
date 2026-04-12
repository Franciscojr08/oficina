package br.com.prime.oficina.servico.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServicoResponse(
        Long id,
        String nome,
        String descricao,
        BigDecimal precoBase,
        Integer tempoEstimadoMinutos,
        Boolean ativo,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
}