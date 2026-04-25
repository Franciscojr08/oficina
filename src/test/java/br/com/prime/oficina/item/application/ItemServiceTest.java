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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private EstoqueRepository estoqueRepository;

    @Mock
    private MovimentoEstoqueRepository movimentoEstoqueRepository;

    @Mock
    private ItemOrdemServicoRepository itemOrdemServicoRepository;

    @InjectMocks
    private ItemService itemService;

    private ItemRequest request;

    @BeforeEach
    void setUp() {
        request = new ItemRequest(
                "Óleo 5W30",
                "Óleo lubrificante sintético",
                TipoItem.PECA,
                BigDecimal.valueOf(50.00),
                "UN",
                10,
                2,
                "Entrada inicial"
        );
    }

    @Test
    void deveCriarItemComEstoqueEMovimentoInicialComSucesso() {
        when(itemRepository.existsDuplicado(request.tipo(), request.descricao(), request.unidadeMedida()))
                .thenReturn(false);

        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            item.setId(1L);
            item.setAtivo(true);
            item.setDataCriacao(LocalDateTime.now());
            item.setDataAtualizacao(LocalDateTime.now());
            return item;
        });

        when(estoqueRepository.save(any(Estoque.class))).thenAnswer(invocation -> {
            Estoque estoque = invocation.getArgument(0);
            estoque.setId(1L);
            return estoque;
        });

        ItemResponse response = itemService.criar(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Óleo 5W30", response.nome());
        assertEquals("Óleo lubrificante sintético", response.descricao());
        assertEquals(TipoItem.PECA, response.tipo());
        assertEquals(BigDecimal.valueOf(50.00), response.valorUnitario());
        assertEquals("UN", response.unidadeMedida());
        assertTrue(response.ativo());
        assertEquals(10, response.quantidadeEstoque());
        assertEquals(2, response.estoqueMinimo());

        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        ArgumentCaptor<Estoque> estoqueCaptor = ArgumentCaptor.forClass(Estoque.class);
        ArgumentCaptor<MovimentoEstoque> movimentoCaptor = ArgumentCaptor.forClass(MovimentoEstoque.class);

        verify(itemRepository).save(itemCaptor.capture());
        verify(estoqueRepository).save(estoqueCaptor.capture());
        verify(movimentoEstoqueRepository).save(movimentoCaptor.capture());

        Item itemSalvo = itemCaptor.getValue();
        Estoque estoqueSalvo = estoqueCaptor.getValue();
        MovimentoEstoque movimentoSalvo = movimentoCaptor.getValue();

        assertEquals("Óleo 5W30", itemSalvo.getNome());
        assertEquals("Óleo lubrificante sintético", itemSalvo.getDescricao());
        assertEquals(TipoItem.PECA, itemSalvo.getTipo());
        assertEquals(BigDecimal.valueOf(50.00), itemSalvo.getValorUnitario());
        assertEquals("UN", itemSalvo.getUnidadeMedida());

        assertSame(itemSalvo, estoqueSalvo.getItem());
        assertEquals(10, estoqueSalvo.getQuantidade());
        assertEquals(2, estoqueSalvo.getEstoqueMinimo());

        assertSame(itemSalvo, movimentoSalvo.getItem());
        assertEquals(TipoMovimentoEstoque.ENTRADA, movimentoSalvo.getTipo());
        assertEquals(10, movimentoSalvo.getQuantidade());
        assertEquals("Entrada inicial", movimentoSalvo.getObservacao());
    }

    @Test
    void deveCriarItemComObservacaoPadraoQuandoObservacaoInicialForNula() {
        ItemRequest requestSemObservacao = new ItemRequest(
                "Filtro de óleo",
                "Filtro de óleo motor",
                TipoItem.PECA,
                BigDecimal.valueOf(30.00),
                "UN",
                5,
                1,
                null
        );

        when(itemRepository.existsDuplicado(
                requestSemObservacao.tipo(),
                requestSemObservacao.descricao(),
                requestSemObservacao.unidadeMedida()
        )).thenReturn(false);

        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            item.setId(1L);
            item.setAtivo(true);
            return item;
        });

        when(estoqueRepository.save(any(Estoque.class))).thenAnswer(invocation -> invocation.getArgument(0));

        itemService.criar(requestSemObservacao);

        ArgumentCaptor<MovimentoEstoque> movimentoCaptor = ArgumentCaptor.forClass(MovimentoEstoque.class);
        verify(movimentoEstoqueRepository).save(movimentoCaptor.capture());

        assertEquals("Cadastro inicial do item", movimentoCaptor.getValue().getObservacao());
    }

    @Test
    void deveCriarItemComObservacaoPadraoQuandoObservacaoInicialForVazia() {
        ItemRequest requestObservacaoVazia = new ItemRequest(
                "Filtro de ar",
                "Filtro de ar condicionado",
                TipoItem.PECA,
                BigDecimal.valueOf(40.00),
                "UN",
                3,
                1,
                "   "
        );

        when(itemRepository.existsDuplicado(
                requestObservacaoVazia.tipo(),
                requestObservacaoVazia.descricao(),
                requestObservacaoVazia.unidadeMedida()
        )).thenReturn(false);

        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            item.setId(1L);
            item.setAtivo(true);
            return item;
        });

        when(estoqueRepository.save(any(Estoque.class))).thenAnswer(invocation -> invocation.getArgument(0));

        itemService.criar(requestObservacaoVazia);

        ArgumentCaptor<MovimentoEstoque> movimentoCaptor = ArgumentCaptor.forClass(MovimentoEstoque.class);
        verify(movimentoEstoqueRepository).save(movimentoCaptor.capture());

        assertEquals("Cadastro inicial do item", movimentoCaptor.getValue().getObservacao());
    }

    @Test
    void deveCriarItemSemMovimentoQuandoQuantidadeInicialForZero() {
        ItemRequest requestSemEstoqueInicial = new ItemRequest(
                "Filtro de ar",
                "Filtro de ar condicionado",
                TipoItem.PECA,
                BigDecimal.valueOf(40.00),
                "UN",
                0,
                1,
                "Sem entrada"
        );

        when(itemRepository.existsDuplicado(
                requestSemEstoqueInicial.tipo(),
                requestSemEstoqueInicial.descricao(),
                requestSemEstoqueInicial.unidadeMedida()
        )).thenReturn(false);

        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            item.setId(1L);
            item.setAtivo(true);
            return item;
        });

        when(estoqueRepository.save(any(Estoque.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemResponse response = itemService.criar(requestSemEstoqueInicial);

        assertEquals(0, response.quantidadeEstoque());

        verify(itemRepository).save(any(Item.class));
        verify(estoqueRepository).save(any(Estoque.class));
        verify(movimentoEstoqueRepository, never()).save(any());
    }

    @Test
    void naoDeveCriarItemDuplicado() {
        when(itemRepository.existsDuplicado(request.tipo(), request.descricao(), request.unidadeMedida()))
                .thenReturn(true);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> itemService.criar(request)
        );

        assertEquals("Já existe item cadastrado com o mesmo tipo, descrição e unidade de medida", exception.getMessage());

        verify(itemRepository).existsDuplicado(request.tipo(), request.descricao(), request.unidadeMedida());
        verify(itemRepository, never()).save(any());
        verify(estoqueRepository, never()).save(any());
        verify(movimentoEstoqueRepository, never()).save(any());
    }

    @Test
    void deveListarItens() {
        Item item1 = criarItem(1L, "Óleo 5W30", TipoItem.PECA);
        Item item2 = criarItem(2L, "Aditivo", TipoItem.INSUMO);

        Estoque estoque1 = criarEstoque(1L, item1, 10, 2);
        Estoque estoque2 = criarEstoque(2L, item2, 20, 5);

        when(itemRepository.findAll()).thenReturn(List.of(item1, item2));
        when(estoqueRepository.findByItemId(1L)).thenReturn(Optional.of(estoque1));
        when(estoqueRepository.findByItemId(2L)).thenReturn(Optional.of(estoque2));

        List<ItemResponse> responses = itemService.listar();

        assertEquals(2, responses.size());
        assertEquals("Óleo 5W30", responses.get(0).nome());
        assertEquals(10, responses.get(0).quantidadeEstoque());
        assertEquals("Aditivo", responses.get(1).nome());
        assertEquals(20, responses.get(1).quantidadeEstoque());

        verify(itemRepository).findAll();
        verify(estoqueRepository).findByItemId(1L);
        verify(estoqueRepository).findByItemId(2L);
    }

    @Test
    void deveListarItensPorTipo() {
        Item item1 = criarItem(1L, "Óleo 5W30", TipoItem.PECA);
        Item item2 = criarItem(2L, "Filtro de óleo", TipoItem.PECA);

        Estoque estoque1 = criarEstoque(1L, item1, 10, 2);
        Estoque estoque2 = criarEstoque(2L, item2, 5, 1);

        when(itemRepository.findByTipo(TipoItem.PECA)).thenReturn(List.of(item1, item2));
        when(estoqueRepository.findByItemId(1L)).thenReturn(Optional.of(estoque1));
        when(estoqueRepository.findByItemId(2L)).thenReturn(Optional.of(estoque2));

        List<ItemResponse> responses = itemService.listarPorTipo(TipoItem.PECA);

        assertEquals(2, responses.size());
        assertEquals(TipoItem.PECA, responses.get(0).tipo());
        assertEquals(TipoItem.PECA, responses.get(1).tipo());

        verify(itemRepository).findByTipo(TipoItem.PECA);
        verify(estoqueRepository).findByItemId(1L);
        verify(estoqueRepository).findByItemId(2L);
    }

    @Test
    void deveBuscarItemPorIdComSucesso() {
        Item item = criarItem(1L, "Óleo 5W30", TipoItem.PECA);
        Estoque estoque = criarEstoque(1L, item, 10, 2);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(estoqueRepository.findByItemId(1L)).thenReturn(Optional.of(estoque));

        ItemResponse response = itemService.buscarPorId(1L);

        assertEquals(1L, response.id());
        assertEquals("Óleo 5W30", response.nome());
        assertEquals(10, response.quantidadeEstoque());
        assertEquals(2, response.estoqueMinimo());

        verify(itemRepository).findById(1L);
        verify(estoqueRepository).findByItemId(1L);
    }

    @Test
    void deveLancarExcecaoAoBuscarItemInexistente() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> itemService.buscarPorId(99L)
        );

        assertEquals("Item não encontrado", exception.getMessage());

        verify(itemRepository).findById(99L);
        verify(estoqueRepository, never()).findByItemId(anyLong());
    }

    @Test
    void deveLancarExcecaoQuandoEstoqueDoItemNaoForEncontrado() {
        Item item = criarItem(1L, "Óleo 5W30", TipoItem.PECA);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(estoqueRepository.findByItemId(1L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> itemService.buscarPorId(1L)
        );

        assertEquals("Estoque do item não encontrado", exception.getMessage());

        verify(itemRepository).findById(1L);
        verify(estoqueRepository).findByItemId(1L);
    }

    @Test
    void deveAtualizarItemComSucesso() {
        Item item = criarItem(1L, "Óleo antigo", TipoItem.PECA);
        Estoque estoque = criarEstoque(1L, item, 10, 2);

        ItemAtualizacaoRequest requestAtualizacao = new ItemAtualizacaoRequest(
                "Óleo 5W30 Atualizado",
                "Óleo sintético atualizado",
                TipoItem.PECA,
                BigDecimal.valueOf(60.00),
                "UN"
        );

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.existsDuplicadoNaAtualizacao(
                1L,
                requestAtualizacao.tipo(),
                requestAtualizacao.descricao(),
                requestAtualizacao.unidadeMedida()
        )).thenReturn(false);
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(estoqueRepository.findByItemId(1L)).thenReturn(Optional.of(estoque));

        ItemResponse response = itemService.atualizar(1L, requestAtualizacao);

        assertEquals("Óleo 5W30 Atualizado", response.nome());
        assertEquals("Óleo sintético atualizado", response.descricao());
        assertEquals(BigDecimal.valueOf(60.00), response.valorUnitario());
        assertEquals("UN", response.unidadeMedida());
        assertEquals(10, response.quantidadeEstoque());

        verify(itemRepository).findById(1L);
        verify(itemRepository).existsDuplicadoNaAtualizacao(
                1L,
                requestAtualizacao.tipo(),
                requestAtualizacao.descricao(),
                requestAtualizacao.unidadeMedida()
        );
        verify(itemRepository).save(item);
        verify(estoqueRepository).findByItemId(1L);
    }

    @Test
    void naoDeveAtualizarItemQuandoHouverDuplicidade() {
        Item item = criarItem(1L, "Óleo antigo", TipoItem.PECA);

        ItemAtualizacaoRequest requestAtualizacao = new ItemAtualizacaoRequest(
                "Óleo 5W30 Atualizado",
                "Óleo sintético atualizado",
                TipoItem.PECA,
                BigDecimal.valueOf(60.00),
                "UN"
        );

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.existsDuplicadoNaAtualizacao(
                1L,
                requestAtualizacao.tipo(),
                requestAtualizacao.descricao(),
                requestAtualizacao.unidadeMedida()
        )).thenReturn(true);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> itemService.atualizar(1L, requestAtualizacao)
        );

        assertEquals("Já existe item cadastrado com o mesmo tipo, descrição e unidade de medida", exception.getMessage());

        verify(itemRepository).findById(1L);
        verify(itemRepository).existsDuplicadoNaAtualizacao(
                1L,
                requestAtualizacao.tipo(),
                requestAtualizacao.descricao(),
                requestAtualizacao.unidadeMedida()
        );
        verify(itemRepository, never()).save(any());
        verify(estoqueRepository, never()).findByItemId(anyLong());
    }

    @Test
    void deveLancarExcecaoAoAtualizarItemInexistente() {
        ItemAtualizacaoRequest requestAtualizacao = new ItemAtualizacaoRequest(
                "Óleo 5W30 Atualizado",
                "Óleo sintético atualizado",
                TipoItem.PECA,
                BigDecimal.valueOf(60.00),
                "UN"
        );

        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> itemService.atualizar(99L, requestAtualizacao)
        );

        assertEquals("Item não encontrado", exception.getMessage());

        verify(itemRepository).findById(99L);
        verify(itemRepository, never()).existsDuplicadoNaAtualizacao(anyLong(), any(), anyString(), anyString());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void deveInativarItemQuandoNaoEstaEmUso() {
        Item item = criarItem(1L, "Óleo 5W30", TipoItem.PECA);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemOrdemServicoRepository.findByItem(item)).thenReturn(Optional.empty());

        itemService.inativar(1L);

        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(itemCaptor.capture());

        assertFalse(itemCaptor.getValue().getAtivo());

        verify(itemRepository).findById(1L);
        verify(itemOrdemServicoRepository).findByItem(item);
    }

    @Test
    void naoDeveInativarItemQuandoEstaEmUsoEmOrdemServicoEmExecucao() {
        Item item = criarItem(1L, "Óleo 5W30", TipoItem.PECA);

        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setStatus(StatusOrdemServico.EM_EXECUCAO);

        ItemOrdemServico itemOrdemServico = new ItemOrdemServico();
        itemOrdemServico.setItem(item);
        itemOrdemServico.setOrdemServico(ordemServico);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemOrdemServicoRepository.findByItem(item)).thenReturn(Optional.of(itemOrdemServico));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> itemService.inativar(1L)
        );

        assertEquals("Item em uso", exception.getMessage());

        verify(itemRepository).findById(1L);
        verify(itemOrdemServicoRepository).findByItem(item);
        verify(itemRepository, never()).save(any());
    }

    @Test
    void deveInativarItemQuandoEstaEmOrdemServicoMasNaoEmExecucao() {
        Item item = criarItem(1L, "Óleo 5W30", TipoItem.PECA);

        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setStatus(StatusOrdemServico.RECEBIDA);

        ItemOrdemServico itemOrdemServico = new ItemOrdemServico();
        itemOrdemServico.setItem(item);
        itemOrdemServico.setOrdemServico(ordemServico);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemOrdemServicoRepository.findByItem(item)).thenReturn(Optional.of(itemOrdemServico));

        itemService.inativar(1L);

        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(itemCaptor.capture());

        assertFalse(itemCaptor.getValue().getAtivo());

        verify(itemRepository).findById(1L);
        verify(itemOrdemServicoRepository).findByItem(item);
    }

    @Test
    void deveLancarExcecaoAoInativarItemInexistente() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> itemService.inativar(99L)
        );

        assertEquals("Item não encontrado", exception.getMessage());

        verify(itemRepository).findById(99L);
        verify(itemOrdemServicoRepository, never()).findByItem(any());
        verify(itemRepository, never()).save(any());
    }

    private Item criarItem(Long id, String nome, TipoItem tipo) {
        Item item = new Item();
        item.setId(id);
        item.setNome(nome);
        item.setDescricao(nome + " descrição");
        item.setTipo(tipo);
        item.setValorUnitario(BigDecimal.valueOf(50.00));
        item.setUnidadeMedida("UN");
        item.setAtivo(true);
        item.setDataCriacao(LocalDateTime.now());
        item.setDataAtualizacao(LocalDateTime.now());
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