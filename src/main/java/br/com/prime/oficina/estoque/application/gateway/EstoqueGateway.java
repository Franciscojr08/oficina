package br.com.prime.oficina.estoque.application.gateway;

import br.com.prime.oficina.estoque.domain.Estoque;

import java.util.List;
import java.util.Optional;

public interface EstoqueGateway {

	Estoque save(Estoque estoque);

	Estoque saveAndFlush(Estoque estoque);

	List<Estoque> findAll();

	Optional<Estoque> findByItemId(Long itemId);

	int baixarEstoque(Long id, int qtd);

	boolean temEstoqueCompletoParaOrdem(Long ordemServicoId);
}
