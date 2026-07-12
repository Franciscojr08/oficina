package br.com.prime.oficina.relatorio.application.dto;


public record RelatorioResponse(
	Double tempoMedioHoras,
	String tempoFormatado
) {
}
