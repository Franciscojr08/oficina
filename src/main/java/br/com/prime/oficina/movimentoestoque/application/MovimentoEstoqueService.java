package br.com.prime.oficina.movimentoestoque.application;

import br.com.prime.oficina.movimentoestoque.application.dto.*;

import br.com.prime.oficina.item.application.gateway.ItemGateway;
import br.com.prime.oficina.movimentoestoque.application.gateway.MovimentoEstoqueGateway;
import br.com.prime.oficina.movimentoestoque.domain.MovimentoEstoque;
import br.com.prime.oficina.movimentoestoque.domain.TipoMovimentoEstoque;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static br.com.prime.oficina.shared.exception.ExceptionMessage.ITEM_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class MovimentoEstoqueService {

    private final MovimentoEstoqueGateway movimentoEstoqueGateway;
    private final ItemGateway itemGateway;

    public List<MovimentoEstoqueResponse> listarPorItem(Long itemId) {
        validarItemExiste(itemId);

        return movimentoEstoqueGateway.findByItemIdOrderByDataMovimentacaoDesc(itemId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<MovimentoEstoqueResponse> listarPorItemETipo(Long itemId, TipoMovimentoEstoque tipo) {
        validarItemExiste(itemId);

        return movimentoEstoqueGateway.findByItemIdAndTipoOrderByDataMovimentacaoDesc(itemId, tipo)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void validarItemExiste(Long itemId) {
        if (!itemGateway.existsById(itemId)) {
            throw new RecursoNaoEncontradoException(ITEM_NOT_FOUND);
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

    public List<MovimentoEstoqueResponse> listarTodos() {
        return movimentoEstoqueGateway.findAllByOrderByDataMovimentacaoDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }
}
