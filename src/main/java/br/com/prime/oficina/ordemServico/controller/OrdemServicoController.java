package br.com.prime.oficina.ordemServico.controller;

import br.com.prime.oficina.ordemServico.application.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/ordens")
@RequiredArgsConstructor
@Validated
public class OrdemServicoController {

    private final OrdemServicoService service;

    @GetMapping
    public ResponseEntity<List<OrdemServicoResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/cliente/{id}")
    public ResponseEntity<List<OrdemServicoResponse>> listarPorCliente(@PathVariable Long id) {
        return ResponseEntity.ok(service.listarPorCliente(id));
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<List<OrdemServicoResponse>> listarPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(service.listarPorCodigo(codigo));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrdemServicoResponse>> listarPorStatus(@PathVariable StatusOrdemServico status) {
        return ResponseEntity.ok(service.listarPorStatus(status));
    }

    @PostMapping
    public ResponseEntity<OrdemServicoResponse> criar(@RequestBody @Valid OrdemServicoRequest request) {
        OrdemServicoResponse response = service.criar(request);
        return ResponseEntity
                .created(URI.create("/ordens/" + response.id()))
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrdemServicoResponse> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid OrdemServicoRequest request
    ) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PostMapping("/{id}/itens")
    public ResponseEntity<OrdemServicoResponse> adicionarItem(@PathVariable Long id, @RequestBody ItemOrdemServicoRequest request) {
        return ResponseEntity.ok(service.adicionarItem(id, request));
    }

	@GetMapping("/{id}/itens")
	public ResponseEntity<List<ItemOrdemServicoResponse>> listarItensPorOrdemServico(@PathVariable Long id) {
		return ResponseEntity.ok(service.listarItensPorOrdemServico(id));
	}

    @PostMapping("/{id}/servicos")
    public ResponseEntity<OrdemServicoResponse> adicionarServico(@PathVariable Long id, @RequestBody ServicoOrdemServicoRequest request) {
        return ResponseEntity.ok(service.adicionarServico(id, request));
    }

	@GetMapping("/{id}/servicos")
	public ResponseEntity<List<ServicoOrdemServicoResponse>> listarServicosPorOrdemServico(@PathVariable Long id) {
		return ResponseEntity.ok(service.listarServicosPorOrdemServico(id));
	}

	@PatchMapping("/{id}/aprovar")
    public ResponseEntity<OrdemServicoResponse> aprovarOrdemServico(@PathVariable Long id) {
        return ResponseEntity.ok(service.aprovarOrdemServico(id));
    }

	@PatchMapping("/{id}/reprovar")
    public ResponseEntity<OrdemServicoResponse> reprovarOrdemServico(@PathVariable Long id) {
        return ResponseEntity.ok(service.reprovarOrdemServico(id));
    }

	@PatchMapping("/{id}/iniciar-diagnostico")
	public ResponseEntity<OrdemServicoResponse> iniciarDiagnostico(@PathVariable Long id) {
		return ResponseEntity.ok(service.iniciarDiagnostico(id));
	}

	@PatchMapping("/{id}/solicitar-aprovacao")
	public ResponseEntity<OrdemServicoResponse> solicitarAprovacao(@PathVariable Long id) {
		return ResponseEntity.ok(service.solicitarAprovacao(id));
	}

	@PatchMapping("/{id}/servicos/{servicoId}/iniciar")
	public ResponseEntity<ServicoOrdemServicoResponse> iniciarServico(@PathVariable Long id, @PathVariable Long servicoId) {
		return ResponseEntity.ok(service.iniciarServico(id,servicoId));
	}

	@PatchMapping("/{id}/servicos/{servicoId}/finalizar")
	public ResponseEntity<ServicoOrdemServicoResponse> finalizarServico(
		@PathVariable Long id,
		@PathVariable Long servicoId
	) {
		return ResponseEntity.ok(service.finalizarServico(id,servicoId));
	}

	@PatchMapping("/{id}/entregar")
	public ResponseEntity<OrdemServicoResponse> entregarOrdemServico(@PathVariable Long id) {
		return ResponseEntity.ok(service.entregarOrdemServico(id));
	}
}
