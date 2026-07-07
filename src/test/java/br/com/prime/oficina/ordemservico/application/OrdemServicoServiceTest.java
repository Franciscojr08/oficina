package br.com.prime.oficina.ordemservico.application;

import br.com.prime.oficina.cliente.domain.Cliente;
import br.com.prime.oficina.cliente.application.gateway.ClienteGateway;
import br.com.prime.oficina.estoque.domain.Estoque;
import br.com.prime.oficina.estoque.application.gateway.EstoqueGateway;
import br.com.prime.oficina.item.domain.Item;
import br.com.prime.oficina.item.application.gateway.ItemGateway;
import br.com.prime.oficina.movimentoestoque.application.gateway.MovimentoEstoqueGateway;
import br.com.prime.oficina.ordemservico.domain.HistoricoOrdemServico;
import br.com.prime.oficina.ordemservico.domain.OrdemServico;
import br.com.prime.oficina.ordemservico.application.gateway.HistoricoOrdemServicoGateway;
import br.com.prime.oficina.ordemservico.application.gateway.OrdemServicoGateway;
import br.com.prime.oficina.ordemservico.itens.application.ItemOrdemServicoRequest;
import br.com.prime.oficina.ordemservico.itens.application.ItemOrdemServicoService;
import br.com.prime.oficina.ordemservico.itens.application.ListaItensOrdemServicoResponse;
import br.com.prime.oficina.ordemservico.itens.domain.ItemOrdemServico;
import br.com.prime.oficina.ordemservico.itens.application.gateway.ItemOrdemServicoGateway;
import br.com.prime.oficina.ordemservico.servicos.application.ListaServicosOrdemServicoResponse;
import br.com.prime.oficina.ordemservico.servicos.application.ServicoOrdemServicoRequest;
import br.com.prime.oficina.ordemservico.servicos.application.ServicoOrdemServicoService;
import br.com.prime.oficina.ordemservico.servicos.application.StatusServico;
import br.com.prime.oficina.ordemservico.servicos.domain.ServicoOrdemServico;
import br.com.prime.oficina.ordemservico.servicos.application.gateway.ServicoOrdemServicoGateway;
import br.com.prime.oficina.servico.domain.Servico;
import br.com.prime.oficina.servico.application.gateway.ServicoGateway;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
import br.com.prime.oficina.veiculo.domain.Veiculo;
import br.com.prime.oficina.veiculo.application.gateway.VeiculoGateway;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static br.com.prime.oficina.shared.exception.ExceptionMessage.SERVICE_ORDER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdemServicoServiceTest {

    @Mock
    private OrdemServicoGateway repository;

    @Mock
    private ClienteGateway clienteRepository;

    @Mock
    private VeiculoGateway veiculoRepository;

    @Mock
    private ItemGateway itemRepository;

    @Mock
    private ServicoGateway servicoRepository;

    @Mock
    private ItemOrdemServicoGateway itemOrdemServicoGateway;

    @Mock
    private ServicoOrdemServicoGateway servicoOrdemServicoGateway;

    @Mock
    private EstoqueGateway estoqueRepository;

    @Mock
    private MovimentoEstoqueGateway movimentoEstoqueGateway;

    @Mock
    private HistoricoOrdemServicoGateway historicoOrdemServicoGateway;

    @Mock
    private EntityManager entityManager;

    private OrdemServicoService ordemServicoService;

    private ItemOrdemServicoService itemOrdemServicoService;

    private ServicoOrdemServicoService servicoOrdemServicoService;

    private OrdemServicoRequest ordemServicoRequest;
    private Cliente clienteAtivo;
    private Veiculo veiculoAtivo;
    private static final LocalDateTime DATE_TIME = LocalDateTime.now();

    @BeforeEach
    void setUp() {
		HistoricoOrdemServicoService historicoOrdemServicoService =
				new HistoricoOrdemServicoService(historicoOrdemServicoGateway);
		OrdemServicoStatusService ordemServicoStatusService =
				new OrdemServicoStatusService(repository, historicoOrdemServicoService);
		OrdemServicoEstoqueService ordemServicoEstoqueService =
				new OrdemServicoEstoqueService(itemOrdemServicoGateway, estoqueRepository, movimentoEstoqueGateway);
		OrdemServicoMapper ordemServicoMapper = new OrdemServicoMapper();

		itemOrdemServicoService = new ItemOrdemServicoService(
				repository,
				itemRepository,
				itemOrdemServicoGateway,
				ordemServicoStatusService
		);
		servicoOrdemServicoService = new ServicoOrdemServicoService(
				repository,
				servicoRepository,
				servicoOrdemServicoGateway,
				ordemServicoStatusService
		);
		ordemServicoService = new OrdemServicoService(
				repository,
				clienteRepository,
				veiculoRepository,
				itemOrdemServicoGateway,
				servicoOrdemServicoGateway,
				itemOrdemServicoService,
				servicoOrdemServicoService,
				historicoOrdemServicoService,
				ordemServicoStatusService,
				ordemServicoEstoqueService,
				ordemServicoMapper
		);
        ReflectionTestUtils.setField(ordemServicoService, "entityManager", entityManager);

		List<Long> servicos = List.of();
		List<ItemOrdemServicoRequest> itens = List.of();

        ordemServicoRequest = new OrdemServicoRequest(
                "Barulho no motor",
                "Cliente relata ruído ao ligar",
                "Diagnóstico inicial",
                1L,
                10L,
				servicos,
				itens
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
        verify(historicoOrdemServicoGateway).save(any(HistoricoOrdemServico.class));
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
        OrdemServico os = criarOrdemServico(100L, StatusOrdemServico.EM_DIAGNOSTICO);

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
        OrdemServico os = criarOrdemServico(100L, StatusOrdemServico.EM_DIAGNOSTICO);

        Item item = criarItem(20L, true, BigDecimal.valueOf(50));
        Estoque estoque = criarEstoque(1L, item, 10, 2);
        item.setEstoque(estoque);

        ItemOrdemServicoRequest request = new ItemOrdemServicoRequest(20L, 3);

        when(repository.findById(100L)).thenReturn(Optional.of(os));
        when(itemRepository.findById(20L)).thenReturn(Optional.of(item));
        when(itemOrdemServicoGateway.save(any(ItemOrdemServico.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(repository.saveAndFlush(any(OrdemServico.class))).thenAnswer(invocation -> {
            OrdemServico ordemServico = invocation.getArgument(0);
            ordemServico.setValorTotalItens(BigDecimal.valueOf(150));
            return ordemServico;
        });

		itemOrdemServicoService.adicionarItem(100L, request);
		ListaItensOrdemServicoResponse response = itemOrdemServicoService.listarItensPorOrdemServico(100L);

        assertEquals(BigDecimal.valueOf(0), response.valorTotalItens());

        ArgumentCaptor<ItemOrdemServico> captor = ArgumentCaptor.forClass(ItemOrdemServico.class);
        verify(itemOrdemServicoGateway).save(captor.capture());

        assertEquals(3, captor.getValue().getQuantidade());
        assertEquals(BigDecimal.valueOf(50), captor.getValue().getValorUnitario());

        verify(repository).saveAndFlush(os);
    }

    @Test
    void naoDeveAdicionarItemQuandoItemEstiverInativo() {
        OrdemServico os = criarOrdemServico(100L, StatusOrdemServico.EM_DIAGNOSTICO);
        Item item = criarItem(20L, false, BigDecimal.valueOf(50));

        when(repository.findById(100L)).thenReturn(Optional.of(os));
        when(itemRepository.findById(20L)).thenReturn(Optional.of(item));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> itemOrdemServicoService.adicionarItem(100L, new ItemOrdemServicoRequest(20L, 2))
        );

        assertEquals("O Item informado não está ativo", exception.getMessage());

        verify(itemOrdemServicoGateway, never()).save(any());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void deveAdicionarServicoESomarValorTotalServicos() {
        OrdemServico os = criarOrdemServico(100L, StatusOrdemServico.EM_DIAGNOSTICO);
        Servico servico = criarServico(30L, true, BigDecimal.valueOf(120));

        when(repository.findById(100L)).thenReturn(Optional.of(os));
        when(servicoRepository.findById(30L)).thenReturn(Optional.of(servico));

        when(servicoOrdemServicoGateway.save(any(ServicoOrdemServico.class)))
                .thenAnswer(invocation -> {
                    ServicoOrdemServico servicoOs = invocation.getArgument(0);

                    os.setValorTotalServicos(BigDecimal.valueOf(120));

                    return servicoOs;
                });

        when(repository.saveAndFlush(any(OrdemServico.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

		servicoOrdemServicoService.adicionarServico(100L, new ServicoOrdemServicoRequest(30L));
		ListaServicosOrdemServicoResponse response = servicoOrdemServicoService.listarServicosPorOrdemServico(100L);

        assertEquals(BigDecimal.valueOf(0), response.valorTotalServicos());

        ArgumentCaptor<ServicoOrdemServico> captor = ArgumentCaptor.forClass(ServicoOrdemServico.class);
        verify(servicoOrdemServicoGateway).save(captor.capture());

        assertEquals(BigDecimal.valueOf(120), captor.getValue().getValorUnitario());
        assertEquals(StatusServico.PENDENTE, captor.getValue().getStatus());

        verify(repository).saveAndFlush(os);
    }

    @Test
    void naoDeveAdicionarServicoQuandoServicoEstiverInativo() {
        OrdemServico os = criarOrdemServico(100L, StatusOrdemServico.EM_DIAGNOSTICO);
        Servico servico = criarServico(30L, false, BigDecimal.valueOf(120));

        when(repository.findById(100L)).thenReturn(Optional.of(os));
        when(servicoRepository.findById(30L)).thenReturn(Optional.of(servico));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> servicoOrdemServicoService.adicionarServico(100L, new ServicoOrdemServicoRequest(30L))
        );

        assertEquals("O Serviço informado não está ativo", exception.getMessage());

        verify(servicoOrdemServicoGateway, never()).save(any());
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
        when(repository.saveAndFlush(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServicoResponse response = ordemServicoService.aprovarOrdemServico(100L);

        assertEquals(StatusOrdemServico.AGUARDANDO_ITENS, response.status());
        assertNotNull(response.dataAprovacao());

        verify(historicoOrdemServicoGateway, times(2)).save(any(HistoricoOrdemServico.class));
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
        when(itemOrdemServicoGateway.findByOrdemServicoId(100L)).thenReturn(List.of(itemOs));
        when(estoqueRepository.baixarEstoque(1L, 3)).thenReturn(0);
        when(estoqueRepository.temEstoqueCompletoParaOrdem(anyLong())).thenReturn(true);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> ordemServicoService.aprovarOrdemServico(100L)
        );

        assertEquals("Estoque insuficiente para o item: Item teste", exception.getMessage());

        verify(estoqueRepository).baixarEstoque(1L, 3);
        verify(movimentoEstoqueGateway, never()).save(any());
        verify(repository, never()).save(any());
        verify(historicoOrdemServicoGateway, times(1)).save(any());
    }

    @Test
    void deveReprovarOrdemServicoECancelarServicos() {
        OrdemServico os = criarOrdemServico(100L, StatusOrdemServico.AGUARDANDO_APROVACAO);

        ServicoOrdemServico servico1 = new ServicoOrdemServico();
        servico1.setStatus(StatusServico.PENDENTE);

        ServicoOrdemServico servico2 = new ServicoOrdemServico();
        servico2.setStatus(StatusServico.INICIADO);

        when(repository.findById(100L)).thenReturn(Optional.of(os));
        when(servicoOrdemServicoGateway.findByOrdemServicoId(100L)).thenReturn(List.of(servico1, servico2));
        when(repository.saveAndFlush(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServicoResponse response = ordemServicoService.reprovarOrdemServico(100L);

        assertEquals(StatusOrdemServico.CANCELADA, response.status());
        assertNotNull(response.dataCancelada());
        assertEquals(StatusServico.CANCELADO, servico1.getStatus());
        assertEquals(StatusServico.CANCELADO, servico2.getStatus());

        verify(servicoOrdemServicoGateway, times(2)).saveAndFlush(any(ServicoOrdemServico.class));
        verify(repository).saveAndFlush(os);
        verify(historicoOrdemServicoGateway).save(any(HistoricoOrdemServico.class));
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
                "Não é possível adicionar o item, pois a ordem de serviço não está Em diagnóstico",
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
                "Não é possível adicionar o serviço, pois a ordem de serviço não está Em diagnóstico",
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

        assertEquals(SERVICE_ORDER_NOT_FOUND, exception.getMessage());

        verify(repository).findById(999L);
    }

    @Test
    void testIniciarDiagnostico() {
        when(repository.findById(100L)).thenReturn(Optional.of(criarOrdemServico(100L, StatusOrdemServico.RECEBIDA)));
        when(repository.saveAndFlush(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(historicoOrdemServicoGateway.save(any(HistoricoOrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var out = ordemServicoService.iniciarDiagnostico(100L);
        var outUpdated = new OrdemServicoResponse(
                out.id(),
                out.codigo(),
                out.descricaoProblema(),
                out.observacoesGerais(),
                out.descricaoServicosExecutados(),
                out.status(),
                out.valorTotalServicos(),
                out.valorTotalItens(),
                DATE_TIME,
                DATE_TIME,
                out.dataAprovacao(),
                out.dataInicioExecucao(),
                out.dataFimExecucao(),
                DATE_TIME,
                out.dataCancelada()
        );

        assertThat(outUpdated).usingRecursiveAssertion().isEqualTo(criarOrdemServicoResponse(StatusOrdemServico.EM_DIAGNOSTICO));
    }

    @Test
    void testSoliticarAprovacao() {
        when(itemOrdemServicoGateway.findByOrdemServicoId(anyLong())).thenReturn(List.of(criarItemServico()));
        when(servicoOrdemServicoGateway.findByOrdemServicoId(anyLong())).thenReturn(List.of(criarServicoOrdemServico()));
        when(repository.findById(100L)).thenReturn(Optional.of(criarOrdemServico(100L, StatusOrdemServico.EM_DIAGNOSTICO)));
        when(repository.saveAndFlush(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(historicoOrdemServicoGateway.save(any(HistoricoOrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));


        var out = ordemServicoService.solicitarAprovacao(100L);
        var outUpdated = new OrdemServicoResponse(
                out.id(),
                out.codigo(),
                out.descricaoProblema(),
                out.observacoesGerais(),
                out.descricaoServicosExecutados(),
                out.status(),
                out.valorTotalServicos(),
                out.valorTotalItens(),
                DATE_TIME,
                DATE_TIME,
                out.dataAprovacao(),
                out.dataInicioExecucao(),
                out.dataFimExecucao(),
                DATE_TIME,
                out.dataCancelada()
        );

        assertThat(outUpdated).usingRecursiveAssertion().isEqualTo(criarOrdemServicoResponse(StatusOrdemServico.AGUARDANDO_APROVACAO));
    }

    @Test
    void testEntregarOrdemServico() {
        when(repository.findById(100L)).thenReturn(Optional.of(criarOrdemServico(100L, StatusOrdemServico.FINALIZADA)));
        when(repository.saveAndFlush(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(historicoOrdemServicoGateway.save(any(HistoricoOrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var out = ordemServicoService.entregarOrdemServico(100L);
        var outUpdated = new OrdemServicoResponse(
                out.id(),
                out.codigo(),
                out.descricaoProblema(),
                out.observacoesGerais(),
                out.descricaoServicosExecutados(),
                out.status(),
                out.valorTotalServicos(),
                out.valorTotalItens(),
                DATE_TIME,
                DATE_TIME,
                out.dataAprovacao(),
                out.dataInicioExecucao(),
                out.dataFimExecucao(),
                DATE_TIME,
                out.dataCancelada()
        );

        assertThat(outUpdated).usingRecursiveAssertion().isEqualTo(criarOrdemServicoResponse(StatusOrdemServico.ENTREGUE));
    }

    @Test
    void testListar() {
        OrdemServico os = criarOrdemServico(100L, StatusOrdemServico.RECEBIDA);
        when(repository.listagemOrdensServico()).thenReturn(List.of(os));

        var out = ordemServicoService.listar();

        assertEquals(1, out.size());
        assertEquals(100L, out.get(0).id());
        assertEquals(StatusOrdemServico.RECEBIDA, out.get(0).status());
    }

    @Test
    void testListarPorCliente() {
        OrdemServico os = criarOrdemServico(100L, StatusOrdemServico.RECEBIDA);
        when(repository.findByClienteId(anyLong())).thenReturn(List.of(os));

        var out = ordemServicoService.listarPorCliente(1L);

        assertThat(out).usingRecursiveComparison().isEqualTo(List.of(os));
    }

    @Test
    void testListarPorCodigo() {
        OrdemServico os = criarOrdemServico(100L, StatusOrdemServico.RECEBIDA);
        when(repository.findByCodigo(anyString())).thenReturn(Optional.of(os));

        var out = ordemServicoService.listarPorCodigo("1L");

        assertThat(out).usingRecursiveComparison().isEqualTo(List.of(os));
    }

    @Test
    void testListarPorStatus() {
        OrdemServico os = criarOrdemServico(100L, StatusOrdemServico.RECEBIDA);
        when(repository.findByStatus(any(StatusOrdemServico.class))).thenReturn(List.of(os));

        var out = ordemServicoService.listarPorStatus(StatusOrdemServico.RECEBIDA);

        assertThat(out).usingRecursiveComparison().isEqualTo(List.of(os));
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

    private OrdemServicoResponse criarOrdemServicoResponse(StatusOrdemServico status) {
        return new OrdemServicoResponse(
                100L,
                "OS-100",
                null,
                null,
                null,
                status,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                DATE_TIME,
                DATE_TIME,
                null,
                null,
                null,
                DATE_TIME,
                null
        );
    }

    private ItemOrdemServico criarItemServico() {
        ItemOrdemServico itemOrdemServico = new ItemOrdemServico();
        Item item = new Item();
        item.setId(1L);
        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setCodigo("OS-1");

        itemOrdemServico.setItem(item);
        itemOrdemServico.setOrdemServico(ordemServico);
        itemOrdemServico.setId(1L);
        itemOrdemServico.setQuantidade(10);
        itemOrdemServico.setValorUnitario(BigDecimal.TEN);

        return itemOrdemServico;
    }

    private ServicoOrdemServico criarServicoOrdemServico() {
        ServicoOrdemServico servicoOS = new ServicoOrdemServico();
        Servico servico = new Servico();
        servico.setId(1L);
        servico.setNome("Servico");
        servico.setValor(BigDecimal.TEN);
        servicoOS.setId(1L);
        servicoOS.setOrdemServico(criarOrdemServico(1L, StatusOrdemServico.RECEBIDA));
        servicoOS.setStatus(StatusServico.PENDENTE);
        servicoOS.setServico(servico);
        servicoOS.setDataCadastro(DATE_TIME);
        servicoOS.setDataInicio(DATE_TIME);
        servicoOS.setDataFim(DATE_TIME);
        servicoOS.setValorUnitario(BigDecimal.TEN);
        return servicoOS;
    }
}
