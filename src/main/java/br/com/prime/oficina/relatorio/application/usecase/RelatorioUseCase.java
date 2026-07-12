package br.com.prime.oficina.relatorio.application.usecase;

import br.com.prime.oficina.relatorio.application.dto.RelatorioResponse;
import br.com.prime.oficina.relatorio.application.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RelatorioUseCase {

	private final RelatorioService relatorioService;

	public RelatorioResponse calcularTempoMedioOS() {
		return relatorioService.calcularTempoMedioOS();
	}

	public RelatorioResponse calcularTempoMedioServicos() {
		return relatorioService.calcularTempoMedioServicos();
	}
}