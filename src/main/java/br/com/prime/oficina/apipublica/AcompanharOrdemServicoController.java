package br.com.prime.oficina.apipublica;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/ordens")
@RequiredArgsConstructor
@Tag(name = "API Publica", description = "Endpoints publicos para acompanhamento de ordens de servico")
@SecurityRequirements
public class AcompanharOrdemServicoController {

    private final AcompanharOrdemServicoPublicaService service;

    @GetMapping("/codigo/{codigo}")
    @Operation(
            summary = "Acompanhar ordem de servico por codigo",
            description = "Consulta publica para clientes acompanharem o status de uma ordem de servico sem autenticacao.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ordem de servico encontrada",
                            content = @Content(
                                    schema = @Schema(implementation = AcompanharOrdemServicoPublicaResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "codigo": "OS-2026-0001",
                                              "descricaoProblema": "Barulho no motor",
                                              "status": "EM_EXECUCAO",
                                              "dataCadastro": "2026-05-01T10:00:00",
                                              "dataInicioExecucao": "2026-05-01T11:00:00",
                                              "dataFimExecucao": null,
                                              "dataEntregue": null,
                                              "dataCancelada": null
                                            }
                                            """)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Ordem de servico nao encontrada")
            }
    )
    public ResponseEntity<AcompanharOrdemServicoPublicaResponse> acompanharPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(service.acompanharPorCodigo(codigo));
    }
}
