package br.com.prime.oficina.ordemservico.controller;

import br.com.prime.oficina.ordemservico.application.*;
import br.com.prime.oficina.ordemservico.itens.application.ItemOrdemServicoRequest;
import br.com.prime.oficina.ordemservico.application.usecase.OrdemServicoUseCase;
import br.com.prime.oficina.ordemservico.itens.application.usecase.ItemOrdemServicoUseCase;
import br.com.prime.oficina.ordemservico.itens.application.ListaItensOrdemServicoResponse;
import br.com.prime.oficina.ordemservico.servicos.application.ListaServicosOrdemServicoResponse;
import br.com.prime.oficina.ordemservico.servicos.application.ServicoOrdemServicoRequest;
import br.com.prime.oficina.ordemservico.servicos.application.ServicoOrdemServicoResponse;
import br.com.prime.oficina.ordemservico.servicos.application.usecase.ServicoOrdemServicoUseCase;
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

    private final OrdemServicoUseCase ordemServicoUseCase;
	private final ItemOrdemServicoUseCase itemOrdemServicoUseCase;
	private final ServicoOrdemServicoUseCase servicoOrdemServicoUseCase;

	@GetMapping
    public ResponseEntity<List<OrdemServicoResponse>> listar() {
        return ResponseEntity.ok(ordemServicoUseCase.listar());
    }

    @GetMapping("/cliente/{id}")
    public ResponseEntity<List<OrdemServicoResponse>> listarPorCliente(@PathVariable Long id) {
        return ResponseEntity.ok(ordemServicoUseCase.listarPorCliente(id));
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<List<OrdemServicoResponse>> listarPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(ordemServicoUseCase.listarPorCodigo(codigo));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrdemServicoResponse>> listarPorStatus(@PathVariable StatusOrdemServico status) {
        return ResponseEntity.ok(ordemServicoUseCase.listarPorStatus(status));
    }

    @PostMapping
    public ResponseEntity<OrdemServicoResponse> criar(@RequestBody @Valid OrdemServicoRequest request) {
        OrdemServicoResponse response = ordemServicoUseCase.criar(request);
        return ResponseEntity
                .created(URI.create("/ordens/" + response.id()))
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrdemServicoResponse> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid OrdemServicoRequest request
    ) {
	    return ResponseEntity.ok(ordemServicoUseCase.atualizar(id, request));
    }

    @PostMapping("/{id}/itens")
    public ResponseEntity<ListaItensOrdemServicoResponse> adicionarItem(
			@PathVariable Long id,
			@RequestBody @Valid ItemOrdemServicoRequest request
	) {
		ListaItensOrdemServicoResponse response = itemOrdemServicoUseCase.adicionarItem(id, request);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/{id}/itens")
	public ResponseEntity<ListaItensOrdemServicoResponse> listarItensPorOrdemServico(@PathVariable Long id) {
		return ResponseEntity.ok(itemOrdemServicoUseCase.listarItensPorOrdemServico(id));
	}

    @PostMapping("/{id}/servicos")
    public ResponseEntity<ListaServicosOrdemServicoResponse> adicionarServico(
			@PathVariable Long id,
			@RequestBody @Valid ServicoOrdemServicoRequest request
	) {
		ListaServicosOrdemServicoResponse response = servicoOrdemServicoUseCase.adicionarServico(id, request);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/{id}/servicos")
	public ResponseEntity<ListaServicosOrdemServicoResponse> listarServicosPorOrdemServico(@PathVariable Long id) {
		return ResponseEntity.ok(servicoOrdemServicoUseCase.listarServicosPorOrdemServico(id));
	}

	@PatchMapping("/{id}/iniciar-execucao")
	public ResponseEntity<OrdemServicoResponse> iniciarExecucao(@PathVariable Long id) {
		return ResponseEntity.ok(ordemServicoUseCase.iniciarExecucao(id));
	}

	@PatchMapping("/{id}/aprovar")
    public ResponseEntity<OrdemServicoResponse> aprovarOrdemServico(@PathVariable Long id) {
        return ResponseEntity.ok(ordemServicoUseCase.aprovar(id));
    }

	@PatchMapping("/{id}/reprovar")
    public ResponseEntity<OrdemServicoResponse> reprovarOrdemServico(@PathVariable Long id) {
        return ResponseEntity.ok(ordemServicoUseCase.reprovar(id));
    }

	@PatchMapping("/{id}/iniciar-diagnostico")
	public ResponseEntity<OrdemServicoResponse> iniciarDiagnostico(@PathVariable Long id) {
		return ResponseEntity.ok(ordemServicoUseCase.iniciarDiagnostico(id));
	}

	@PatchMapping("/{id}/solicitar-aprovacao")
	public ResponseEntity<OrdemServicoResponse> solicitarAprovacao(@PathVariable Long id) {
		return ResponseEntity.ok(ordemServicoUseCase.solicitarAprovacao(id));
	}

	@PatchMapping("/{id}/servicos/{servicoId}/iniciar")
	public ResponseEntity<ServicoOrdemServicoResponse> iniciarServico(@PathVariable Long id, @PathVariable Long servicoId) {
		return ResponseEntity.ok(servicoOrdemServicoUseCase.iniciarServico(id,servicoId));
	}

	@PatchMapping("/{id}/servicos/{servicoId}/finalizar")
	public ResponseEntity<ServicoOrdemServicoResponse> finalizarServico(
		@PathVariable Long id,
		@PathVariable Long servicoId
	) {
		return ResponseEntity.ok(servicoOrdemServicoUseCase.finalizarServico(id,servicoId));
	}

	@PatchMapping("/{id}/entregar")
	public ResponseEntity<OrdemServicoResponse> entregarOrdemServico(@PathVariable Long id) {
		return ResponseEntity.ok(ordemServicoUseCase.entregar(id));
	}

	@GetMapping("/{id}/status")
	public ResponseEntity<StatusOrdemServicoResponse> status(@PathVariable Long id) {
		return ResponseEntity.ok(ordemServicoUseCase.consultarStatus(id));
	}
}
