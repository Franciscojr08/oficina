package br.com.prime.oficina.relatorio.entrypoint.controller;

import br.com.prime.oficina.relatorio.application.dto.RelatorioResponse;
import br.com.prime.oficina.relatorio.application.usecase.RelatorioUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/relatorios")
@RequiredArgsConstructor
@Tag(name = "Relatorios", description = "Indicadores operacionais da oficina")
@SecurityRequirement(name = "bearerAuth")
public class RelatorioController {

	private final RelatorioUseCase relatorioUseCase;

	@GetMapping("/ordens-servico/tempo-medio")
	@Operation(
			summary = "Calcular tempo medio de ordens de servico",
			description = "Retorna o tempo medio entre inicio e fim de execucao das ordens finalizadas ou entregues.",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "Tempo medio calculado",
							content = @Content(
									schema = @Schema(implementation = RelatorioResponse.class),
									examples = @ExampleObject(value = """
											{
											  "tempoMedioHoras": 2.5,
											  "tempoFormatado": "2 horas e 30 minutos"
											}
											""")
							)
					),
					@ApiResponse(responseCode = "401", description = "Token ausente ou invalido")
			}
	)
	public ResponseEntity<RelatorioResponse> calcularTempoMedioOS() {
		return ResponseEntity.ok(relatorioUseCase.calcularTempoMedioOS());
	}

	@GetMapping("/ordens-servico/tempo-medio-servicos")
	@Operation(
			summary = "Calcular tempo medio dos servicos executados",
			description = "Retorna o tempo medio entre inicio e fim dos servicos finalizados nas ordens de servico.",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "Tempo medio calculado",
							content = @Content(
									schema = @Schema(implementation = RelatorioResponse.class),
									examples = @ExampleObject(value = """
											{
											  "tempoMedioHoras": 1.25,
											  "tempoFormatado": "1 hora e 15 minutos"
											}
											""")
							)
					),
					@ApiResponse(responseCode = "401", description = "Token ausente ou invalido")
			}
	)
	public ResponseEntity<RelatorioResponse> calcularTempoMedioServicos() {
		return ResponseEntity.ok(relatorioUseCase.calcularTempoMedioServicos());
	}
}
