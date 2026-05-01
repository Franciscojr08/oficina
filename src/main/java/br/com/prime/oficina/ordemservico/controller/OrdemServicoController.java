package br.com.prime.oficina.ordemservico.controller;

import br.com.prime.oficina.ordemservico.application.*;
import br.com.prime.oficina.ordemservico.itens.application.ItemOrdemServicoRequest;
import br.com.prime.oficina.ordemservico.itens.application.ItemOrdemServicoService;
import br.com.prime.oficina.ordemservico.itens.application.ListaItensOrdemServicoResponse;
import br.com.prime.oficina.ordemservico.servicos.application.ListaServicosOrdemServicoResponse;
import br.com.prime.oficina.ordemservico.servicos.application.ServicoOrdemServicoRequest;
import br.com.prime.oficina.ordemservico.servicos.application.ServicoOrdemServicoResponse;
import br.com.prime.oficina.ordemservico.servicos.application.ServicoOrdemServicoService;
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

    private final OrdemServicoService ordemServicoService;
	private final ItemOrdemServicoService itemOrdemServicoService;
	private final ServicoOrdemServicoService servicoOrdemServicoService;

    @GetMapping
    public ResponseEntity<List<OrdemServicoResponse>> listar() {
        return ResponseEntity.ok(ordemServicoService.listar());
    }

    @GetMapping("/cliente/{id}")
    public ResponseEntity<List<OrdemServicoResponse>> listarPorCliente(@PathVariable Long id) {
        return ResponseEntity.ok(ordemServicoService.listarPorCliente(id));
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<List<OrdemServicoResponse>> listarPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(ordemServicoService.listarPorCodigo(codigo));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrdemServicoResponse>> listarPorStatus(@PathVariable StatusOrdemServico status) {
        return ResponseEntity.ok(ordemServicoService.listarPorStatus(status));
    }

    @PostMapping
    public ResponseEntity<OrdemServicoResponse> criar(@RequestBody @Valid OrdemServicoRequest request) {
        OrdemServicoResponse response = ordemServicoService.criar(request);
        return ResponseEntity
                .created(URI.create("/ordens/" + response.id()))
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrdemServicoResponse> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid OrdemServicoRequest request
    ) {
        return ResponseEntity.ok(ordemServicoService.atualizar(id, request));
    }

    @PostMapping("/{id}/itens")
    public ResponseEntity<ListaItensOrdemServicoResponse> adicionarItem(@PathVariable Long id, @RequestBody ItemOrdemServicoRequest request) {
        return ResponseEntity.ok(itemOrdemServicoService.adicionarItem(id, request));
    }

	@GetMapping("/{id}/itens")
	public ResponseEntity<ListaItensOrdemServicoResponse> listarItensPorOrdemServico(@PathVariable Long id) {
		return ResponseEntity.ok(itemOrdemServicoService.listarItensPorOrdemServico(id));
	}

    @PostMapping("/{id}/servicos")
    public ResponseEntity<ListaServicosOrdemServicoResponse> adicionarServico(@PathVariable Long id, @RequestBody ServicoOrdemServicoRequest request) {
        return ResponseEntity.ok(servicoOrdemServicoService.adicionarServico(id, request));
    }

	@GetMapping("/{id}/servicos")
	public ResponseEntity<ListaServicosOrdemServicoResponse> listarServicosPorOrdemServico(@PathVariable Long id) {
		return ResponseEntity.ok(servicoOrdemServicoService.listarServicosPorOrdemServico(id));
	}

	@PatchMapping("/{id}/iniciar-execucao")
	public ResponseEntity<OrdemServicoResponse> iniciarExecucao(@PathVariable Long id) {
		return ResponseEntity.ok(ordemServicoService.iniciarExecucao(id));
	}

	@PatchMapping("/{id}/aprovar")
    public ResponseEntity<OrdemServicoResponse> aprovarOrdemServico(@PathVariable Long id) {
        return ResponseEntity.ok(ordemServicoService.aprovarOrdemServico(id));
    }

	@PatchMapping("/{id}/reprovar")
    public ResponseEntity<OrdemServicoResponse> reprovarOrdemServico(@PathVariable Long id) {
        return ResponseEntity.ok(ordemServicoService.reprovarOrdemServico(id));
    }

	@PatchMapping("/{id}/iniciar-diagnostico")
	public ResponseEntity<OrdemServicoResponse> iniciarDiagnostico(@PathVariable Long id) {
		return ResponseEntity.ok(ordemServicoService.iniciarDiagnostico(id));
	}

	@PatchMapping("/{id}/solicitar-aprovacao")
	public ResponseEntity<OrdemServicoResponse> solicitarAprovacao(@PathVariable Long id) {
		return ResponseEntity.ok(ordemServicoService.solicitarAprovacao(id));
	}

	@PatchMapping("/{id}/servicos/{servicoId}/iniciar")
	public ResponseEntity<ServicoOrdemServicoResponse> iniciarServico(@PathVariable Long id, @PathVariable Long servicoId) {
		return ResponseEntity.ok(servicoOrdemServicoService.iniciarServico(id,servicoId));
	}

	@PatchMapping("/{id}/servicos/{servicoId}/finalizar")
	public ResponseEntity<ServicoOrdemServicoResponse> finalizarServico(
		@PathVariable Long id,
		@PathVariable Long servicoId
	) {
		return ResponseEntity.ok(servicoOrdemServicoService.finalizarServico(id,servicoId));
	}

	@PatchMapping("/{id}/entregar")
	public ResponseEntity<OrdemServicoResponse> entregarOrdemServico(@PathVariable Long id) {
		return ResponseEntity.ok(ordemServicoService.entregarOrdemServico(id));
	}
}
