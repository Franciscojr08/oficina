package br.com.prime.oficina.ordemservico.itens.output.persistence;

import br.com.prime.oficina.item.domain.Item;
import br.com.prime.oficina.ordemservico.application.StatusOrdemServico;
import br.com.prime.oficina.ordemservico.itens.application.gateway.ItemOrdemServicoGateway;
import br.com.prime.oficina.ordemservico.itens.domain.ItemOrdemServico;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ItemOrdemServicoPersistenceAdapter implements ItemOrdemServicoGateway {

	private final ItemOrdemServicoRepository itemOrdemServicoRepository;

	@Override
	public ItemOrdemServico save(ItemOrdemServico itemOrdemServico) {
		return itemOrdemServicoRepository.save(itemOrdemServico);
	}

	@Override
	public Optional<ItemOrdemServico> findByItem(Item item) {
		return itemOrdemServicoRepository.findByItem(item);
	}

	@Override
	public List<ItemOrdemServico> findByOrdemServicoId(Long ordemServicoId) {
		return itemOrdemServicoRepository.findByOrdemServicoId(ordemServicoId);
	}

	@Override
	public int sumQuantidadeByOrdemServicoIdAndItemId(Long ordemId, Long itemId) {
		return itemOrdemServicoRepository.sumQuantidadeByOrdemServicoIdAndItemId(ordemId, itemId);
	}

	@Override
	public boolean existsByItemIdAndOrdemServicoStatusIn(Long itemId, List<StatusOrdemServico> status) {
		return itemOrdemServicoRepository.existsByItemIdAndOrdemServicoStatusIn(itemId, status);
	}
}
