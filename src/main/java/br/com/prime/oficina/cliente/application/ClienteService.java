package br.com.prime.oficina.cliente.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.prime.oficina.cliente.domain.Cliente;
import br.com.prime.oficina.cliente.infraestructure.ClienteRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional
    public ClienteResponse criar(ClienteRequest request) {
        validarCpfCnpjDuplicado(request.cpfCnpj());

        Cliente cliente = new Cliente();
        cliente.setNome(request.nome());
        cliente.setCpfCnpj(request.cpfCnpj());
        cliente.setTelefone(request.telefone());
        cliente.setEmail(request.email());

        Cliente salvo = clienteRepository.save(cliente);
        return toResponse(salvo);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clienteRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        return toResponse(cliente);
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        if (!cliente.getCpfCnpj().equals(request.cpfCnpj())
                && clienteRepository.existsByCpfCnpj(request.cpfCnpj())) {
            throw new RuntimeException("Já existe cliente cadastrado com este CPF/CNPJ");
        }

        cliente.setNome(request.nome());
        cliente.setCpfCnpj(request.cpfCnpj());
        cliente.setTelefone(request.telefone());
        cliente.setEmail(request.email());

        Cliente atualizado = clienteRepository.save(cliente);
        return toResponse(atualizado);
    }

    @Transactional
    public void inativar(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        cliente.setAtivo(false);
        clienteRepository.save(cliente);
    }

    private void validarCpfCnpjDuplicado(String cpfCnpj) {
        if (clienteRepository.existsByCpfCnpj(cpfCnpj)) {
            throw new RuntimeException("Já existe cliente cadastrado com este CPF/CNPJ");
        }
    }

    private ClienteResponse toResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpfCnpj(),
                cliente.getTelefone(),
                cliente.getEmail(),
                cliente.getAtivo(),
                cliente.getDataCriacao(),
                cliente.getDataAtualizacao()
        );
    }
}
