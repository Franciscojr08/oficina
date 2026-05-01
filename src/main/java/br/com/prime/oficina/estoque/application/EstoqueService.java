package br.com.prime.oficina.estoque.application;

import br.com.prime.oficina.estoque.domain.Estoque;
import br.com.prime.oficina.estoque.infrastructure.EstoqueRepository;
import br.com.prime.oficina.item.domain.Item;
import br.com.prime.oficina.item.infrastructure.ItemRepository;
import br.com.prime.oficina.movimentoestoque.domain.MovimentoEstoque;
import br.com.prime.oficina.movimentoestoque.domain.TipoMovimentoEstoque;
import br.com.prime.oficina.movimentoestoque.infrastructure.MovimentoEstoqueRepository;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static br.com.prime.oficina.shared.exception.ExceptionMessage.ITEM_NOT_FOUND;
import static br.com.prime.oficina.shared.exception.ExceptionMessage.ITEM_STOCK_ERROR;

@Service
@RequiredArgsConstructor
public class EstoqueService {

    private final EstoqueRepository estoqueRepository;
    private final ItemRepository itemRepository;
    private final MovimentoEstoqueRepository movimentoEstoqueRepository;

    public List<EstoqueResponse> listarTodos() {
        return estoqueRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public EstoqueResponse buscarPorItem(Long itemId) {
        Estoque estoque = buscarEstoquePorItemId(itemId);
        return toResponse(estoque);
    }

    @Transactional
    public EstoqueResponse atualizarPorItem(Long itemId, EstoqueRequest request) {
        Item item = buscarItemPorId(itemId);
		Estoque estoque = buscarEstoquePorItemId(itemId);

        Integer quantidadeAnterior = estoque.getQuantidade();
        Integer quantidadeNova = request.quantidade();

        estoque.setQuantidade(quantidadeNova);
        estoque.setEstoqueMinimo(request.estoqueMinimo());

        estoqueRepository.save(estoque);

		lancarMovimentacaoEstoque(quantidadeNova, quantidadeAnterior, item);

        return toResponse(estoque);
    }

	private void lancarMovimentacaoEstoque(Integer quantidadeNova, Integer quantidadeAnterior, Item item) {
		if (quantidadeAnterior.equals(quantidadeNova)) {
			return;
		}

		Integer quantidadeMovimentacao = Math.abs(quantidadeNova - quantidadeAnterior);
		String observacao = String.format(
			"Ajuste manual de estoque. Quantidade anterior: %d, nova quantidade: %d",
			quantidadeAnterior,
			quantidadeNova
		);

		MovimentoEstoque movimentacao = new MovimentoEstoque();
		movimentacao.setItem(item);
		movimentacao.setTipo(TipoMovimentoEstoque.AJUSTE);
		movimentacao.setQuantidade(quantidadeMovimentacao);
		movimentacao.setObservacao(observacao);
		movimentoEstoqueRepository.save(movimentacao);
	}

	private Item buscarItemPorId(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(ITEM_NOT_FOUND));
    }

    private Estoque buscarEstoquePorItemId(Long itemId) {
        return estoqueRepository.findByItemId(itemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(ITEM_STOCK_ERROR));
    }

    private EstoqueResponse toResponse(Estoque estoque) {
        return new EstoqueResponse(
                estoque.getId(),
                estoque.getItem().getId(),
                estoque.getItem().getNome(),
                estoque.getQuantidade(),
                estoque.getEstoqueMinimo(),
                estoque.getDataCriacao(),
                estoque.getDataAtualizacao()
        );
    }
}