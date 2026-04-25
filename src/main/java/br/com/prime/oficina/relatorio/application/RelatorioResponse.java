package br.com.prime.oficina.relatorio.application;


public record RelatorioResponse(
	Double tempoMedioHoras,
	String tempoFormatado
) {
}
