package br.com.prime.oficina.veiculo.application;

import br.com.prime.oficina.cliente.domain.Cliente;
import br.com.prime.oficina.cliente.infraestructure.ClienteRepository;
import br.com.prime.oficina.shared.exception.RecursoDuplicadoException;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
import br.com.prime.oficina.veiculo.domain.Veiculo;
import br.com.prime.oficina.veiculo.infrastructure.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
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
class VeiculoServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private VeiculoService veiculoService;

    private VeiculoRequest request;
    private Cliente clienteAtivo;

    @BeforeEach
    void setUp() {
        request = new VeiculoRequest(
                1L,
                "ABC1234",
                "Toyota",
                "Corolla",
                2020,
                "Prata",
                "Veículo em bom estado"
        );

        clienteAtivo = criarCliente(1L, true);
    }

    @Test
    void deveCriarVeiculoComSucesso() {
        when(veiculoRepository.existsByPlaca("ABC1234")).thenReturn(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAtivo));
        when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(invocation -> {
            Veiculo veiculo = invocation.getArgument(0);
            veiculo.setId(10L);
            veiculo.setAtivo(true);
            veiculo.setDataCriacao(LocalDateTime.now());
            veiculo.setDataAtualizacao(LocalDateTime.now());
            return veiculo;
        });

        VeiculoResponse response = veiculoService.criar(request);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals(1L, response.clienteId());
        assertEquals("ABC1234", response.placa());
        assertEquals("Toyota", response.marca());
        assertEquals("Corolla", response.modelo());
        assertEquals(2020, response.ano());
        assertEquals("Prata", response.cor());
        assertEquals("Veículo em bom estado", response.observacao());
        assertTrue(response.ativo());

        verify(veiculoRepository).existsByPlaca("ABC1234");
        verify(clienteRepository).findById(1L);
        verify(veiculoRepository).save(any(Veiculo.class));
    }

    @Test
    void naoDeveCriarVeiculoQuandoPlacaJaExiste() {
        when(veiculoRepository.existsByPlaca("ABC1234")).thenReturn(true);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> veiculoService.criar(request)
        );

        assertEquals("Já existe veículo cadastrado com esta placa", exception.getMessage());

        verify(veiculoRepository).existsByPlaca("ABC1234");
        verify(clienteRepository, never()).findById(anyLong());
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void naoDeveCriarVeiculoQuandoClienteNaoExiste() {
        when(veiculoRepository.existsByPlaca("ABC1234")).thenReturn(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> veiculoService.criar(request)
        );

        assertEquals("Cliente não encontrado", exception.getMessage());

        verify(veiculoRepository).existsByPlaca("ABC1234");
        verify(clienteRepository).findById(1L);
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void naoDeveCriarVeiculoQuandoClienteEstaInativo() {
        Cliente clienteInativo = criarCliente(1L, false);

        when(veiculoRepository.existsByPlaca("ABC1234")).thenReturn(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteInativo));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> veiculoService.criar(request)
        );

        assertEquals("O cliente informado não está ativo", exception.getMessage());

        verify(veiculoRepository).existsByPlaca("ABC1234");
        verify(clienteRepository).findById(1L);
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void deveListarVeiculos() {
        Veiculo veiculo1 = criarVeiculo(1L, "ABC1234", clienteAtivo);
        Veiculo veiculo2 = criarVeiculo(2L, "DEF5678", clienteAtivo);

        when(veiculoRepository.findAll()).thenReturn(List.of(veiculo1, veiculo2));

        List<VeiculoResponse> responses = veiculoService.listar();

        assertEquals(2, responses.size());
        assertEquals("ABC1234", responses.get(0).placa());
        assertEquals("DEF5678", responses.get(1).placa());

        verify(veiculoRepository).findAll();
    }

    @Test
    void deveBuscarVeiculoPorIdComSucesso() {
        Veiculo veiculo = criarVeiculo(10L, "ABC1234", clienteAtivo);

        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(veiculo));

        VeiculoResponse response = veiculoService.buscarPorId(10L);

        assertEquals(10L, response.id());
        assertEquals("ABC1234", response.placa());
        assertEquals(1L, response.clienteId());

        verify(veiculoRepository).findById(10L);
    }

    @Test
    void deveLancarExcecaoAoBuscarVeiculoPorIdInexistente() {
        when(veiculoRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> veiculoService.buscarPorId(99L)
        );

        assertEquals("Veículo não encontrado", exception.getMessage());

        verify(veiculoRepository).findById(99L);
    }

    @Test
    void deveListarVeiculosPorCliente() {
        Veiculo veiculo1 = criarVeiculo(1L, "ABC1234", clienteAtivo);
        Veiculo veiculo2 = criarVeiculo(2L, "DEF5678", clienteAtivo);

        when(veiculoRepository.findByClienteId(1L)).thenReturn(List.of(veiculo1, veiculo2));

        List<VeiculoResponse> responses = veiculoService.listarPorCliente(1L);

        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).clienteId());
        assertEquals(1L, responses.get(1).clienteId());

        verify(veiculoRepository).findByClienteId(1L);
    }

    @Test
    void deveAtualizarVeiculoComSucessoMantendoMesmaPlaca() {
        Veiculo veiculo = criarVeiculo(10L, "ABC1234", clienteAtivo);

        VeiculoRequest requestAtualizacao = new VeiculoRequest(
                1L,
                "ABC1234",
                "Honda",
                "Civic",
                2022,
                "Preto",
                "Atualizado"
        );

        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(veiculo));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAtivo));
        when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VeiculoResponse response = veiculoService.atualizar(10L, requestAtualizacao);

        assertEquals(10L, response.id());
        assertEquals("ABC1234", response.placa());
        assertEquals("Honda", response.marca());
        assertEquals("Civic", response.modelo());
        assertEquals(2022, response.ano());
        assertEquals("Preto", response.cor());
        assertEquals("Atualizado", response.observacao());

        verify(veiculoRepository).findById(10L);
        verify(clienteRepository).findById(1L);
        verify(veiculoRepository, never()).existsByPlaca(anyString());
        verify(veiculoRepository).save(veiculo);
    }

    @Test
    void deveAtualizarVeiculoComNovaPlacaQuandoNaoExisteDuplicidade() {
        Veiculo veiculo = criarVeiculo(10L, "ABC1234", clienteAtivo);

        VeiculoRequest requestAtualizacao = new VeiculoRequest(
                1L,
                "XYZ9999",
                "Toyota",
                "Corolla",
                2021,
                "Branco",
                "Nova placa"
        );

        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(veiculo));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAtivo));
        when(veiculoRepository.existsByPlaca("XYZ9999")).thenReturn(false);
        when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VeiculoResponse response = veiculoService.atualizar(10L, requestAtualizacao);

        assertEquals("XYZ9999", response.placa());

        verify(veiculoRepository).findById(10L);
        verify(clienteRepository).findById(1L);
        verify(veiculoRepository).existsByPlaca("XYZ9999");
        verify(veiculoRepository).save(veiculo);
    }

    @Test
    void naoDeveAtualizarVeiculoQuandoNovaPlacaJaExiste() {
        Veiculo veiculo = criarVeiculo(10L, "ABC1234", clienteAtivo);

        VeiculoRequest requestAtualizacao = new VeiculoRequest(
                1L,
                "XYZ9999",
                "Toyota",
                "Corolla",
                2021,
                "Branco",
                "Nova placa"
        );

        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(veiculo));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAtivo));
        when(veiculoRepository.existsByPlaca("XYZ9999")).thenReturn(true);

        RecursoDuplicadoException exception = assertThrows(
                RecursoDuplicadoException.class,
                () -> veiculoService.atualizar(10L, requestAtualizacao)
        );

        assertEquals("Já existe veículo cadastrado com esta placa", exception.getMessage());

        verify(veiculoRepository).findById(10L);
        verify(clienteRepository).findById(1L);
        verify(veiculoRepository).existsByPlaca("XYZ9999");
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void naoDeveAtualizarVeiculoQuandoVeiculoNaoExiste() {
        when(veiculoRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> veiculoService.atualizar(99L, request)
        );

        assertEquals("Veículo não encontrado", exception.getMessage());

        verify(veiculoRepository).findById(99L);
        verify(clienteRepository, never()).findById(anyLong());
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void naoDeveAtualizarVeiculoQuandoClienteNaoExiste() {
        Veiculo veiculo = criarVeiculo(10L, "ABC1234", clienteAtivo);

        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(veiculo));
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> veiculoService.atualizar(10L, request)
        );

        assertEquals("Cliente não encontrado", exception.getMessage());

        verify(veiculoRepository).findById(10L);
        verify(clienteRepository).findById(1L);
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void naoDeveAtualizarVeiculoQuandoClienteEstaInativo() {
        Veiculo veiculo = criarVeiculo(10L, "ABC1234", clienteAtivo);
        Cliente clienteInativo = criarCliente(1L, false);

        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(veiculo));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteInativo));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> veiculoService.atualizar(10L, request)
        );

        assertEquals("O cliente informado não está ativo", exception.getMessage());

        verify(veiculoRepository).findById(10L);
        verify(clienteRepository).findById(1L);
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void deveInativarVeiculoComSucesso() {
        Veiculo veiculo = criarVeiculo(10L, "ABC1234", clienteAtivo);

        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(veiculo));

        veiculoService.inativar(10L);

        ArgumentCaptor<Veiculo> veiculoCaptor = ArgumentCaptor.forClass(Veiculo.class);
        verify(veiculoRepository).save(veiculoCaptor.capture());

        Veiculo veiculoSalvo = veiculoCaptor.getValue();

        assertFalse(veiculoSalvo.getAtivo());

        verify(veiculoRepository).findById(10L);
    }

    @Test
    void deveLancarExcecaoAoInativarVeiculoInexistente() {
        when(veiculoRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> veiculoService.inativar(99L)
        );

        assertEquals("Veículo não encontrado", exception.getMessage());

        verify(veiculoRepository).findById(99L);
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void deveBuscarVeiculoPorPlacaComSucesso() {
        Veiculo veiculo = criarVeiculo(10L, "ABC1234", clienteAtivo);

        when(veiculoRepository.findByPlaca("ABC1234")).thenReturn(Optional.of(veiculo));

        VeiculoResponse response = veiculoService.buscarPorPlaca("ABC1234");

        assertEquals(10L, response.id());
        assertEquals("ABC1234", response.placa());
        assertEquals(1L, response.clienteId());

        verify(veiculoRepository).findByPlaca("ABC1234");
    }

    @Test
    void deveLancarExcecaoAoBuscarVeiculoPorPlacaInexistente() {
        when(veiculoRepository.findByPlaca("XXX0000")).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> veiculoService.buscarPorPlaca("XXX0000")
        );

        assertEquals("Veículo não encontrado", exception.getMessage());

        verify(veiculoRepository).findByPlaca("XXX0000");
    }

    private Cliente criarCliente(Long id, Boolean ativo) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNome("João da Silva");
        cliente.setCpfCnpj("12345678901");
        cliente.setAtivo(ativo);
        return cliente;
    }

    private Veiculo criarVeiculo(Long id, String placa, Cliente cliente) {
        Veiculo veiculo = new Veiculo();
        veiculo.setId(id);
        veiculo.setCliente(cliente);
        veiculo.setPlaca(placa);
        veiculo.setMarca("Toyota");
        veiculo.setModelo("Corolla");
        veiculo.setAno(2020);
        veiculo.setCor("Prata");
        veiculo.setObservacao("Veículo em bom estado");
        veiculo.setAtivo(true);
        veiculo.setDataCriacao(LocalDateTime.now());
        veiculo.setDataAtualizacao(LocalDateTime.now());
        return veiculo;
    }
}