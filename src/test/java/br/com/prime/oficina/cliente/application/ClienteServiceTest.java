package br.com.prime.oficina.cliente.application;

import br.com.prime.oficina.cliente.domain.Cliente;
import br.com.prime.oficina.cliente.infraestructure.ClienteRepository;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
import br.com.prime.oficina.veiculo.domain.Veiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    private ClienteRequest request;

    @BeforeEach
    void setUp() {
        request = new ClienteRequest(
                "João da Silva",
                "12345678901",
                "85999999999",
                "joao@email.com",
                "60000000",
                "Rua A",
                "Centro",
                "Fortaleza",
                "CE",
                LocalDate.of(1990, 1, 1)
        );
    }

    @Test
    void deveCriarClienteComSucesso() {
        when(clienteRepository.existsByCpfCnpj(request.cpfCnpj())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente cliente = invocation.getArgument(0);
            cliente.setId(1L);
            cliente.setAtivo(true);
            cliente.setDataCriacao(LocalDateTime.now());
            cliente.setDataAtualizacao(LocalDateTime.now());
            return cliente;
        });

        ClienteResponse response = clienteService.criar(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("João da Silva", response.nome());
        assertEquals("12345678901", response.cpfCnpj());
        assertEquals("85999999999", response.telefone());
        assertEquals("joao@email.com", response.email());
        assertEquals("60000000", response.cep());
        assertEquals("Rua A", response.logradouro());
        assertEquals("Centro", response.bairro());
        assertEquals("Fortaleza", response.cidade());
        assertEquals("CE", response.uf());
        assertEquals(LocalDate.of(1990, 1, 1), response.dataNascimento());

        verify(clienteRepository).existsByCpfCnpj(request.cpfCnpj());
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void naoDeveCriarClienteQuandoCpfCnpjJaExiste() {
        when(clienteRepository.existsByCpfCnpj(request.cpfCnpj())).thenReturn(true);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> clienteService.criar(request)
        );

        assertEquals("Já existe cliente cadastrado com este CPF/CNPJ", exception.getMessage());

        verify(clienteRepository).existsByCpfCnpj(request.cpfCnpj());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void deveListarClientes() {
        Cliente cliente1 = criarCliente(1L, "João da Silva", "12345678901");
        Cliente cliente2 = criarCliente(2L, "Maria Souza", "98765432100");

        when(clienteRepository.findAll()).thenReturn(List.of(cliente1, cliente2));

        List<ClienteResponse> responses = clienteService.listar();

        assertEquals(2, responses.size());
        assertEquals("João da Silva", responses.get(0).nome());
        assertEquals("Maria Souza", responses.get(1).nome());

        verify(clienteRepository).findAll();
    }

    @Test
    void deveBuscarClientePorIdComSucesso() {
        Cliente cliente = criarCliente(1L, "João da Silva", "12345678901");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        ClienteResponse response = clienteService.buscarPorId(1L);

        assertEquals(1L, response.id());
        assertEquals("João da Silva", response.nome());
        assertEquals("12345678901", response.cpfCnpj());

        verify(clienteRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoAoBuscarClientePorIdInexistente() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> clienteService.buscarPorId(99L)
        );

        assertEquals("Cliente não encontrado", exception.getMessage());

        verify(clienteRepository).findById(99L);
    }

    @Test
    void deveAtualizarClienteComSucessoMantendoMesmoCpfCnpj() {
        Cliente cliente = criarCliente(1L, "João Antigo", "12345678901");

        ClienteRequest requestAtualizacao = new ClienteRequest(
                "João Atualizado",
                "12345678901",
                "85888888888",
                "joao.atualizado@email.com",
                "60100000",
                "Rua Nova",
                "Aldeota",
                "Fortaleza",
                "CE",
                LocalDate.of(1991, 2, 2)
        );

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClienteResponse response = clienteService.atualizar(1L, requestAtualizacao);

        assertEquals("João Atualizado", response.nome());
        assertEquals("12345678901", response.cpfCnpj());
        assertEquals("85888888888", response.telefone());
        assertEquals("joao.atualizado@email.com", response.email());
        assertEquals("60100000", response.cep());
        assertEquals("Rua Nova", response.logradouro());
        assertEquals("Aldeota", response.bairro());
        assertEquals(LocalDate.of(1991, 2, 2), response.dataNascimento());

        verify(clienteRepository).findById(1L);
        verify(clienteRepository, never()).existsByCpfCnpj(anyString());
        verify(clienteRepository).save(cliente);
    }

    @Test
    void deveAtualizarClienteComNovoCpfCnpjQuandoNaoExisteDuplicidade() {
        Cliente cliente = criarCliente(1L, "João da Silva", "12345678901");

        ClienteRequest requestAtualizacao = new ClienteRequest(
                "João da Silva",
                "22222222222",
                "85999999999",
                "joao@email.com",
                "60000000",
                "Rua A",
                "Centro",
                "Fortaleza",
                "CE",
                LocalDate.of(1990, 1, 1)
        );

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByCpfCnpj("22222222222")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClienteResponse response = clienteService.atualizar(1L, requestAtualizacao);

        assertEquals("22222222222", response.cpfCnpj());

        verify(clienteRepository).findById(1L);
        verify(clienteRepository).existsByCpfCnpj("22222222222");
        verify(clienteRepository).save(cliente);
    }

    @Test
    void naoDeveAtualizarClienteQuandoNovoCpfCnpjJaExiste() {
        Cliente cliente = criarCliente(1L, "João da Silva", "12345678901");

        ClienteRequest requestAtualizacao = new ClienteRequest(
                "João da Silva",
                "22222222222",
                "85999999999",
                "joao@email.com",
                "60000000",
                "Rua A",
                "Centro",
                "Fortaleza",
                "CE",
                LocalDate.of(1990, 1, 1)
        );

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByCpfCnpj("22222222222")).thenReturn(true);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> clienteService.atualizar(1L, requestAtualizacao)
        );

        assertEquals("Já existe cliente cadastrado com este CPF/CNPJ", exception.getMessage());

        verify(clienteRepository).findById(1L);
        verify(clienteRepository).existsByCpfCnpj("22222222222");
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoAtualizarClienteInexistente() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> clienteService.atualizar(99L, request)
        );

        assertEquals("Cliente não encontrado", exception.getMessage());

        verify(clienteRepository).findById(99L);
        verify(clienteRepository, never()).save(any());
        verify(clienteRepository, never()).existsByCpfCnpj(anyString());
    }

    @Test
    void deveInativarClienteESeusVeiculos() {
        Cliente cliente = criarCliente(1L, "João da Silva", "12345678901");

        Veiculo veiculo1 = new Veiculo();
        veiculo1.setAtivo(true);

        Veiculo veiculo2 = new Veiculo();
        veiculo2.setAtivo(true);

        cliente.setVeiculos(new ArrayList<>(List.of(veiculo1, veiculo2)));

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        clienteService.inativar(1L);

        ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(clienteCaptor.capture());

        Cliente clienteSalvo = clienteCaptor.getValue();

        assertFalse(clienteSalvo.getAtivo());
        assertFalse(clienteSalvo.getVeiculos().get(0).getAtivo());
        assertFalse(clienteSalvo.getVeiculos().get(1).getAtivo());

        verify(clienteRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoAoInativarClienteInexistente() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> clienteService.inativar(99L)
        );

        assertEquals("Cliente não encontrado", exception.getMessage());

        verify(clienteRepository).findById(99L);
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void deveBuscarClientePorCpfCnpjComSucesso() {
        Cliente cliente = criarCliente(1L, "João da Silva", "12345678901");

        when(clienteRepository.findByCpfCnpj("12345678901")).thenReturn(Optional.of(cliente));

        ClienteResponse response = clienteService.findByCpfCnpj("12345678901");

        assertEquals(1L, response.id());
        assertEquals("João da Silva", response.nome());
        assertEquals("12345678901", response.cpfCnpj());

        verify(clienteRepository).findByCpfCnpj("12345678901");
    }

    @Test
    void deveLancarExcecaoAoBuscarClientePorCpfCnpjInexistente() {
        when(clienteRepository.findByCpfCnpj("00000000000")).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> clienteService.findByCpfCnpj("00000000000")
        );

        assertEquals("Cliente não encontrado", exception.getMessage());

        verify(clienteRepository).findByCpfCnpj("00000000000");
    }

    private Cliente criarCliente(Long id, String nome, String cpfCnpj) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNome(nome);
        cliente.setCpfCnpj(cpfCnpj);
        cliente.setTelefone("85999999999");
        cliente.setEmail("cliente@email.com");
        cliente.setCep("60000000");
        cliente.setLogradouro("Rua A");
        cliente.setBairro("Centro");
        cliente.setCidade("Fortaleza");
        cliente.setUf("CE");
        cliente.setDataNascimento(LocalDate.of(1990, 1, 1));
        cliente.setAtivo(true);
        cliente.setDataCriacao(LocalDateTime.now());
        cliente.setDataAtualizacao(LocalDateTime.now());
        cliente.setVeiculos(new ArrayList<>());
        return cliente;
    }
}