package br.com.prime.oficina.estoque.application;

import br.com.prime.oficina.estoque.domain.Estoque;
import br.com.prime.oficina.estoque.infrastructure.EstoqueRepository;
import br.com.prime.oficina.item.domain.Item;
import br.com.prime.oficina.item.infrastructure.ItemRepository;
import br.com.prime.oficina.movimentoEstoque.domain.MovimentoEstoque;
import br.com.prime.oficina.movimentoEstoque.domain.TipoMovimentoEstoque;
import br.com.prime.oficina.movimentoEstoque.infrastructure.MovimentoEstoqueRepository;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstoqueServiceTest {

    @Mock
    private EstoqueRepository estoqueRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private MovimentoEstoqueRepository movimentoEstoqueRepository;

    @InjectMocks
    private EstoqueService estoqueService;

    @Test
    void deveListarTodosEstoques() {
        Item item1 = criarItem(1L, "Óleo 5W30");
        Item item2 = criarItem(2L, "Filtro de óleo");

        Estoque estoque1 = criarEstoque(1L, item1, 10, 2);
        Estoque estoque2 = criarEstoque(2L, item2, 5, 1);

        when(estoqueRepository.findAll()).thenReturn(List.of(estoque1, estoque2));

        List<EstoqueResponse> responses = estoqueService.listarTodos();

        assertEquals(2, responses.size());

        assertEquals(1L, responses.get(0).id());
        assertEquals(1L, responses.get(0).itemId());
        assertEquals("Óleo 5W30", responses.get(0).nomeItem());
        assertEquals(10, responses.get(0).quantidade());
        assertEquals(2, responses.get(0).estoqueMinimo());

        assertEquals(2L, responses.get(1).id());
        assertEquals(2L, responses.get(1).itemId());
        assertEquals("Filtro de óleo", responses.get(1).nomeItem());
        assertEquals(5, responses.get(1).quantidade());
        assertEquals(1, responses.get(1).estoqueMinimo());

        verify(estoqueRepository).findAll();
    }

    @Test
    void deveBuscarEstoquePorItemComSucesso() {
        Item item = criarItem(1L, "Óleo 5W30");
        Estoque estoque = criarEstoque(1L, item, 10, 2);

        when(estoqueRepository.findByItemId(1L)).thenReturn(Optional.of(estoque));

        EstoqueResponse response = estoqueService.buscarPorItem(1L);

        assertEquals(1L, response.id());
        assertEquals(1L, response.itemId());
        assertEquals("Óleo 5W30", response.nomeItem());
        assertEquals(10, response.quantidade());
        assertEquals(2, response.estoqueMinimo());

        verify(estoqueRepository).findByItemId(1L);
    }

    @Test
    void deveLancarExcecaoAoBuscarEstoquePorItemQuandoNaoExistir() {
        when(estoqueRepository.findByItemId(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> estoqueService.buscarPorItem(99L)
        );

        assertEquals("Estoque do item não encontrado", exception.getMessage());

        verify(estoqueRepository).findByItemId(99L);
    }

    @Test
    void deveAtualizarEstoquePorItemEGerarMovimentacaoDeAjusteQuandoQuantidadeMudar() {
        Item item = criarItem(1L, "Óleo 5W30");
        Estoque estoque = criarEstoque(1L, item, 10, 2);

        EstoqueRequest request = new EstoqueRequest(15, 3);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(estoqueRepository.findByItemId(1L)).thenReturn(Optional.of(estoque));
        when(estoqueRepository.save(any(Estoque.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EstoqueResponse response = estoqueService.atualizarPorItem(1L, request);

        assertEquals(1L, response.id());
        assertEquals(1L, response.itemId());
        assertEquals("Óleo 5W30", response.nomeItem());
        assertEquals(15, response.quantidade());
        assertEquals(3, response.estoqueMinimo());

        ArgumentCaptor<Estoque> estoqueCaptor = ArgumentCaptor.forClass(Estoque.class);
        verify(estoqueRepository).save(estoqueCaptor.capture());

        Estoque estoqueSalvo = estoqueCaptor.getValue();
        assertEquals(15, estoqueSalvo.getQuantidade());
        assertEquals(3, estoqueSalvo.getEstoqueMinimo());

        ArgumentCaptor<MovimentoEstoque> movimentoCaptor = ArgumentCaptor.forClass(MovimentoEstoque.class);
        verify(movimentoEstoqueRepository).save(movimentoCaptor.capture());

        MovimentoEstoque movimento = movimentoCaptor.getValue();
        assertSame(item, movimento.getItem());
        assertEquals(TipoMovimentoEstoque.AJUSTE, movimento.getTipo());
        assertEquals(5, movimento.getQuantidade());
        assertEquals(
                "Ajuste manual de estoque. Quantidade anterior: 10, nova quantidade: 15",
                movimento.getObservacao()
        );

        verify(itemRepository).findById(1L);
        verify(estoqueRepository).findByItemId(1L);
    }

    @Test
    void deveAtualizarEstoquePorItemSemGerarMovimentacaoQuandoQuantidadeNaoMudar() {
        Item item = criarItem(1L, "Óleo 5W30");
        Estoque estoque = criarEstoque(1L, item, 10, 2);

        EstoqueRequest request = new EstoqueRequest(10, 5);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(estoqueRepository.findByItemId(1L)).thenReturn(Optional.of(estoque));
        when(estoqueRepository.save(any(Estoque.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EstoqueResponse response = estoqueService.atualizarPorItem(1L, request);

        assertEquals(10, response.quantidade());
        assertEquals(5, response.estoqueMinimo());

        verify(itemRepository).findById(1L);
        verify(estoqueRepository).findByItemId(1L);
        verify(estoqueRepository).save(any(Estoque.class));
        verify(movimentoEstoqueRepository, never()).save(any());
    }

    @Test
    void deveGerarMovimentacaoDeAjusteQuandoQuantidadeDiminuir() {
        Item item = criarItem(1L, "Óleo 5W30");
        Estoque estoque = criarEstoque(1L, item, 10, 2);

        EstoqueRequest request = new EstoqueRequest(6, 2);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(estoqueRepository.findByItemId(1L)).thenReturn(Optional.of(estoque));
        when(estoqueRepository.save(any(Estoque.class))).thenAnswer(invocation -> invocation.getArgument(0));

        estoqueService.atualizarPorItem(1L, request);

        ArgumentCaptor<MovimentoEstoque> movimentoCaptor = ArgumentCaptor.forClass(MovimentoEstoque.class);
        verify(movimentoEstoqueRepository).save(movimentoCaptor.capture());

        MovimentoEstoque movimento = movimentoCaptor.getValue();
        assertEquals(TipoMovimentoEstoque.AJUSTE, movimento.getTipo());
        assertEquals(4, movimento.getQuantidade());
        assertEquals(
                "Ajuste manual de estoque. Quantidade anterior: 10, nova quantidade: 6",
                movimento.getObservacao()
        );
    }

    @Test
    void deveLancarExcecaoAoAtualizarEstoqueQuandoItemNaoExistir() {
        EstoqueRequest request = new EstoqueRequest(15, 3);

        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> estoqueService.atualizarPorItem(99L, request)
        );

        assertEquals("Item não encontrado", exception.getMessage());

        verify(itemRepository).findById(99L);
        verify(estoqueRepository, never()).findByItemId(anyLong());
        verify(estoqueRepository, never()).save(any());
        verify(movimentoEstoqueRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoAtualizarEstoqueQuandoEstoqueDoItemNaoExistir() {
        Item item = criarItem(1L, "Óleo 5W30");
        EstoqueRequest request = new EstoqueRequest(15, 3);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(estoqueRepository.findByItemId(1L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> estoqueService.atualizarPorItem(1L, request)
        );

        assertEquals("Estoque do item não encontrado", exception.getMessage());

        verify(itemRepository).findById(1L);
        verify(estoqueRepository).findByItemId(1L);
        verify(estoqueRepository, never()).save(any());
        verify(movimentoEstoqueRepository, never()).save(any());
    }

    private Item criarItem(Long id, String nome) {
        Item item = new Item();
        item.setId(id);
        item.setNome(nome);
        return item;
    }

    private Estoque criarEstoque(Long id, Item item, Integer quantidade, Integer estoqueMinimo) {
        Estoque estoque = new Estoque();
        estoque.setId(id);
        estoque.setItem(item);
        estoque.setQuantidade(quantidade);
        estoque.setEstoqueMinimo(estoqueMinimo);
        estoque.setDataCriacao(LocalDateTime.now());
        estoque.setDataAtualizacao(LocalDateTime.now());
        return estoque;
    }
}