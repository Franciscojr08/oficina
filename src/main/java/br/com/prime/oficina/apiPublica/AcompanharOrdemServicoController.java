package br.com.prime.oficina.apiPublica;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/ordens")
@RequiredArgsConstructor
public class AcompanharOrdemServicoController {

    private final AcompanharOrdemServicoPublicaService service;

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<AcompanharOrdemServicoPublicaResponse> acompanharPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(service.acompanharPorCodigo(codigo));
    }
}