package br.com.prime.oficina.ordemservico.itens.application.usecase;

import br.com.prime.oficina.ordemservico.itens.application.dto.ItemOrdemServicoRequest;
import br.com.prime.oficina.ordemservico.itens.application.ItemOrdemServicoService;
import br.com.prime.oficina.ordemservico.itens.application.dto.ListaItensOrdemServicoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemOrdemServicoUseCase {

	private final ItemOrdemServicoService itemOrdemServicoService;

	public ListaItensOrdemServicoResponse adicionarItem(Long ordemServicoId, ItemOrdemServicoRequest request) {
		itemOrdemServicoService.adicionarItem(ordemServicoId, request);
		return itemOrdemServicoService.listarItensPorOrdemServico(ordemServicoId);
	}

	public ListaItensOrdemServicoResponse listarItensPorOrdemServico(Long ordemServicoId) {
		return itemOrdemServicoService.listarItensPorOrdemServico(ordemServicoId);
	}
}