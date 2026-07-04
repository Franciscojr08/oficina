package br.com.prime.oficina.servico.application;

import br.com.prime.oficina.ordemservico.servicos.infrastructure.ServicoOrdemServicoRepository;
import br.com.prime.oficina.servico.domain.Servico;
import br.com.prime.oficina.servico.infrastructure.ServicoRepository;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static br.com.prime.oficina.shared.exception.ExceptionMessage.SERVICE_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private ServicoOrdemServicoRepository servicoOrdemServicoRepository;

    @Spy
    private ServicoMapper servicoMapper;

    @InjectMocks
    private ServicoService servicoService;

    private ServicoRequest request;

    @BeforeEach
    void setUp() {
        request = new ServicoRequest(
                "Troca de óleo",
                "Troca completa do óleo do motor",
                BigDecimal.valueOf(120.00)
        );
    }

    @Test
    void deveCriarServicoComSucesso() {
        when(servicoRepository.existsByNomeIgnoreCase("Troca de óleo")).thenReturn(false);
        when(servicoRepository.save(any(Servico.class))).thenAnswer(invocation -> {
            Servico servico = invocation.getArgument(0);
            servico.setId(1L);
            servico.setAtivo(true);
            servico.setDataCriacao(LocalDateTime.now());
            servico.setDataAtualizacao(LocalDateTime.now());
            return servico;
        });

        ServicoResponse response = servicoService.criar(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Troca de óleo", response.nome());
        assertEquals("Troca completa do óleo do motor", response.descricao());
        assertEquals(BigDecimal.valueOf(120.00), response.valor());
        assertTrue(response.ativo());

        verify(servicoRepository).existsByNomeIgnoreCase("Troca de óleo");
        verify(servicoRepository).save(any(Servico.class));
    }

    @Test
    void naoDeveCriarServicoQuandoNomeJaExiste() {
        when(servicoRepository.existsByNomeIgnoreCase("Troca de óleo")).thenReturn(true);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> servicoService.criar(request)
        );

        assertEquals("Já existe serviço cadastrado com este nome", exception.getMessage());

        verify(servicoRepository).existsByNomeIgnoreCase("Troca de óleo");
        verify(servicoRepository, never()).save(any());
    }

    @Test
    void deveListarServicos() {
        Servico servico1 = criarServico(1L, "Troca de óleo", BigDecimal.valueOf(120.00));
        Servico servico2 = criarServico(2L, "Alinhamento", BigDecimal.valueOf(80.00));

        when(servicoRepository.findAll()).thenReturn(List.of(servico1, servico2));

        List<ServicoResponse> responses = servicoService.listar();

        assertEquals(2, responses.size());
        assertEquals("Troca de óleo", responses.get(0).nome());
        assertEquals("Alinhamento", responses.get(1).nome());

        verify(servicoRepository).findAll();
    }

    @Test
    void deveBuscarServicoPorIdComSucesso() {
        Servico servico = criarServico(1L, "Troca de óleo", BigDecimal.valueOf(120.00));

        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));

        ServicoResponse response = servicoService.buscarPorId(1L);

        assertEquals(1L, response.id());
        assertEquals("Troca de óleo", response.nome());
        assertEquals(BigDecimal.valueOf(120.00), response.valor());

        verify(servicoRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoAoBuscarServicoPorIdInexistente() {
        when(servicoRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> servicoService.buscarPorId(99L)
        );

        assertEquals(SERVICE_NOT_FOUND, exception.getMessage());

        verify(servicoRepository).findById(99L);
    }

    @Test
    void deveAtualizarServicoComSucessoMantendoMesmoNome() {
        Servico servico = criarServico(1L, "Troca de óleo", BigDecimal.valueOf(120.00));

        ServicoRequest requestAtualizacao = new ServicoRequest(
                "Troca de óleo",
                "Troca de óleo atualizada",
                BigDecimal.valueOf(150.00)
        );

        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(servicoRepository.saveAndFlush(any(Servico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServicoResponse response = servicoService.atualizar(1L, requestAtualizacao);

        assertEquals("Troca de óleo", response.nome());
        assertEquals("Troca de óleo atualizada", response.descricao());
        assertEquals(BigDecimal.valueOf(150.00), response.valor());

        verify(servicoRepository).findById(1L);
        verify(servicoRepository, never()).existsByNomeIgnoreCase(anyString());
        verify(servicoRepository).saveAndFlush(servico);
    }

    @Test
    void deveAtualizarServicoComNovoNomeQuandoNaoHouverDuplicidade() {
        Servico servico = criarServico(1L, "Troca de óleo", BigDecimal.valueOf(120.00));

        ServicoRequest requestAtualizacao = new ServicoRequest(
                "Troca de filtro",
                "Troca de filtro do motor",
                BigDecimal.valueOf(90.00)
        );

        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(servicoRepository.existsByNomeIgnoreCase("Troca de filtro")).thenReturn(false);
        when(servicoRepository.saveAndFlush(any(Servico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServicoResponse response = servicoService.atualizar(1L, requestAtualizacao);

        assertEquals("Troca de filtro", response.nome());
        assertEquals("Troca de filtro do motor", response.descricao());
        assertEquals(BigDecimal.valueOf(90.00), response.valor());

        verify(servicoRepository).findById(1L);
        verify(servicoRepository).existsByNomeIgnoreCase("Troca de filtro");
        verify(servicoRepository).saveAndFlush(servico);
    }

    @Test
    void naoDeveAtualizarServicoQuandoNovoNomeJaExiste() {
        Servico servico = criarServico(1L, "Troca de óleo", BigDecimal.valueOf(120.00));

        ServicoRequest requestAtualizacao = new ServicoRequest(
                "Alinhamento",
                "Alinhamento atualizado",
                BigDecimal.valueOf(100.00)
        );

        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(servicoRepository.existsByNomeIgnoreCase("Alinhamento")).thenReturn(true);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> servicoService.atualizar(1L, requestAtualizacao)
        );

        assertEquals("Já existe serviço cadastrado com este nome", exception.getMessage());

        verify(servicoRepository).findById(1L);
        verify(servicoRepository).existsByNomeIgnoreCase("Alinhamento");
        verify(servicoRepository, never()).saveAndFlush(any());
    }

    @Test
    void deveLancarExcecaoAoAtualizarServicoInexistente() {
        when(servicoRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> servicoService.atualizar(99L, request)
        );

        assertEquals(SERVICE_NOT_FOUND, exception.getMessage());

        verify(servicoRepository).findById(99L);
        verify(servicoRepository, never()).existsByNomeIgnoreCase(anyString());
        verify(servicoRepository, never()).saveAndFlush(any());
    }

    @Test
    void deveInativarServicoQuandoNaoPossuiVinculo() {
        Servico servico = criarServico(1L, "Troca de óleo", BigDecimal.valueOf(120.00));

        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));

        servicoService.inativar(1L);

        ArgumentCaptor<Servico> captor = ArgumentCaptor.forClass(Servico.class);
        verify(servicoRepository).save(captor.capture());

        Servico servicoSalvo = captor.getValue();
        assertFalse(servicoSalvo.getAtivo());

        verify(servicoRepository).findById(1L);
        verify(servicoOrdemServicoRepository, never()).findByServicoId(anyLong());
    }

    @Test
    void deveLancarExcecaoAoInativarServicoInexistente() {
        when(servicoRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> servicoService.inativar(99L)
        );

        assertEquals(SERVICE_NOT_FOUND, exception.getMessage());

        verify(servicoRepository).findById(99L);
        verify(servicoOrdemServicoRepository, never()).findByServicoId(anyLong());
        verify(servicoRepository, never()).save(any());
    }

    private Servico criarServico(Long id, String nome, BigDecimal valor) {
        Servico servico = new Servico();
        servico.setId(id);
        servico.setNome(nome);
        servico.setDescricao(nome + " descrição");
        servico.setValor(valor);
        servico.setAtivo(true);
        servico.setDataCriacao(LocalDateTime.now());
        servico.setDataAtualizacao(LocalDateTime.now());
        return servico;
    }
}
