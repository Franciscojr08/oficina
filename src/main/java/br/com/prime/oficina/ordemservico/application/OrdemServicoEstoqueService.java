package br.com.prime.oficina.ordemservico.application;

import br.com.prime.oficina.estoque.application.gateway.EstoqueGateway;
import br.com.prime.oficina.item.domain.Item;
import br.com.prime.oficina.movimentoestoque.application.gateway.MovimentoEstoqueGateway;
import br.com.prime.oficina.movimentoestoque.domain.MovimentoEstoque;
import br.com.prime.oficina.movimentoestoque.domain.TipoMovimentoEstoque;
import br.com.prime.oficina.ordemservico.domain.OrdemServico;
import br.com.prime.oficina.ordemservico.itens.domain.ItemOrdemServico;
import br.com.prime.oficina.ordemservico.itens.application.gateway.ItemOrdemServicoGateway;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static br.com.prime.oficina.shared.exception.ExceptionMessage.NOT_ENOUGH_STOCK_ITEM;

@Service
@RequiredArgsConstructor
public class OrdemServicoEstoqueService {

	private static final String SAIDA_DEFAULT_ITEM = "BAIXA DE ITEM NO ESTOQUE";

	private final ItemOrdemServicoGateway itemOrdemServicoGateway;
	private final EstoqueGateway estoqueGateway;
	private final MovimentoEstoqueGateway movimentoEstoqueGateway;

	public boolean temEstoqueCompletoParaOrdem(Long ordemServicoId) {
		return estoqueGateway.temEstoqueCompletoParaOrdem(ordemServicoId);
	}

	public void baixarEstoqueDaOrdem(OrdemServico ordemServico) {
		List<ItemOrdemServico> itens = itemOrdemServicoGateway.findByOrdemServicoId(ordemServico.getId());
		List<MovimentoEstoque> movimentos = new ArrayList<>(itens.size());

		for (ItemOrdemServico itemOrdemServico : itens) {
			Item item = itemOrdemServico.getItem();
			Long estoqueId = item.getEstoque().getId();
			int quantidade = itemOrdemServico.getQuantidade();

			int atualizado = estoqueGateway.baixarEstoque(estoqueId, quantidade);

			if (atualizado == 0) {
				throw new RegraNegocioException(NOT_ENOUGH_STOCK_ITEM + item.getNome());
			}

			movimentos.add(criarMovimentoEstoque(item, quantidade, ordemServico.getId()));
		}

		movimentoEstoqueGateway.saveAll(movimentos);
	}

	private MovimentoEstoque criarMovimentoEstoque(Item item, int quantidade, Long ordemServicoId) {
		MovimentoEstoque movimento = new MovimentoEstoque();
		movimento.setItem(item);
		movimento.setTipo(TipoMovimentoEstoque.SAIDA);
		movimento.setQuantidade(quantidade);
		movimento.setOrdemServicoId(ordemServicoId);
		movimento.setObservacao(SAIDA_DEFAULT_ITEM);
		movimento.setDataMovimentacao(LocalDateTime.now());
		return movimento;
	}
}
