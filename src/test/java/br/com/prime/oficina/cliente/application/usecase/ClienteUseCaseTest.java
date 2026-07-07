package br.com.prime.oficina.cliente.application.usecase;

import br.com.prime.oficina.cliente.application.ClienteMapper;
import br.com.prime.oficina.cliente.application.dto.ClienteRequest;
import br.com.prime.oficina.cliente.application.dto.ClienteResponse;
import br.com.prime.oficina.cliente.domain.Cliente;
import br.com.prime.oficina.cliente.application.gateway.ClienteGateway;
import br.com.prime.oficina.ordemservico.application.gateway.OrdemServicoGateway;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
import br.com.prime.oficina.veiculo.domain.Veiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteUseCaseTest {

    private static final String CPF_VALIDO = "52998224725";
    private static final String OUTRO_CPF_VALIDO = "39053344705";
    private static final String CPF_VALIDO_INEXISTENTE = "11144477735";

    @Mock
    private ClienteGateway clienteRepository;

    @Mock
    private OrdemServicoGateway ordemServicoGateway;

    @Spy
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteUseCase clienteUseCase;

    private ClienteRequest request;

    @BeforeEach
    void setUp() {
        request = new ClienteRequest(
                "João da Silva",
                CPF_VALIDO,
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

        ClienteResponse response = clienteUseCase.criar(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("João da Silva", response.nome());
        assertEquals(CPF_VALIDO, response.cpfCnpj());
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
                () -> clienteUseCase.criar(request)
        );

        assertEquals("Já existe cliente cadastrado com este CPF/CNPJ", exception.getMessage());

        verify(clienteRepository).existsByCpfCnpj(request.cpfCnpj());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void deveListarClientes() {
        Cliente cliente1 = criarCliente(1L, "João da Silva", CPF_VALIDO);
        Cliente cliente2 = criarCliente(2L, "Maria Souza", OUTRO_CPF_VALIDO);

        when(clienteRepository.findAll()).thenReturn(List.of(cliente1, cliente2));

        List<ClienteResponse> responses = clienteUseCase.listar();

        assertEquals(2, responses.size());
        assertEquals("João da Silva", responses.get(0).nome());
        assertEquals("Maria Souza", responses.get(1).nome());

        verify(clienteRepository).findAll();
    }

    @Test
    void deveBuscarClientePorIdComSucesso() {
        Cliente cliente = criarCliente(1L, "João da Silva", CPF_VALIDO);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        ClienteResponse response = clienteUseCase.buscarPorId(1L);

        assertEquals(1L, response.id());
        assertEquals("João da Silva", response.nome());
        assertEquals(CPF_VALIDO, response.cpfCnpj());

        verify(clienteRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoAoBuscarClientePorIdInexistente() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> clienteUseCase.buscarPorId(99L)
        );

        assertEquals("Cliente não encontrado", exception.getMessage());

        verify(clienteRepository).findById(99L);
    }

    @Test
    void deveAtualizarClienteComSucessoMantendoMesmoCpfCnpj() {
        Cliente cliente = criarCliente(1L, "João Antigo", CPF_VALIDO);

        ClienteRequest requestAtualizacao = new ClienteRequest(
                "João Atualizado",
                CPF_VALIDO,
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

        ClienteResponse response = clienteUseCase.atualizar(1L, requestAtualizacao);

        assertEquals("João Atualizado", response.nome());
        assertEquals(CPF_VALIDO, response.cpfCnpj());
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
        Cliente cliente = criarCliente(1L, "João da Silva", CPF_VALIDO);

        ClienteRequest requestAtualizacao = new ClienteRequest(
                "João da Silva",
                OUTRO_CPF_VALIDO,
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
        when(clienteRepository.existsByCpfCnpj(OUTRO_CPF_VALIDO)).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClienteResponse response = clienteUseCase.atualizar(1L, requestAtualizacao);

        assertEquals(OUTRO_CPF_VALIDO, response.cpfCnpj());

        verify(clienteRepository).findById(1L);
        verify(clienteRepository).existsByCpfCnpj(OUTRO_CPF_VALIDO);
        verify(clienteRepository).save(cliente);
    }

    @Test
    void naoDeveAtualizarClienteQuandoNovoCpfCnpjJaExiste() {
        Cliente cliente = criarCliente(1L, "João da Silva", CPF_VALIDO);

        ClienteRequest requestAtualizacao = new ClienteRequest(
                "João da Silva",
                OUTRO_CPF_VALIDO,
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
        when(clienteRepository.existsByCpfCnpj(OUTRO_CPF_VALIDO)).thenReturn(true);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> clienteUseCase.atualizar(1L, requestAtualizacao)
        );

        assertEquals("Já existe cliente cadastrado com este CPF/CNPJ", exception.getMessage());

        verify(clienteRepository).findById(1L);
        verify(clienteRepository).existsByCpfCnpj(OUTRO_CPF_VALIDO);
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoAtualizarClienteInexistente() {
        ClienteRequest requestValido = new ClienteRequest(
                "Cliente Inexistente",
                CPF_VALIDO_INEXISTENTE,
                "85999999999",
                "cliente@email.com",
                "60000000",
                "Rua A",
                "Centro",
                "Fortaleza",
                "CE",
                LocalDate.of(1990, 1, 1)
        );

        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> clienteUseCase.atualizar(99L, requestValido)
        );

        assertEquals("Cliente não encontrado", exception.getMessage());

        verify(clienteRepository).findById(99L);
        verify(clienteRepository, never()).save(any());
        verify(clienteRepository, never()).existsByCpfCnpj(anyString());
    }

    @Test
    void deveInativarClienteESeusVeiculos() {
        Cliente cliente = criarCliente(1L, "João da Silva", CPF_VALIDO);

        Veiculo veiculo1 = new Veiculo();
        veiculo1.setAtivo(true);

        Veiculo veiculo2 = new Veiculo();
        veiculo2.setAtivo(true);

        cliente.setVeiculos(new ArrayList<>(List.of(veiculo1, veiculo2)));

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(ordemServicoGateway.existsByClienteIdAndStatusIn(eq(1L), anyList())).thenReturn(false);

        clienteUseCase.inativar(1L);

        ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(clienteCaptor.capture());

        Cliente clienteSalvo = clienteCaptor.getValue();

        assertFalse(clienteSalvo.getAtivo());
        assertFalse(clienteSalvo.getVeiculos().get(0).getAtivo());
        assertFalse(clienteSalvo.getVeiculos().get(1).getAtivo());

        verify(clienteRepository).findById(1L);
        verify(ordemServicoGateway).existsByClienteIdAndStatusIn(eq(1L), anyList());
    }

    @Test
    void naoDeveInativarClienteQuandoPossuiOrdemServicoAtiva() {
        Cliente cliente = criarCliente(1L, "João da Silva", CPF_VALIDO);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(ordemServicoGateway.existsByClienteIdAndStatusIn(eq(1L), anyList())).thenReturn(true);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> clienteUseCase.inativar(1L)
        );

        assertEquals(
                "Não é possível inativar o cliente, pois ele possui ordens de serviço ativas.",
                exception.getMessage()
        );

        verify(clienteRepository).findById(1L);
        verify(ordemServicoGateway).existsByClienteIdAndStatusIn(eq(1L), anyList());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoInativarClienteInexistente() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> clienteUseCase.inativar(99L)
        );

        assertEquals("Cliente não encontrado", exception.getMessage());

        verify(clienteRepository).findById(99L);
        verify(ordemServicoGateway, never()).existsByClienteIdAndStatusIn(anyLong(), anyList());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void deveBuscarClientePorCpfCnpjComSucesso() {
        Cliente cliente = criarCliente(1L, "João da Silva", CPF_VALIDO);

        when(clienteRepository.findByCpfCnpj(CPF_VALIDO)).thenReturn(Optional.of(cliente));

        ClienteResponse response = clienteUseCase.buscarPorDocumento(CPF_VALIDO);

        assertEquals(1L, response.id());
        assertEquals("João da Silva", response.nome());
        assertEquals(CPF_VALIDO, response.cpfCnpj());

        verify(clienteRepository).findByCpfCnpj(CPF_VALIDO);
    }

    @Test
    void deveLancarExcecaoAoBuscarClientePorCpfCnpjInexistente() {
        when(clienteRepository.findByCpfCnpj(CPF_VALIDO_INEXISTENTE)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> clienteUseCase.buscarPorDocumento(CPF_VALIDO_INEXISTENTE)
        );

        assertEquals("Cliente não encontrado", exception.getMessage());

        verify(clienteRepository).findByCpfCnpj(CPF_VALIDO_INEXISTENTE);
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
