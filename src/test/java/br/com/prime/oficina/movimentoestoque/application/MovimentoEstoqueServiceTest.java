package br.com.prime.oficina.movimentoestoque.application;

import br.com.prime.oficina.item.domain.Item;
import br.com.prime.oficina.item.application.gateway.ItemGateway;
import br.com.prime.oficina.movimentoestoque.domain.MovimentoEstoque;
import br.com.prime.oficina.movimentoestoque.domain.TipoMovimentoEstoque;
import br.com.prime.oficina.movimentoestoque.application.gateway.MovimentoEstoqueGateway;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimentoEstoqueServiceTest {

    @Mock
    private MovimentoEstoqueGateway movimentoEstoqueGateway;

    @Mock
    private ItemGateway itemRepository;

    @InjectMocks
    private MovimentoEstoqueService movimentoEstoqueService;

    @Test
    void deveListarMovimentosPorItem() {
        Item item = criarItem(1L, "Óleo 5W30");

        MovimentoEstoque movimento1 = criarMovimento(
                1L,
                item,
                TipoMovimentoEstoque.ENTRADA,
                10,
                "Entrada inicial",
                null
        );

        MovimentoEstoque movimento2 = criarMovimento(
                2L,
                item,
                TipoMovimentoEstoque.AJUSTE,
                2,
                "Ajuste manual",
                null
        );

        when(itemRepository.existsById(1L)).thenReturn(true);
        when(movimentoEstoqueGateway.findByItemIdOrderByDataMovimentacaoDesc(1L))
                .thenReturn(List.of(movimento1, movimento2));

        List<MovimentoEstoqueResponse> responses = movimentoEstoqueService.listarPorItem(1L);

        assertEquals(2, responses.size());

        assertEquals(1L, responses.get(0).id());
        assertEquals(1L, responses.get(0).itemId());
        assertEquals("Óleo 5W30", responses.get(0).nomeItem());
        assertEquals(TipoMovimentoEstoque.ENTRADA, responses.get(0).tipo());
        assertEquals(10, responses.get(0).quantidade());
        assertEquals("Entrada inicial", responses.get(0).observacao());
        assertNull(responses.get(0).ordemServicoId());

        assertEquals(2L, responses.get(1).id());
        assertEquals(TipoMovimentoEstoque.AJUSTE, responses.get(1).tipo());
        assertEquals(2, responses.get(1).quantidade());
        assertEquals("Ajuste manual", responses.get(1).observacao());

        verify(itemRepository).existsById(1L);
        verify(movimentoEstoqueGateway).findByItemIdOrderByDataMovimentacaoDesc(1L);
    }

    @Test
    void deveLancarExcecaoAoListarMovimentosPorItemQuandoItemNaoExiste() {
        when(itemRepository.existsById(99L)).thenReturn(false);

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> movimentoEstoqueService.listarPorItem(99L)
        );

        assertEquals("Item não encontrado", exception.getMessage());

        verify(itemRepository).existsById(99L);
        verify(movimentoEstoqueGateway, never()).findByItemIdOrderByDataMovimentacaoDesc(anyLong());
    }

    @Test
    void deveListarMovimentosPorItemETipo() {
        Item item = criarItem(1L, "Óleo 5W30");

        MovimentoEstoque movimento = criarMovimento(
                1L,
                item,
                TipoMovimentoEstoque.ENTRADA,
                10,
                "Entrada inicial",
                null
        );

        when(itemRepository.existsById(1L)).thenReturn(true);
        when(movimentoEstoqueGateway.findByItemIdAndTipoOrderByDataMovimentacaoDesc(
                1L, TipoMovimentoEstoque.ENTRADA))
                .thenReturn(List.of(movimento));

        List<MovimentoEstoqueResponse> responses =
                movimentoEstoqueService.listarPorItemETipo(1L, TipoMovimentoEstoque.ENTRADA);

        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).id());
        assertEquals(1L, responses.get(0).itemId());
        assertEquals("Óleo 5W30", responses.get(0).nomeItem());
        assertEquals(TipoMovimentoEstoque.ENTRADA, responses.get(0).tipo());
        assertEquals(10, responses.get(0).quantidade());
        assertEquals("Entrada inicial", responses.get(0).observacao());

        verify(itemRepository).existsById(1L);
        verify(movimentoEstoqueGateway)
                .findByItemIdAndTipoOrderByDataMovimentacaoDesc(1L, TipoMovimentoEstoque.ENTRADA);
    }

    @Test
    void deveLancarExcecaoAoListarMovimentosPorItemETipoQuandoItemNaoExiste() {
        when(itemRepository.existsById(99L)).thenReturn(false);

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> movimentoEstoqueService.listarPorItemETipo(99L, TipoMovimentoEstoque.AJUSTE)
        );

        assertEquals("Item não encontrado", exception.getMessage());

        verify(itemRepository).existsById(99L);
        verify(movimentoEstoqueGateway, never())
                .findByItemIdAndTipoOrderByDataMovimentacaoDesc(anyLong(), any());
    }

    @Test
    void deveListarTodosMovimentos() {
        Item item1 = criarItem(1L, "Óleo 5W30");
        Item item2 = criarItem(2L, "Filtro de óleo");

        MovimentoEstoque movimento1 = criarMovimento(
                1L,
                item1,
                TipoMovimentoEstoque.ENTRADA,
                10,
                "Entrada inicial",
                null
        );

        MovimentoEstoque movimento2 = criarMovimento(
                2L,
                item2,
                TipoMovimentoEstoque.SAIDA,
                1,
                "Consumo na OS",
                100L
        );

        when(movimentoEstoqueGateway.findAllByOrderByDataMovimentacaoDesc())
                .thenReturn(List.of(movimento1, movimento2));

        List<MovimentoEstoqueResponse> responses = movimentoEstoqueService.listarTodos();

        assertEquals(2, responses.size());

        assertEquals(1L, responses.get(0).id());
        assertEquals("Óleo 5W30", responses.get(0).nomeItem());
        assertEquals(TipoMovimentoEstoque.ENTRADA, responses.get(0).tipo());
        assertNull(responses.get(0).ordemServicoId());

        assertEquals(2L, responses.get(1).id());
        assertEquals("Filtro de óleo", responses.get(1).nomeItem());
        assertEquals(TipoMovimentoEstoque.SAIDA, responses.get(1).tipo());
        assertEquals(100L, responses.get(1).ordemServicoId());

        verify(movimentoEstoqueGateway).findAllByOrderByDataMovimentacaoDesc();
        verifyNoInteractions(itemRepository);
    }

    private Item criarItem(Long id, String nome) {
        Item item = new Item();
        item.setId(id);
        item.setNome(nome);
        return item;
    }

    private MovimentoEstoque criarMovimento(
            Long id,
            Item item,
            TipoMovimentoEstoque tipo,
            Integer quantidade,
            String observacao,
            Long ordemServicoId
    ) {
        MovimentoEstoque movimento = new MovimentoEstoque();
        movimento.setId(id);
        movimento.setItem(item);
        movimento.setTipo(tipo);
        movimento.setQuantidade(quantidade);
        movimento.setObservacao(observacao);
        movimento.setOrdemServicoId(ordemServicoId);
        movimento.setDataMovimentacao(LocalDateTime.now());
        return movimento;
    }
}