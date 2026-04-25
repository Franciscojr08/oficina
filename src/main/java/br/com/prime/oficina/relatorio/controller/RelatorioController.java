package br.com.prime.oficina.relatorio.controller;

import br.com.prime.oficina.relatorio.application.RelatorioResponse;
import br.com.prime.oficina.relatorio.application.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

	private final RelatorioService relatorioService;

	@GetMapping("/ordens-servico/tempo-medio")
	public ResponseEntity<RelatorioResponse> calcularTempoMedioOS() {
		return ResponseEntity.ok(relatorioService.calcularTempoMedioOS());
	}

	@GetMapping("/ordens-servico/tempo-medio-servicos")
	public ResponseEntity<RelatorioResponse> calcularTempoMedioServicos() {
		return ResponseEntity.ok(relatorioService.calcularTempoMedioServicos());
	}
}
