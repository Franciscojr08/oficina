package br.com.prime.oficina.movimentoestoque.infrastructure;

import br.com.prime.oficina.movimentoestoque.application.gateway.MovimentoEstoqueGateway;
import br.com.prime.oficina.movimentoestoque.domain.MovimentoEstoque;
import br.com.prime.oficina.movimentoestoque.domain.TipoMovimentoEstoque;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MovimentoEstoquePersistenceAdapter implements MovimentoEstoqueGateway {

	private final MovimentoEstoqueRepository movimentoEstoqueRepository;

	@Override
	public MovimentoEstoque save(MovimentoEstoque movimentoEstoque) {
		return movimentoEstoqueRepository.save(movimentoEstoque);
	}

	@Override
	public <S extends MovimentoEstoque> List<S> saveAll(Iterable<S> movimentos) {
		return movimentoEstoqueRepository.saveAll(movimentos);
	}

	@Override
	public List<MovimentoEstoque> findByItemIdOrderByDataMovimentacaoDesc(Long itemId) {
		return movimentoEstoqueRepository.findByItemIdOrderByDataMovimentacaoDesc(itemId);
	}

	@Override
	public List<MovimentoEstoque> findByItemIdAndTipoOrderByDataMovimentacaoDesc(Long itemId, TipoMovimentoEstoque tipo) {
		return movimentoEstoqueRepository.findByItemIdAndTipoOrderByDataMovimentacaoDesc(itemId, tipo);
	}

	@Override
	public List<MovimentoEstoque> findAllByOrderByDataMovimentacaoDesc() {
		return movimentoEstoqueRepository.findAllByOrderByDataMovimentacaoDesc();
	}
}
