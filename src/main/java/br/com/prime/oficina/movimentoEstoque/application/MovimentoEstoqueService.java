package br.com.prime.oficina.movimentoEstoque.application;

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
public class MovimentoEstoqueService {

    private final MovimentoEstoqueRepository movimentoEstoqueRepository;
    private final ItemRepository itemRepository;

    public List<MovimentoEstoqueResponse> listarPorItem(Long itemId) {
        validarItemExiste(itemId);

        return movimentoEstoqueRepository.findByItemIdOrderByDataMovimentacaoDesc(itemId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<MovimentoEstoqueResponse> listarPorItemETipo(Long itemId, TipoMovimentoEstoque tipo) {
        validarItemExiste(itemId);

        return movimentoEstoqueRepository.findByItemIdAndTipoOrderByDataMovimentacaoDesc(itemId, tipo)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void validarItemExiste(Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new RecursoNaoEncontradoException("Item não encontrado");
        }
    }

    private MovimentoEstoqueResponse toResponse(MovimentoEstoque movimentacao) {
        return new MovimentoEstoqueResponse(
                movimentacao.getId(),
                movimentacao.getItem().getId(),
                movimentacao.getItem().getNome(),
                movimentacao.getTipo(),
                movimentacao.getQuantidade(),
                movimentacao.getObservacao(),
                movimentacao.getOrdemServicoId(),
                movimentacao.getDataMovimentacao()
        );
    }
}