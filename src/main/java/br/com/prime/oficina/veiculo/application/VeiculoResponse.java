package br.com.prime.oficina.veiculo.application;

import java.time.LocalDateTime;

public record VeiculoResponse(
        Long id,
        Long clienteId,
        String placa,
        String marca,
        String modelo,
        Integer ano,
        String cor,
        String observacao,
        Boolean ativo,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
}