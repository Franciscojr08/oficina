package br.com.prime.oficina.movimentoestoque.application.usecase;

import br.com.prime.oficina.movimentoestoque.application.MovimentoEstoqueResponse;
import br.com.prime.oficina.movimentoestoque.application.MovimentoEstoqueService;
import br.com.prime.oficina.movimentoestoque.domain.TipoMovimentoEstoque;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimentoEstoqueUseCase {

	private final MovimentoEstoqueService movimentoEstoqueService;

	public List<MovimentoEstoqueResponse> listarPorItem(Long itemId) {
		return movimentoEstoqueService.listarPorItem(itemId);
	}

	public List<MovimentoEstoqueResponse> listarPorItemETipo(Long itemId, TipoMovimentoEstoque tipo) {
		return movimentoEstoqueService.listarPorItemETipo(itemId, tipo);
	}

	public List<MovimentoEstoqueResponse> listarTodos() {
		return movimentoEstoqueService.listarTodos();
	}
}