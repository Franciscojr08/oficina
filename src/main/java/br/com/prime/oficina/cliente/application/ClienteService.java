package br.com.prime.oficina.cliente.application;

import java.util.List;

import br.com.prime.oficina.ordemservico.application.StatusOrdemServico;
import br.com.prime.oficina.ordemservico.infrastructure.OrdemServicoRepository;
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

import static br.com.prime.oficina.shared.exception.ExceptionMessage.*;

@Service
@RequiredArgsConstructor
public class ClienteService {

	private final ClienteRepository clienteRepository;
	private final OrdemServicoRepository ordemServicoRepository;
	private final ClienteMapper clienteMapper;

	@Transactional
	public ClienteResponse criar(ClienteRequest request) {
		validarCpfCnpjDuplicado(request.cpfCnpj());
		validarCpfCnpj(request.cpfCnpj());

		Cliente cliente = new Cliente();
		preencherCliente(request, cliente);

		Cliente salvo = clienteRepository.save(cliente);
		return clienteMapper.toResponse(salvo);
	}

	@Transactional(readOnly = true)
	public List<ClienteResponse> listar() {
		return clienteRepository.findAll()
				.stream()
				.map(clienteMapper::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public ClienteResponse buscarPorId(Long id) {
		Cliente cliente = clienteRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException(CUSTOMER_NOT_FOUND));
		return clienteMapper.toResponse(cliente);
	}

	@Transactional
	public ClienteResponse atualizar(Long id, ClienteRequest request) {
		validarCpfCnpj(request.cpfCnpj());

		Cliente cliente = clienteRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException(CUSTOMER_NOT_FOUND));

		if (!cliente.getCpfCnpj().equals(request.cpfCnpj())
				&& clienteRepository.existsByCpfCnpj(request.cpfCnpj())) {
			throw new RegraNegocioException(EXISTING_CUSTOMER);
		}

		preencherCliente(request, cliente);

		Cliente atualizado = clienteRepository.save(cliente);
		return clienteMapper.toResponse(atualizado);
	}

	private void preencherCliente(ClienteRequest request, Cliente cliente) {
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
				.orElseThrow(() -> new RecursoNaoEncontradoException(CUSTOMER_NOT_FOUND));

		boolean possuiOrdemAtiva = ordemServicoRepository
				.existsByClienteIdAndStatusIn(id, StatusOrdemServico.statusAtivos());

		if (possuiOrdemAtiva) {
			throw new RegraNegocioException(CANNOT_INACTIVATE_CUSTOMER_WITH_ACTIVE_SERVICE_ORDERS);
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
				.orElseThrow(() -> new RecursoNaoEncontradoException(CUSTOMER_NOT_FOUND));

		return clienteMapper.toResponse(cliente);
	}

	private void validarCpfCnpjDuplicado(String cpfCnpj) {
		if (clienteRepository.existsByCpfCnpj(cpfCnpj)) {
			throw new RegraNegocioException(EXISTING_CUSTOMER);
		}
	}

	private void validarCpfCnpj(String cpfCnpj) {
		String valor = cpfCnpj.replaceAll("\\D", "");

		if (valor.length() == 11) {
			boolean cpfValido = ValidadorCPF.isValido(cpfCnpj);
			if (!cpfValido) {
				throw new RegraNegocioException(INVALID_PERSON_DOCUMENT);
			}
		}

		if (valor.length() == 14) {
			boolean cnpjValido = ValidadorCNPJ.isValido(valor);
			if (!cnpjValido) {
				throw new RegraNegocioException(INVALID_COMPANY_DOCUMENT);
			}
		}
	}

}
