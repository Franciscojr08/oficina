package br.com.prime.oficina.movimentoestoque.application.dto;

import br.com.prime.oficina.movimentoestoque.domain.TipoMovimentoEstoque;

import java.time.LocalDateTime;

public record MovimentoEstoqueResponse(
        Long id,
        Long itemId,
        String nomeItem,
        TipoMovimentoEstoque tipo,
        Integer quantidade,
        String observacao,
        Long ordemServicoId,
        LocalDateTime dataMovimentacao
) {
}