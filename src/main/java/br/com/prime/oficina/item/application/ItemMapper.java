package br.com.prime.oficina.item.application;

import br.com.prime.oficina.item.application.dto.*;

import br.com.prime.oficina.estoque.domain.Estoque;
import br.com.prime.oficina.item.domain.Item;
import org.springframework.stereotype.Component;

@Component
public class ItemMapper {

	public ItemResponse toResponse(Item item, Estoque estoque) {
		return new ItemResponse(
				item.getId(),
				item.getNome(),
				item.getDescricao(),
				item.getTipo(),
				item.getValorUnitario(),
				item.getUnidadeMedida(),
				item.getAtivo(),
				estoque.getQuantidade(),
				estoque.getEstoqueMinimo(),
				item.getDataCriacao(),
				item.getDataAtualizacao()
		);
	}
}
