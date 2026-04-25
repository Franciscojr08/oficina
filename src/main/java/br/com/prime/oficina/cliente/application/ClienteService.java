package br.com.prime.oficina.cliente.application;

import java.util.List;

import br.com.prime.oficina.ordemServico.application.StatusOrdemServico;
import br.com.prime.oficina.ordemServico.infrastructure.OrdemServicoRepository;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
import br.com.prime.oficina.shared.validator.ValidadorCNPJ;
import br.com.prime.oficina.shared.validator.ValidadorCPF;
import br.com.prime.oficina.veiculo.domain.Veiculo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.prime.oficina.cliente.domain.Cliente;
import br.com.prime.oficina.cliente.infraestructure.ClienteRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteService {

	private final ClienteRepository clienteRepository;
	private final OrdemServicoRepository ordemServicoRepository;

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
		validarCpfCnpj(request.cpfCnpj());

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
		cliente.setDataNascimento(request.data_nascimento());
	}

	@Transactional
	public void inativar(Long id) {
		Cliente cliente = clienteRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));

		boolean possuiOrdemAtiva = ordemServicoRepository
				.existsByClienteIdAndStatusIn(id, StatusOrdemServico.statusAtivos());

		if (possuiOrdemAtiva) {
			throw new RegraNegocioException(
				"Não é possível inativar o cliente, pois ele possui ordens de serviço ativas."
			);
		}

		cliente.setAtivo(false);

		for (Veiculo veiculo : cliente.getVeiculos()) {
			veiculo.setAtivo(false);
		}

		clienteRepository.save(cliente);
	}

	@Transactional(readOnly = true)
	public ClienteResponse findByCpfCnpj(String cpfCnpj) {
		Cliente cliente = clienteRepository.findByCpfCnpj(cpfCnpj)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));

		return toResponse(cliente);
	}

	private void validarCpfCnpjDuplicado(String cpfCnpj) {
		validarCpfCnpj(cpfCnpj);

		if (clienteRepository.existsByCpfCnpj(cpfCnpj)) {
			throw new RegraNegocioException("Já existe cliente cadastrado com este CPF/CNPJ");
		}
	}

	private void validarCpfCnpj(String cpfCnpj) {
		validarCpfCnpjDuplicado(cpfCnpj);

		String valor = cpfCnpj.replaceAll("\\D", "");

		if (valor.length() == 11) {
			boolean cpfValido = ValidadorCPF.isValido(cpfCnpj);
			if (!cpfValido) {
				throw new RegraNegocioException("CPF inválido");
			}
		}

		if (valor.length() == 14) {
			boolean cnpjValido = ValidadorCNPJ.isValido(valor);
			if (!cnpjValido) {
				throw new RegraNegocioException("CNPJ inválido");
			}
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
				cliente.getDataNascimento(),
				cliente.getAtivo(),
				cliente.getDataCriacao(),
				cliente.getDataAtualizacao()
		);
	}
}
