package br.com.prime.oficina.item.application.gateway;

import br.com.prime.oficina.item.domain.Item;
import br.com.prime.oficina.item.domain.TipoItem;

import java.util.List;
import java.util.Optional;

public interface ItemGateway {

	Item save(Item item);

	Item saveAndFlush(Item item);

	List<Item> findAll();

	Optional<Item> findById(Long id);

	boolean existsById(Long id);

	List<Item> findByTipo(TipoItem tipo);

	boolean existsDuplicado(TipoItem tipo, String descricao, String unidadeMedida);

	boolean existsDuplicadoNaAtualizacao(Long id, TipoItem tipo, String descricao, String unidadeMedida);
}
