package br.com.prime.oficina.item.application;

import br.com.prime.oficina.estoque.domain.Estoque;
import br.com.prime.oficina.estoque.infrastructure.EstoqueRepository;
import br.com.prime.oficina.item.domain.Item;
import br.com.prime.oficina.item.domain.TipoItem;
import br.com.prime.oficina.item.infrastructure.ItemRepository;
import br.com.prime.oficina.movimentoEstoque.domain.MovimentoEstoque;
import br.com.prime.oficina.movimentoEstoque.domain.TipoMovimentoEstoque;
import br.com.prime.oficina.movimentoEstoque.infrastructure.MovimentoEstoqueRepository;
import br.com.prime.oficina.ordemServico.application.StatusOrdemServico;
import br.com.prime.oficina.ordemServico.itens.domain.ItemOrdemServico;
import br.com.prime.oficina.ordemServico.domain.OrdemServico;
import br.com.prime.oficina.ordemServico.itens.infrastructure.ItemOrdemServicoRepository;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private static final String OBSERVACAO_PADRAO_CADASTRO = "Cadastro inicial do item";

    private final ItemRepository itemRepository;
    private final EstoqueRepository estoqueRepository;
    private final MovimentoEstoqueRepository movimentoEstoqueRepository;
    private final ItemOrdemServicoRepository itemOrdemServicoRepository;

    @Transactional
    public ItemResponse criar(ItemRequest request) {
        validarDuplicidade(request.tipo(), request.descricao(), request.unidadeMedida());

        Item item = new Item();
		item.setNome(request.nome());
		item.setDescricao(request.descricao());
		item.setTipo(request.tipo());
		item.setValorUnitario(request.valorUnitario());
		item.setUnidadeMedida(request.unidadeMedida());
		itemRepository.save(item);

        Estoque estoque = new Estoque();
		estoque.setItem(item);
        estoque.setQuantidade(request.quantidadeInicial());
        estoque.setEstoqueMinimo(request.estoqueMinimo());
		estoqueRepository.save(estoque);

        if (request.quantidadeInicial() > 0) {
            MovimentoEstoque movimento = new MovimentoEstoque();
			movimento.setItem(item);
            movimento.setTipo(TipoMovimentoEstoque.ENTRADA);
            movimento.setQuantidade(request.quantidadeInicial());
            movimento.setObservacao(
                    request.observacaoInicial() != null && !request.observacaoInicial().isBlank()
                            ? request.observacaoInicial()
                            : OBSERVACAO_PADRAO_CADASTRO
            );
            movimentoEstoqueRepository.save(movimento);
        }

		return toResponse(item, estoque);
    }

    public List<ItemResponse> listar() {
        return itemRepository.findAll()
                .stream()
                .map(item -> toResponse(item, buscarEstoquePorItemId(item.getId())))
                .toList();
    }

    public List<ItemResponse> listarPorTipo(TipoItem tipo) {
        return itemRepository.findByTipo(tipo)
                .stream()
                .map(item -> toResponse(item, buscarEstoquePorItemId(item.getId())))
                .toList();
    }

    public ItemResponse buscarPorId(Long id) {
        Item item = buscarItemPorId(id);
        Estoque estoque = buscarEstoquePorItemId(id);
        return toResponse(item, estoque);
    }

    @Transactional
    public ItemResponse atualizar(Long id, ItemAtualizacaoRequest request) {
        Item item = buscarItemPorId(id);

        if (itemRepository.existsDuplicadoNaAtualizacao(
                id,
                request.tipo(),
                request.descricao(),
                request.unidadeMedida()
        )) {
            throw new RegraNegocioException("Já existe item cadastrado com o mesmo tipo, descrição e unidade de medida");
        }

		item.setNome(request.nome());
		item.setDescricao(request.descricao());
		item.setTipo(request.tipo());
		item.setValorUnitario(request.valorUnitario());
		item.setUnidadeMedida(request.unidadeMedida());
		itemRepository.save(item);

		Estoque estoque = buscarEstoquePorItemId(id);

		return toResponse(item, estoque);
    }

    @Transactional
    public void inativar(Long id) {
        Item item = buscarItemPorId(id);

        Optional<ItemOrdemServico> itemOrdemServico = itemOrdemServicoRepository.findByItem(item);
        if(itemOrdemServico.isPresent()) {
            ItemOrdemServico itemOrdemServicoAtualizado = itemOrdemServico.get();
            OrdemServico ordemServico = itemOrdemServicoAtualizado.getOrdemServico();
            if(StatusOrdemServico.EM_EXECUCAO.equals(ordemServico.getStatus())) throw new RegraNegocioException("Item em uso");
        }

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

    private Estoque buscarEstoquePorItemId(Long itemId) {
        return estoqueRepository.findByItemId(itemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Estoque do item não encontrado"));
    }

    private ItemResponse toResponse(Item item, Estoque estoque) {
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