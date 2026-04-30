package br.com.prime.oficina.ordemservico.application;

import br.com.prime.oficina.cliente.domain.Cliente;
import br.com.prime.oficina.cliente.infraestructure.ClienteRepository;
import br.com.prime.oficina.estoque.domain.Estoque;
import br.com.prime.oficina.estoque.infrastructure.EstoqueRepository;
import br.com.prime.oficina.item.domain.Item;
import br.com.prime.oficina.item.infrastructure.ItemRepository;
import br.com.prime.oficina.movimentoEstoque.domain.MovimentoEstoque;
import br.com.prime.oficina.movimentoEstoque.domain.TipoMovimentoEstoque;
import br.com.prime.oficina.movimentoEstoque.infrastructure.MovimentoEstoqueRepository;
import br.com.prime.oficina.ordemservico.domain.HistoricoOrdemServico;
import br.com.prime.oficina.ordemservico.domain.OrdemServico;
import br.com.prime.oficina.ordemservico.infrastructure.HistoricoOrdemServicoRepository;
import br.com.prime.oficina.ordemservico.infrastructure.OrdemServicoRepository;
import br.com.prime.oficina.ordemservico.itens.application.ItemOrdemServicoRequest;
import br.com.prime.oficina.ordemservico.itens.application.ItemOrdemServicoService;
import br.com.prime.oficina.ordemservico.itens.application.ListaItensOrdemServicoResponse;
import br.com.prime.oficina.ordemservico.itens.domain.ItemOrdemServico;
import br.com.prime.oficina.ordemservico.itens.infrastructure.ItemOrdemServicoRepository;
import br.com.prime.oficina.ordemservico.servicos.application.ListaServicosOrdemServicoResponse;
import br.com.prime.oficina.ordemservico.servicos.application.ServicoOrdemServicoRequest;
import br.com.prime.oficina.ordemservico.servicos.application.ServicoOrdemServicoService;
import br.com.prime.oficina.ordemservico.servicos.application.StatusServico;
import br.com.prime.oficina.ordemservico.servicos.domain.ServicoOrdemServico;
import br.com.prime.oficina.ordemservico.servicos.infrastructure.ServicoOrdemServicoRepository;
import br.com.prime.oficina.ordemservico.itens.application.ItemOrdemServicoRequest;
import br.com.prime.oficina.servico.domain.Servico;
import br.com.prime.oficina.servico.infrasctucture.ServicoRepository;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
import br.com.prime.oficina.veiculo.domain.Veiculo;
import br.com.prime.oficina.veiculo.infrastructure.VeiculoRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdemServicoServiceTest {

    @Mock
    private OrdemServicoRepository repository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private ItemOrdemServicoRepository itemOrdemServicoRepository;

    @Mock
    private ServicoOrdemServicoRepository servicoOrdemServicoRepository;

    @Mock
    private EstoqueRepository estoqueRepository;

    @Mock
    private MovimentoEstoqueRepository movimentoEstoqueRepository;

    @Mock
    private HistoricoOrdemServicoRepository historicoOrdemServicoRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private OrdemServicoService ordemServicoService;
	private ItemOrdemServicoService itemOrdemServicoService;
	private ServicoOrdemServicoService servicoOrdemServicoService;

    private OrdemServicoRequest ordemServicoRequest;
    private Cliente clienteAtivo;
    private Veiculo veiculoAtivo;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(ordemServicoService, "entityManager", entityManager);

        ordemServicoRequest = new OrdemServicoRequest(
                "Barulho no motor",
                "Cliente relata ruído ao ligar",
                "Diagnóstico inicial",
                1L,
                10L
        );

        clienteAtivo = criarCliente(1L, true);
        veiculoAtivo = criarVeiculo(10L, true, clienteAtivo);
    }

    @Test
    void deveCriarOrdemServicoComSucesso() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAtivo));
        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(veiculoAtivo));
        when(repository.saveAndFlush(any(OrdemServico.class))).thenAnswer(invocation -> {
            OrdemServico os = invocation.getArgument(0);
            os.setId(100L);
            os.setCodigo("OS-100");
            os.setDataCadastro(LocalDateTime.now());
            return os;
        });

        OrdemServicoResponse response = ordemServicoService.criar(ordemServicoRequest);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals("OS-100", response.codigo());
        assertEquals(StatusOrdemServico.RECEBIDA, response.status());
        assertEquals(BigDecimal.ZERO, response.valorTotalItens());
        assertEquals(BigDecimal.ZERO, response.valorTotalServicos());
        assertEquals("Barulho no motor", response.descricaoProblema());

        verify(clienteRepository).findById(1L);
        verify(veiculoRepository).findById(10L);
        verify(repository).saveAndFlush(any(OrdemServico.class));
        verify(entityManager).refresh(any(OrdemServico.class));
        verify(historicoOrdemServicoRepository).save(any(HistoricoOrdemServico.class));
    }

    @Test
    void naoDeveCriarOrdemServicoQuandoClienteEstiverInativo() {
        Cliente clienteInativo = criarCliente(1L, false);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteInativo));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> ordemServicoService.criar(ordemServicoRequest)
        );

        assertEquals("O cliente informado não está ativo", exception.getMessage());

        verify(clienteRepository).findById(1L);
        verify(veiculoRepository, never()).findById(anyLong());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void naoDeveCriarOrdemServicoQuandoVeiculoEstiverInativo() {
        Veiculo veiculoInativo = criarVeiculo(10L, false, clienteAtivo);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAtivo));
        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(veiculoInativo));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> ordemServicoService.criar(ordemServicoRequest)
        );

        assertEquals("O Veículo informado não está ativo", exception.getMessage());

        verify(clienteRepository).findById(1L);
        verify(veiculoRepository).findById(10L);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void deveAtualizarOrdemServicoComSucesso() {
        OrdemServico os = criarOrdemServico(100L, StatusOrdemServico.RECEBIDA);

        when(repository.findById(100L)).thenReturn(Optional.of(os));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAtivo));
        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(veiculoAtivo));
        when(repository.saveAndFlush(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServicoResponse response = ordemServicoService.atualizar(100L, ordemServicoRequest);

        assertEquals("Barulho no motor", response.descricaoProblema());
        assertEquals("Cliente relata ruído ao ligar", response.observacoesGerais());
        assertEquals("Diagnóstico inicial", response.descricaoServicosExecutados());

        verify(repository).findById(100L);
        verify(clienteRepository).findById(1L);
        verify(veiculoRepository).findById(10L);
        verify(repository).saveAndFlush(os);
    }

    @Test
    void deveAdicionarItemESomarValorTotalItens() {
        OrdemServico os = criarOrdemServico(100L, StatusOrdemServico.RECEBIDA);

        Item item = criarItem(20L, true, BigDecimal.valueOf(50));
        Estoque estoque = criarEstoque(1L, item, 10, 2);
        item.setEstoque(estoque);

        ItemOrdemServicoRequest request = new ItemOrdemServicoRequest(20L, 3);

        when(repository.findById(100L)).thenReturn(Optional.of(os));
        when(itemRepository.findById(20L)).thenReturn(Optional.of(item));
        when(itemOrdemServicoRepository.save(any(ItemOrdemServico.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(repository.saveAndFlush(any(OrdemServico.class))).thenAnswer(invocation -> {
            OrdemServico ordemServico = invocation.getArgument(0);
            ordemServico.setValorTotalItens(BigDecimal.valueOf(150));
            return ordemServico;
        });

        ListaItensOrdemServicoResponse response = itemOrdemServicoService.adicionarItem(100L, request);

        assertEquals(BigDecimal.valueOf(0), response.valorTotalItens());

        ArgumentCaptor<ItemOrdemServico> captor = ArgumentCaptor.forClass(ItemOrdemServico.class);
        verify(itemOrdemServicoRepository).save(captor.capture());

        assertEquals(3, captor.getValue().getQuantidade());
        assertEquals(BigDecimal.valueOf(50), captor.getValue().getValorUnitario());

        verify(repository).saveAndFlush(os);
    }

    @Test
    void naoDeveAdicionarItemQuandoItemEstiverInativo() {
        OrdemServico os = criarOrdemServico(100L, StatusOrdemServico.RECEBIDA);
        Item item = criarItem(20L, false, BigDecimal.valueOf(50));

        when(repository.findById(100L)).thenReturn(Optional.of(os));
        when(itemRepository.findById(20L)).thenReturn(Optional.of(item));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> itemOrdemServicoService.adicionarItem(100L, new ItemOrdemServicoRequest(20L, 2))
        );

        assertEquals("O Item informado não está ativo", exception.getMessage());

        verify(itemOrdemServicoRepository, never()).save(any());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void deveAdicionarServicoESomarValorTotalServicos() {
        OrdemServico os = criarOrdemServico(100L, StatusOrdemServico.RECEBIDA);
        Servico servico = criarServico(30L, true, BigDecimal.valueOf(120));

        when(repository.findById(100L)).thenReturn(Optional.of(os));
        when(servicoRepository.findById(30L)).thenReturn(Optional.of(servico));

        when(servicoOrdemServicoRepository.save(any(ServicoOrdemServico.class)))
                .thenAnswer(invocation -> {
                    ServicoOrdemServico servicoOs = invocation.getArgument(0);

                    os.setValorTotalServicos(BigDecimal.valueOf(120));

                    return servicoOs;
                });

        when(repository.saveAndFlush(any(OrdemServico.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ListaServicosOrdemServicoResponse response =
                servicoOrdemServicoService.adicionarServico(100L, new ServicoOrdemServicoRequest(30L));

        assertEquals(BigDecimal.valueOf(0), response.valorTotalServicos());

        ArgumentCaptor<ServicoOrdemServico> captor = ArgumentCaptor.forClass(ServicoOrdemServico.class);
        verify(servicoOrdemServicoRepository).save(captor.capture());

        assertEquals(BigDecimal.valueOf(120), captor.getValue().getValorUnitario());
        assertEquals(StatusServico.PENDENTE, captor.getValue().getStatus());

        verify(repository).saveAndFlush(os);
    }

    @Test
    void naoDeveAdicionarServicoQuandoServicoEstiverInativo() {
        OrdemServico os = criarOrdemServico(100L, StatusOrdemServico.RECEBIDA);
        Servico servico = criarServico(30L, false, BigDecimal.valueOf(120));

        when(repository.findById(100L)).thenReturn(Optional.of(os));
        when(servicoRepository.findById(30L)).thenReturn(Optional.of(servico));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> servicoOrdemServicoService.adicionarServico(100L, new ServicoOrdemServicoRequest(30L))
        );

        assertEquals("O Serviço informado não está ativo", exception.getMessage());

        verify(servicoOrdemServicoRepository, never()).save(any());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void deveAprovarOrdemServicoComBaixaDeEstoqueEGerarMovimento() {
        OrdemServico os = criarOrdemServico(100L, StatusOrdemServico.AGUARDANDO_APROVACAO);
        Item item = criarItem(20L, true, BigDecimal.valueOf(50));
        Estoque estoque = criarEstoque(1L, item, 10, 2);
        item.setEstoque(estoque);

        ItemOrdemServico itemOs = new ItemOrdemServico();
        itemOs.setItem(item);
        itemOs.setQuantidade(3);
        itemOs.setValorUnitario(BigDecimal.valueOf(50));

        when(repository.findById(100L)).thenReturn(Optional.of(os));
        when(itemOrdemServicoRepository.findByOrdemServicoId(100L)).thenReturn(List.of(itemOs));
        when(estoqueRepository.baixarEstoque(1L, 3)).thenReturn(1);
        when(repository.save(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServicoResponse response = ordemServicoService.aprovarOrdemServico(100L);

        assertEquals(StatusOrdemServico.EM_EXECUCAO, response.status());
        assertNotNull(response.dataAprovacao());
        assertNotNull(response.dataInicioExecucao());

        ArgumentCaptor<MovimentoEstoque> movimentoCaptor = ArgumentCaptor.forClass(MovimentoEstoque.class);
        verify(movimentoEstoqueRepository).save(movimentoCaptor.capture());

        MovimentoEstoque movimento = movimentoCaptor.getValue();

        assertEquals(item, movimento.getItem());
        assertEquals(TipoMovimentoEstoque.SAIDA, movimento.getTipo());
        assertEquals(3, movimento.getQuantidade());
        assertEquals(100L, movimento.getOrdemServicoId());
        assertEquals("BAIXA DE ITEM NO ESTOQUE", movimento.getObservacao());

        verify(estoqueRepository).baixarEstoque(1L, 3);
        verify(repository).save(os);
        verify(historicoOrdemServicoRepository).save(any(HistoricoOrdemServico.class));
        verify(itemRepository, never()).saveAndFlush(any());
        verify(estoqueRepository, never()).saveAndFlush(any());
    }

    @Test
    void naoDeveAprovarOrdemServicoQuandoEstoqueForInsuficiente() {
        OrdemServico os = criarOrdemServico(100L, StatusOrdemServico.AGUARDANDO_APROVACAO);
        Item item = criarItem(20L, true, BigDecimal.valueOf(50));
        Estoque estoque = criarEstoque(1L, item, 1, 0);
        item.setEstoque(estoque);

        ItemOrdemServico itemOs = new ItemOrdemServico();
        itemOs.setItem(item);
        itemOs.setQuantidade(3);
        itemOs.setValorUnitario(BigDecimal.valueOf(50));

        when(repository.findById(100L)).thenReturn(Optional.of(os));
        when(itemOrdemServicoRepository.findByOrdemServicoId(100L)).thenReturn(List.of(itemOs));
        when(estoqueRepository.baixarEstoque(1L, 3)).thenReturn(0);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> ordemServicoService.aprovarOrdemServico(100L)
        );

        assertEquals("Estoque insuficiente para o item: Item teste", exception.getMessage());

        verify(estoqueRepository).baixarEstoque(1L, 3);
        verify(movimentoEstoqueRepository, never()).save(any());
        verify(repository, never()).save(any());
        verify(historicoOrdemServicoRepository, never()).save(any());
    }

    @Test
    void deveReprovarOrdemServicoECancelarServicos() {
        OrdemServico os = criarOrdemServico(100L, StatusOrdemServico.AGUARDANDO_APROVACAO);

        ServicoOrdemServico servico1 = new ServicoOrdemServico();
        servico1.setStatus(StatusServico.PENDENTE);

        ServicoOrdemServico servico2 = new ServicoOrdemServico();
        servico2.setStatus(StatusServico.INICIADO);

        when(repository.findById(100L)).thenReturn(Optional.of(os));
        when(servicoOrdemServicoRepository.findByOrdemServicoId(100L)).thenReturn(List.of(servico1, servico2));
        when(repository.save(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServicoResponse response = ordemServicoService.reprovarOrdemServico(100L);

        assertEquals(StatusOrdemServico.CANCELADA, response.status());
        assertNotNull(response.dataCancelada());
        assertEquals(StatusServico.CANCELADO, servico1.getStatus());
        assertEquals(StatusServico.CANCELADO, servico2.getStatus());

        verify(servicoOrdemServicoRepository, times(2)).saveAndFlush(any(ServicoOrdemServico.class));
        verify(repository).save(os);
        verify(historicoOrdemServicoRepository).save(any(HistoricoOrdemServico.class));
    }

    @Test
    void naoDeveExecutarOperacoesQuandoOrdemServicoJaEstiverEmExecucao() {
        OrdemServico os = criarOrdemServico(100L, StatusOrdemServico.EM_EXECUCAO);

        when(repository.findById(100L)).thenReturn(Optional.of(os));

        RegraNegocioException exAdicionarItem = assertThrows(
                RegraNegocioException.class,
                () -> itemOrdemServicoService.adicionarItem(100L, new ItemOrdemServicoRequest(20L, 1))
        );

        assertEquals(
                "Não é possível adicionar o item, pois a ordem de serviço está Em execução",
                exAdicionarItem.getMessage()
        );

        RegraNegocioException exAprovar = assertThrows(
                RegraNegocioException.class,
                () -> ordemServicoService.aprovarOrdemServico(100L)
        );

        assertTrue(exAprovar.getMessage().contains("Não é possível aprovar a ordem de serviço"));
    }

    @Test
    void naoDeveExecutarOperacoesQuandoOrdemServicoEstiverCancelada() {
        OrdemServico os = criarOrdemServico(100L, StatusOrdemServico.CANCELADA);

        when(repository.findById(100L)).thenReturn(Optional.of(os));

        RegraNegocioException exAdicionarServico = assertThrows(
                RegraNegocioException.class,
                () -> servicoOrdemServicoService.adicionarServico(100L, new ServicoOrdemServicoRequest(30L))
        );

        assertEquals(
                "Não é possível adicionar o serviço, pois a ordem de serviço está Cancelada",
                exAdicionarServico.getMessage()
        );

        RegraNegocioException exReprovar = assertThrows(
                RegraNegocioException.class,
                () -> ordemServicoService.reprovarOrdemServico(100L)
        );

        assertEquals(
                "Não é possível reprovar a ordem de serviço, pois a ordem de serviço está Cancelada",
                exReprovar.getMessage()
        );
    }

    @Test
    void deveLancarExcecaoQuandoOrdemServicoNaoExistir() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> ordemServicoService.aprovarOrdemServico(999L)
        );

        assertEquals("Ordem de Serviço não encontrada", exception.getMessage());

        verify(repository).findById(999L);
    }

    private Cliente criarCliente(Long id, boolean ativo) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setAtivo(ativo);
        return cliente;
    }

    private Veiculo criarVeiculo(Long id, boolean ativo, Cliente cliente) {
        Veiculo veiculo = new Veiculo();
        veiculo.setId(id);
        veiculo.setAtivo(ativo);
        veiculo.setCliente(cliente);
        return veiculo;
    }

    private Item criarItem(Long id, boolean ativo, BigDecimal valorUnitario) {
        Item item = new Item();
        item.setId(id);
        item.setAtivo(ativo);
        item.setValorUnitario(valorUnitario);
        item.setNome("Item teste");
        return item;
    }

    private Servico criarServico(Long id, boolean ativo, BigDecimal valor) {
        Servico servico = new Servico();
        servico.setId(id);
        servico.setAtivo(ativo);
        servico.setValor(valor);
        servico.setNome("Serviço teste");
        return servico;
    }

    private Estoque criarEstoque(Long id, Item item, int quantidade, int estoqueMinimo) {
        Estoque estoque = new Estoque();
        estoque.setId(id);
        estoque.setItem(item);
        estoque.setQuantidade(quantidade);
        estoque.setEstoqueMinimo(estoqueMinimo);
        return estoque;
    }

    private OrdemServico criarOrdemServico(Long id, StatusOrdemServico status) {
        OrdemServico os = new OrdemServico();
        os.setId(id);
        os.setCodigo("OS-" + id);
        os.setStatus(status);
        os.setValorTotalItens(BigDecimal.ZERO);
        os.setValorTotalServicos(BigDecimal.ZERO);
        os.setDataCadastro(LocalDateTime.now());
        return os;
    }
}