package br.com.prime.oficina.item.application.usecase;

import br.com.prime.oficina.item.application.ItemAtualizacaoRequest;
import br.com.prime.oficina.item.application.ItemRequest;
import br.com.prime.oficina.item.application.ItemResponse;
import br.com.prime.oficina.item.application.ItemService;
import br.com.prime.oficina.item.domain.TipoItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemUseCase {

	private final ItemService itemService;

	public ItemResponse criar(ItemRequest request) {
		return itemService.criar(request);
	}

	public List<ItemResponse> listar() {
		return itemService.listar();
	}

	public ItemResponse buscarPorId(Long id) {
		return itemService.buscarPorId(id);
	}

	public List<ItemResponse> listarPorTipo(TipoItem tipo) {
		return itemService.listarPorTipo(tipo);
	}

	public ItemResponse atualizar(Long id, ItemAtualizacaoRequest request) {
		return itemService.atualizar(id, request);
	}

	public void inativar(Long id) {
		itemService.inativar(id);
	}
}