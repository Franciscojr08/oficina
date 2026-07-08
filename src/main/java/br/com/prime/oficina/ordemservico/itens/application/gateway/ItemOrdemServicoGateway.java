package br.com.prime.oficina.ordemservico.itens.application.gateway;

import br.com.prime.oficina.ordemservico.application.StatusOrdemServico;
import br.com.prime.oficina.item.domain.Item;
import br.com.prime.oficina.ordemservico.itens.domain.ItemOrdemServico;

import java.util.List;
import java.util.Optional;

public interface ItemOrdemServicoGateway {

	ItemOrdemServico save(ItemOrdemServico itemOrdemServico);

	Optional<ItemOrdemServico> findByItem(Item item);

	List<ItemOrdemServico> findByOrdemServicoId(Long ordemServicoId);

	int sumQuantidadeByOrdemServicoIdAndItemId(Long ordemId, Long itemId);

	boolean existsByItemIdAndOrdemServicoStatusIn(Long itemId, List<StatusOrdemServico> status);
}
