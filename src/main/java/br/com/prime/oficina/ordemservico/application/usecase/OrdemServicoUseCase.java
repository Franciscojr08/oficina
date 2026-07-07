package br.com.prime.oficina.ordemservico.application.usecase;

import br.com.prime.oficina.ordemservico.application.dto.OrdemServicoRequest;
import br.com.prime.oficina.ordemservico.application.dto.OrdemServicoResponse;
import br.com.prime.oficina.ordemservico.application.OrdemServicoService;
import br.com.prime.oficina.ordemservico.application.StatusOrdemServico;
import br.com.prime.oficina.ordemservico.application.dto.StatusOrdemServicoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdemServicoUseCase {

	private final OrdemServicoService ordemServicoService;

	public List<OrdemServicoResponse> listar() {
		return ordemServicoService.listar();
	}

	public List<OrdemServicoResponse> listarPorCliente(Long clienteId) {
		return ordemServicoService.listarPorCliente(clienteId);
	}

	public List<OrdemServicoResponse> listarPorCodigo(String codigo) {
		return ordemServicoService.listarPorCodigo(codigo);
	}

	public List<OrdemServicoResponse> listarPorStatus(StatusOrdemServico status) {
		return ordemServicoService.listarPorStatus(status);
	}

	public OrdemServicoResponse criar(OrdemServicoRequest request) {
		return ordemServicoService.criar(request);
	}

	public OrdemServicoResponse atualizar(Long id, OrdemServicoRequest request) {
		return ordemServicoService.atualizar(id, request);
	}

	public OrdemServicoResponse iniciarExecucao(Long id) {
		return ordemServicoService.iniciarExecucao(id);
	}

	public OrdemServicoResponse aprovar(Long id) {
		return ordemServicoService.aprovarOrdemServico(id);
	}

	public OrdemServicoResponse reprovar(Long id) {
		return ordemServicoService.reprovarOrdemServico(id);
	}

	public OrdemServicoResponse iniciarDiagnostico(Long id) {
		return ordemServicoService.iniciarDiagnostico(id);
	}

	public OrdemServicoResponse solicitarAprovacao(Long id) {
		return ordemServicoService.solicitarAprovacao(id);
	}

	public OrdemServicoResponse entregar(Long id) {
		return ordemServicoService.entregarOrdemServico(id);
	}

	public StatusOrdemServicoResponse consultarStatus(Long id) {
		return ordemServicoService.consultarStatus(id);
	}
}