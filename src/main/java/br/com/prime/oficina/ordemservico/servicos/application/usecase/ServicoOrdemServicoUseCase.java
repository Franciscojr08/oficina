package br.com.prime.oficina.ordemservico.servicos.application.usecase;

import br.com.prime.oficina.ordemservico.servicos.application.dto.ListaServicosOrdemServicoResponse;
import br.com.prime.oficina.ordemservico.servicos.application.dto.ServicoOrdemServicoRequest;
import br.com.prime.oficina.ordemservico.servicos.application.dto.ServicoOrdemServicoResponse;
import br.com.prime.oficina.ordemservico.servicos.application.ServicoOrdemServicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServicoOrdemServicoUseCase {

	private final ServicoOrdemServicoService servicoOrdemServicoService;

	public ListaServicosOrdemServicoResponse adicionarServico(Long ordemServicoId, ServicoOrdemServicoRequest request) {
		servicoOrdemServicoService.adicionarServico(ordemServicoId, request);
		return servicoOrdemServicoService.listarServicosPorOrdemServico(ordemServicoId);
	}

	public ListaServicosOrdemServicoResponse listarServicosPorOrdemServico(Long ordemServicoId) {
		return servicoOrdemServicoService.listarServicosPorOrdemServico(ordemServicoId);
	}

	public ServicoOrdemServicoResponse iniciarServico(Long ordemServicoId, Long servicoId) {
		return servicoOrdemServicoService.iniciarServico(ordemServicoId, servicoId);
	}

	public ServicoOrdemServicoResponse finalizarServico(Long ordemServicoId, Long servicoId) {
		return servicoOrdemServicoService.finalizarServico(ordemServicoId, servicoId);
	}
}