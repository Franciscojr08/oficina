package br.com.prime.oficina.estoque.application;

import br.com.prime.oficina.estoque.domain.Estoque;
import br.com.prime.oficina.estoque.infrastructure.EstoqueRepository;
import br.com.prime.oficina.item.domain.Item;
import br.com.prime.oficina.item.infrastructure.ItemRepository;
import br.com.prime.oficina.movimentoEstoque.domain.MovimentoEstoque;
import br.com.prime.oficina.movimentoEstoque.domain.TipoMovimentoEstoque;
import br.com.prime.oficina.movimentoEstoque.infrastructure.MovimentoEstoqueRepository;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        Estoque estoque = estoqueRepository.findByItemId(itemId)
                .orElseGet(() -> {
                    Estoque novoEstoque = new Estoque();
                    novoEstoque.setItem(item);
                    novoEstoque.setQuantidade(0);
                    novoEstoque.setEstoqueMinimo(0);
                    return novoEstoque;
                });

        Integer quantidadeAnterior = estoque.getQuantidade();
        Integer quantidadeNova = request.quantidade();

        estoque.setQuantidade(quantidadeNova);
        estoque.setEstoqueMinimo(request.estoqueMinimo());

        Estoque atualizado = estoqueRepository.save(estoque);

        if (!quantidadeAnterior.equals(quantidadeNova)) {
            MovimentoEstoque movimentacao = new MovimentoEstoque();
            movimentacao.setItem(item);
            movimentacao.setTipo(TipoMovimentoEstoque.AJUSTE);
            movimentacao.setQuantidade(Math.abs(quantidadeNova - quantidadeAnterior));
            movimentacao.setObservacao(
                    "Ajuste manual de estoque. Quantidade anterior: "
                            + quantidadeAnterior + ", nova quantidade: " + quantidadeNova
            );
            movimentoEstoqueRepository.save(movimentacao);
        }

        return toResponse(atualizado);
    }

    private Item buscarItemPorId(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item não encontrado"));
    }

    private Estoque buscarEstoquePorItemId(Long itemId) {
        return estoqueRepository.findByItemId(itemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Estoque do item não encontrado"));
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