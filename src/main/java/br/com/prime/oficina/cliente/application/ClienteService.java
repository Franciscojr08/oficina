package br.com.prime.oficina.cliente.application;

import java.util.List;

import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
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
		setDadosCliente(request, cliente);

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
				.orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));
		return toResponse(cliente);
	}

	@Transactional
	public ClienteResponse atualizar(Long id, ClienteRequest request) {
		Cliente cliente = clienteRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));

		if (!cliente.getCpfCnpj().equals(request.cpfCnpj())
				&& clienteRepository.existsByCpfCnpj(request.cpfCnpj())) {
			throw new RegraNegocioException("Já existe cliente cadastrado com este CPF/CNPJ");
		}

		setDadosCliente(request,cliente);

		Cliente atualizado = clienteRepository.save(cliente);
		return toResponse(atualizado);
	}

	private void setDadosCliente(ClienteRequest request, Cliente cliente) {
		cliente.setNome(request.nome());
		cliente.setCpfCnpj(request.cpfCnpj());
		cliente.setTelefone(request.telefone());
		cliente.setEmail(request.email());
		cliente.setCep(request.cep());
		cliente.setLogradouro(request.logradouro());
		cliente.setBairro(request.bairro());
		cliente.setCidade(request.cidade());
		cliente.setUf(request.uf());
	}

	@Transactional
	public void inativar(Long id) {
		Cliente cliente = clienteRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));

		cliente.setAtivo(false);
		clienteRepository.save(cliente);
	}

	@Transactional(readOnly = true)
	public ClienteResponse findByCpfCnpj(String cpfCnpj) {
		Cliente cliente = clienteRepository.findByCpfCnpj(cpfCnpj)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));

		return toResponse(cliente);
	}

	private void validarCpfCnpjDuplicado(String cpfCnpj) {
		if (clienteRepository.existsByCpfCnpj(cpfCnpj)) {
			throw new RegraNegocioException("Já existe cliente cadastrado com este CPF/CNPJ");
		}
	}

	private ClienteResponse toResponse(Cliente cliente) {
		return new ClienteResponse(
				cliente.getId(),
				cliente.getNome(),
				cliente.getCpfCnpj(),
				cliente.getTelefone(),
				cliente.getEmail(),
				cliente.getCep(),
				cliente.getLogradouro(),
				cliente.getBairro(),
				cliente.getCidade(),
				cliente.getUf(),
				cliente.getAtivo(),
				cliente.getDataCriacao(),
				cliente.getDataAtualizacao()
		);
	}
}
