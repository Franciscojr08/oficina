package br.com.prime.oficina.ordemservico.itens.application;

import br.com.prime.oficina.estoque.domain.Estoque;
import br.com.prime.oficina.item.application.gateway.ItemGateway;
import br.com.prime.oficina.item.domain.Item;
import br.com.prime.oficina.ordemservico.application.OrdemServicoStatusService;
import br.com.prime.oficina.ordemservico.application.StatusOrdemServico;
import br.com.prime.oficina.ordemservico.application.gateway.OrdemServicoGateway;
import br.com.prime.oficina.ordemservico.domain.OrdemServico;
import br.com.prime.oficina.ordemservico.itens.domain.ItemOrdemServico;
import br.com.prime.oficina.ordemservico.itens.application.gateway.ItemOrdemServicoGateway;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static br.com.prime.oficina.shared.exception.ExceptionMessage.*;

@Service
@RequiredArgsConstructor
public class ItemOrdemServicoService {

	private final OrdemServicoGateway ordemServicoGateway;
	private final ItemGateway itemGateway;
	private final ItemOrdemServicoGateway itemOrdemServicoGateway;
	private final OrdemServicoStatusService ordemServicoStatusService;

	public ListaItensOrdemServicoResponse listarItensPorOrdemServico(Long id) {
		List<ItemOrdemServico> itemOrdemServicoList = itemOrdemServicoGateway.findByOrdemServicoId(id);

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
	public void adicionarItem(Long id, ItemOrdemServicoRequest request) {
		OrdemServico ordemServico = buscarOrdemServicoPorId(id);

		String mensagem = String.format(
			CANNOT_ADD_ITEM_WITH_ORDER_OUTSIDE_DIAGNOSIS,
			StatusOrdemServico.EM_DIAGNOSTICO.getDescricao()
		);
		ordemServicoStatusService.validarEmDiagnostico(ordemServico, mensagem);

		adicionarItemNaOrdem(ordemServico, request);
	}

	@Transactional
	public void adicionarItemDuranteCadastro(OrdemServico ordemServico, ItemOrdemServicoRequest request) {
		adicionarItemNaOrdem(ordemServico, request);
	}

	private void adicionarItemNaOrdem(OrdemServico ordemServico, ItemOrdemServicoRequest request) {
		Item item = buscarItemPorId(request.itemId());
		Estoque estoque = item.getEstoque();

		if (Boolean.FALSE.equals(item.getAtivo())) {
			throw new RegraNegocioException(NOT_ACTIVE_ITEM);
		}

		int quantidadeAtual = itemOrdemServicoGateway.sumQuantidadeByOrdemServicoIdAndItemId(
				ordemServico.getId(),
				item.getId()
		);
		int totalSolicitado = quantidadeAtual + request.quantidade();

		if (estoque.getQuantidade() < totalSolicitado) {
			throw new RegraNegocioException(NOT_ENOUGH_STOCK_ITEM + item.getNome());
		}

		ItemOrdemServico itemOrdemServico = new ItemOrdemServico();
		preencherItemOrdemServico(itemOrdemServico, ordemServico, item, request);

		itemOrdemServicoGateway.save(itemOrdemServico);

		BigDecimal novoTotal = calcularNovoValorTotalItens(itemOrdemServico, ordemServico);
		ordemServico.setValorTotalItens(novoTotal);

		ordemServicoGateway.saveAndFlush(ordemServico);
	}

	private Item buscarItemPorId(Long id) {
		return itemGateway.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException(ITEM_NOT_FOUND));
	}

	private OrdemServico buscarOrdemServicoPorId(Long id) {
		return ordemServicoGateway.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException(SERVICE_ORDER_NOT_FOUND));
	}

	private BigDecimal calcularNovoValorTotalItens(ItemOrdemServico itemOrdemServico, OrdemServico ordemServico) {
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
