package br.com.prime.oficina.estoque.application;

import java.time.LocalDateTime;

public record EstoqueResponse(
        Long id,
        Long itemId,
        String nomeItem,
        Integer quantidade,
        Integer estoqueMinimo,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
}