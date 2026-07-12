package br.com.prime.oficina.movimentoestoque.application.gateway;

import br.com.prime.oficina.movimentoestoque.domain.MovimentoEstoque;
import br.com.prime.oficina.movimentoestoque.domain.TipoMovimentoEstoque;

import java.util.List;

public interface MovimentoEstoqueGateway {

	MovimentoEstoque save(MovimentoEstoque movimentoEstoque);

	<S extends MovimentoEstoque> List<S> saveAll(Iterable<S> movimentos);

	List<MovimentoEstoque> findByItemIdOrderByDataMovimentacaoDesc(Long itemId);

	List<MovimentoEstoque> findByItemIdAndTipoOrderByDataMovimentacaoDesc(Long itemId, TipoMovimentoEstoque tipo);

	List<MovimentoEstoque> findAllByOrderByDataMovimentacaoDesc();
}
