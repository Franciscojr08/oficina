package br.com.prime.oficina.item.application;

import br.com.prime.oficina.item.application.dto.*;

import br.com.prime.oficina.estoque.application.gateway.EstoqueGateway;
import br.com.prime.oficina.estoque.domain.Estoque;
import br.com.prime.oficina.item.application.gateway.ItemGateway;
import br.com.prime.oficina.item.domain.Item;
import br.com.prime.oficina.item.domain.TipoItem;
import br.com.prime.oficina.movimentoestoque.application.gateway.MovimentoEstoqueGateway;
import br.com.prime.oficina.movimentoestoque.domain.MovimentoEstoque;
import br.com.prime.oficina.movimentoestoque.domain.TipoMovimentoEstoque;
import br.com.prime.oficina.ordemservico.application.StatusOrdemServico;
import br.com.prime.oficina.ordemservico.itens.application.gateway.ItemOrdemServicoGateway;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static br.com.prime.oficina.shared.exception.ExceptionMessage.*;

@Service
@RequiredArgsConstructor
public class ItemService {

    private static final String OBSERVACAO_PADRAO_CADASTRO = "Cadastro inicial do item";

    private final ItemGateway itemGateway;
    private final EstoqueGateway estoqueGateway;
    private final MovimentoEstoqueGateway movimentoEstoqueGateway;
    private final ItemOrdemServicoGateway itemOrdemServicoGateway;
	private final ItemMapper itemMapper;

    @Transactional
    public ItemResponse criar(ItemRequest request) {
        validarDuplicidade(request.tipo(), request.descricao(), request.unidadeMedida());

        Item item = new Item();
		item.setNome(request.nome());
		item.setDescricao(request.descricao());
		item.setTipo(request.tipo());
		item.setValorUnitario(request.valorUnitario());
		item.setUnidadeMedida(request.unidadeMedida());
		itemGateway.save(item);

        Estoque estoque = new Estoque();
		estoque.setItem(item);
        estoque.setQuantidade(request.quantidadeInicial());
        estoque.setEstoqueMinimo(request.estoqueMinimo());
		estoqueGateway.save(estoque);

        if (request.quantidadeInicial() > 0) {
            MovimentoEstoque movimento = new MovimentoEstoque();
			movimento.setItem(item);
            movimento.setTipo(TipoMovimentoEstoque.ENTRADA);
            movimento.setQuantidade(request.quantidadeInicial());
            movimento.setObservacao(
                    request.observacaoInicial() != null && !request.observacaoInicial().isBlank()
                            ? request.observacaoInicial()
                            : OBSERVACAO_PADRAO_CADASTRO
            );
            movimentoEstoqueGateway.save(movimento);
        }

		return itemMapper.toResponse(item, estoque);
    }

    public List<ItemResponse> listar() {
        return itemGateway.findAll()
                .stream()
                .map(item -> itemMapper.toResponse(item, buscarEstoquePorItemId(item.getId())))
                .toList();
    }

    public List<ItemResponse> listarPorTipo(TipoItem tipo) {
        return itemGateway.findByTipo(tipo)
                .stream()
                .map(item -> itemMapper.toResponse(item, buscarEstoquePorItemId(item.getId())))
                .toList();
    }

    public ItemResponse buscarPorId(Long id) {
        Item item = buscarItemPorId(id);
        Estoque estoque = buscarEstoquePorItemId(id);
        return itemMapper.toResponse(item, estoque);
    }

    @Transactional
    public ItemResponse atualizar(Long id, ItemAtualizacaoRequest request) {
        Item item = buscarItemPorId(id);

        if (itemGateway.existsDuplicadoNaAtualizacao(
                id,
                request.tipo(),
                request.descricao(),
                request.unidadeMedida()
        )) {
            throw new RegraNegocioException(SAME_ITEM_ERROR);
        }

		item.setNome(request.nome());
		item.setDescricao(request.descricao());
		item.setTipo(request.tipo());
		item.setValorUnitario(request.valorUnitario());
		item.setUnidadeMedida(request.unidadeMedida());
		itemGateway.save(item);

		Estoque estoque = buscarEstoquePorItemId(id);

		return itemMapper.toResponse(item, estoque);
    }

    @Transactional
    public void inativar(Long id) {
        Item item = buscarItemPorId(id);

		boolean estaEmOrdemAtiva = itemOrdemServicoGateway
				.existsByItemIdAndOrdemServicoStatusIn(
						id,
						StatusOrdemServico.statusAtivos()
				);

		if (estaEmOrdemAtiva) {
			throw new RegraNegocioException(CANNOT_INACTIVATE_ITEM_WITH_ACTIVE_SERVICE_ORDERS);
		}

        item.setAtivo(false);
        itemGateway.save(item);
    }

    private void validarDuplicidade(TipoItem tipo, String descricao, String unidadeMedida) {
        if (itemGateway.existsDuplicado(tipo, descricao, unidadeMedida)) {
            throw new RegraNegocioException(SAME_ITEM_ERROR);
        }
    }

    private Item buscarItemPorId(Long id) {
        return itemGateway.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(ITEM_NOT_FOUND));
    }

    private Estoque buscarEstoquePorItemId(Long itemId) {
        return estoqueGateway.findByItemId(itemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(ITEM_STOCK_ERROR));
    }

}
