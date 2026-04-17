package br.com.prime.oficina.item.application;

import br.com.prime.oficina.item.domain.TipoItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ItemResponse(
        Long id,
        String nome,
        String descricao,
        TipoItem tipo,
        BigDecimal valorUnitario,
        String unidadeMedida,
        Boolean ativo,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
}