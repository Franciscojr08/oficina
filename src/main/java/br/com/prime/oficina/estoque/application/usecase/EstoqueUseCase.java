package br.com.prime.oficina.estoque.application.usecase;

import br.com.prime.oficina.estoque.application.EstoqueRequest;
import br.com.prime.oficina.estoque.application.EstoqueResponse;
import br.com.prime.oficina.estoque.application.EstoqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EstoqueUseCase {

	private final EstoqueService estoqueService;

	public List<EstoqueResponse> listarTodos() {
		return estoqueService.listarTodos();
	}

	public EstoqueResponse buscarPorItem(Long itemId) {
		return estoqueService.buscarPorItem(itemId);
	}

	public EstoqueResponse atualizarPorItem(Long itemId, EstoqueRequest request) {
		return estoqueService.atualizarPorItem(itemId, request);
	}
}