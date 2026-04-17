package br.com.prime.oficina.item.application;

import br.com.prime.oficina.item.domain.Item;
import br.com.prime.oficina.item.domain.TipoItem;
import br.com.prime.oficina.item.infrastructure.ItemRepository;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public ItemResponse criar(ItemRequest request) {
        validarDuplicidade(request.tipo(), request.descricao(), request.unidadeMedida());

        Item item = new Item();
        preencherItem(item, request);

        Item salvo = itemRepository.save(item);
        return toResponse(salvo);
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> listar() {
        return itemRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> listarPorTipo(TipoItem tipo) {
        return itemRepository.findByTipo(tipo)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ItemResponse buscarPorId(Long id) {
        Item item = buscarItemPorId(id);
        return toResponse(item);
    }

    @Transactional
    public ItemResponse atualizar(Long id, ItemRequest request) {
        Item item = buscarItemPorId(id);

        if (itemRepository.existsDuplicadoNaAtualizacao(
                id,
                request.tipo(),
                request.descricao(),
                request.unidadeMedida()
        )) {
            throw new RegraNegocioException("Já existe item cadastrado com o mesmo tipo, descrição e unidade de medida");
        }

        preencherItem(item, request);

        Item atualizado = itemRepository.save(item);
        return toResponse(atualizado);
    }

    @Transactional
    public void inativar(Long id) {
        Item item = buscarItemPorId(id);

        // TODO: validar vínculo com OS em aberto quando o módulo de OS existir
        item.setAtivo(false);
        itemRepository.save(item);
    }

    private void validarDuplicidade(TipoItem tipo, String descricao, String unidadeMedida) {
        if (itemRepository.existsDuplicado(tipo, descricao, unidadeMedida)) {
            throw new RegraNegocioException("Já existe item cadastrado com o mesmo tipo, descrição e unidade de medida");
        }
    }

    private Item buscarItemPorId(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item não encontrado"));
    }

    private void preencherItem(Item item, ItemRequest request) {
        item.setNome(request.nome());
        item.setDescricao(request.descricao());
        item.setTipo(request.tipo());
        item.setValorUnitario(request.valorUnitario());
        item.setUnidadeMedida(request.unidadeMedida());
    }

    private ItemResponse toResponse(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getNome(),
                item.getDescricao(),
                item.getTipo(),
                item.getValorUnitario(),
                item.getUnidadeMedida(),
                item.getAtivo(),
                item.getDataCriacao(),
                item.getDataAtualizacao()
        );
    }
}