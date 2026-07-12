package br.com.prime.oficina.estoque.output.persistence;

import br.com.prime.oficina.estoque.application.gateway.EstoqueGateway;
import br.com.prime.oficina.estoque.domain.Estoque;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EstoquePersistenceAdapter implements EstoqueGateway {

	private final EstoqueRepository estoqueRepository;

	@Override
	public Estoque save(Estoque estoque) {
		return estoqueRepository.save(estoque);
	}

	@Override
	public Estoque saveAndFlush(Estoque estoque) {
		return estoqueRepository.saveAndFlush(estoque);
	}

	@Override
	public List<Estoque> findAll() {
		return estoqueRepository.findAll();
	}

	@Override
	public Optional<Estoque> findByItemId(Long itemId) {
		return estoqueRepository.findByItemId(itemId);
	}

	@Override
	public int baixarEstoque(Long id, int qtd) {
		return estoqueRepository.baixarEstoque(id, qtd);
	}

	@Override
	public boolean temEstoqueCompletoParaOrdem(Long ordemServicoId) {
		return estoqueRepository.temEstoqueCompletoParaOrdem(ordemServicoId);
	}
}
