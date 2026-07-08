package br.com.prime.oficina.item.output.persistence;

import br.com.prime.oficina.item.application.gateway.ItemGateway;
import br.com.prime.oficina.item.domain.Item;
import br.com.prime.oficina.item.domain.TipoItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ItemPersistenceAdapter implements ItemGateway {

	private final ItemRepository itemRepository;

	@Override
	public Item save(Item item) {
		return itemRepository.save(item);
	}

	@Override
	public Item saveAndFlush(Item item) {
		return itemRepository.saveAndFlush(item);
	}

	@Override
	public List<Item> findAll() {
		return itemRepository.findAll();
	}

	@Override
	public Optional<Item> findById(Long id) {
		return itemRepository.findById(id);
	}

	@Override
	public boolean existsById(Long id) {
		return itemRepository.existsById(id);
	}

	@Override
	public List<Item> findByTipo(TipoItem tipo) {
		return itemRepository.findByTipo(tipo);
	}

	@Override
	public boolean existsDuplicado(TipoItem tipo, String descricao, String unidadeMedida) {
		return itemRepository.existsDuplicado(tipo, descricao, unidadeMedida);
	}

	@Override
	public boolean existsDuplicadoNaAtualizacao(Long id, TipoItem tipo, String descricao, String unidadeMedida) {
		return itemRepository.existsDuplicadoNaAtualizacao(id, tipo, descricao, unidadeMedida);
	}
}
