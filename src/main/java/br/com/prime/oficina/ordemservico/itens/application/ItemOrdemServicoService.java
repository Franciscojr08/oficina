package br.com.prime.oficina.ordemservico.itens.application;

import br.com.prime.oficina.estoque.domain.Estoque;
import br.com.prime.oficina.item.domain.Item;
import br.com.prime.oficina.item.infrastructure.ItemRepository;
import br.com.prime.oficina.ordemservico.domain.OrdemServico;
import br.com.prime.oficina.ordemservico.infrastructure.OrdemServicoRepository;
import br.com.prime.oficina.ordemservico.itens.domain.ItemOrdemServico;
import br.com.prime.oficina.ordemservico.itens.infrastructure.ItemOrdemServicoRepository;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemOrdemServicoService {

	private final OrdemServicoRepository ordemServicoRepository;
	private final ItemRepository itemRepository;
	private final ItemOrdemServicoRepository itemOrdemServicoRepository;

	public ListaItensOrdemServicoResponse listarItensPorOrdemServico(Long id) {
		List<ItemOrdemServico> itemOrdemServicoList = itemOrdemServicoRepository.findByOrdemServicoId(id);

		List<ItemOrdemServicoResponse> itens = itemOrdemServicoList.stream()
				.map(this::toItemResponse)
				.toList();

		BigDecimal total = itemOrdemServicoList.stream()
				.map(item -> item.getValorUnitario()
						.multiply(BigDecimal.valueOf(item.getQuantidade())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		return new ListaItensOrdemServicoResponse(itens, total);
	}

	@Transactional
	public ListaItensOrdemServicoResponse adicionarItem(Long id, ItemOrdemServicoRequest request) {
		OrdemServico ordemServico = buscarOrdemServicoPorId(id);

		if (ordemServico.getStatus().estaEmEdicao()) {
			String mensagem = String.format(
				"Não é possível adicionar o item, pois a ordem de serviço está %s",
				ordemServico.getStatus().getDescricao()
			);
			throw new RegraNegocioException(mensagem);
		}

		Item item = buscarItemPorId(request.itemId());
		Estoque estoque = item.getEstoque();

		if (!item.getAtivo()) {
			throw new RegraNegocioException("O Item informado não está ativo");
		}

		int quantidadeAtual = itemOrdemServicoRepository.sumQuantidadeByOrdemServicoIdAndItemId(id, item.getId());
		int totalSolicitado = quantidadeAtual + request.quantidade();

		if (estoque.getQuantidade() < totalSolicitado) {
			throw new RegraNegocioException("Estoque insuficiente para o item: " + item.getNome());
		}

		ItemOrdemServico itemOrdemServico = new ItemOrdemServico();
		preencherItemOrdemServico(itemOrdemServico, ordemServico, item, request);

		itemOrdemServicoRepository.save(itemOrdemServico);

		BigDecimal novoTotal = getValorTotalItens(itemOrdemServico, ordemServico);
		ordemServico.setValorTotalItens(novoTotal);

		ordemServicoRepository.saveAndFlush(ordemServico);

		return listarItensPorOrdemServico(id);
	}

	private Item buscarItemPorId(Long id) {
		return itemRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Item não encontrado"));
	}

	private OrdemServico buscarOrdemServicoPorId(Long id) {
		return ordemServicoRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de Serviço não encontrada"));
	}

	private BigDecimal getValorTotalItens(ItemOrdemServico itemOrdemServico, OrdemServico ordemServico) {
		BigDecimal valorItem = itemOrdemServico.getValorUnitario()
				.multiply(BigDecimal.valueOf(itemOrdemServico.getQuantidade()));
		return ordemServico.getValorTotalItens().add(valorItem);
	}

	private void preencherItemOrdemServico(
			ItemOrdemServico itemOrdemServico,
			OrdemServico ordemServico,
			Item item,
			ItemOrdemServicoRequest request
	) {
		itemOrdemServico.setOrdemServico(ordemServico);
		itemOrdemServico.setItem(item);
		itemOrdemServico.setQuantidade(request.quantidade());
		itemOrdemServico.setValorUnitario(item.getValorUnitario());
	}

	private ItemOrdemServicoResponse toItemResponse(ItemOrdemServico itemOS) {
		BigDecimal quantidade = BigDecimal.valueOf(itemOS.getQuantidade());
		BigDecimal valorUnitario = itemOS.getValorUnitario();

		BigDecimal valorTotal = valorUnitario.multiply(quantidade);

		return new ItemOrdemServicoResponse(
			itemOS.getOrdemServico().getCodigo(),
			itemOS.getItem().getId(),
			itemOS.getItem().getNome(),
			itemOS.getQuantidade(),
			valorUnitario,
			valorTotal
		);
	}
}
