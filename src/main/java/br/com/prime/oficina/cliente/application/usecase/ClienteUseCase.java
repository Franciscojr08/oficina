package br.com.prime.oficina.cliente.application.usecase;

import br.com.prime.oficina.cliente.application.ClienteMapper;
import br.com.prime.oficina.cliente.application.ClienteRequest;
import br.com.prime.oficina.cliente.application.ClienteResponse;
import br.com.prime.oficina.cliente.application.gateway.ClienteGateway;
import br.com.prime.oficina.cliente.domain.Cliente;
import br.com.prime.oficina.ordemservico.application.StatusOrdemServico;
import br.com.prime.oficina.ordemservico.application.gateway.OrdemServicoGateway;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
import br.com.prime.oficina.shared.validator.ValidadorCNPJ;
import br.com.prime.oficina.shared.validator.ValidadorCPF;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static br.com.prime.oficina.shared.exception.ExceptionMessage.CANNOT_INACTIVATE_CUSTOMER_WITH_ACTIVE_SERVICE_ORDERS;
import static br.com.prime.oficina.shared.exception.ExceptionMessage.CUSTOMER_NOT_FOUND;
import static br.com.prime.oficina.shared.exception.ExceptionMessage.EXISTING_CUSTOMER;
import static br.com.prime.oficina.shared.exception.ExceptionMessage.INVALID_COMPANY_DOCUMENT;
import static br.com.prime.oficina.shared.exception.ExceptionMessage.INVALID_PERSON_DOCUMENT;

@Service
@RequiredArgsConstructor
public class ClienteUseCase {

	private final ClienteGateway clienteGateway;
	private final OrdemServicoGateway ordemServicoGateway;
	private final ClienteMapper clienteMapper;

	@Transactional
	public ClienteResponse criar(ClienteRequest request) {
		validarCpfCnpjDuplicado(request.cpfCnpj());
		validarCpfCnpj(request.cpfCnpj());

		Cliente cliente = new Cliente();
		preencherCliente(request, cliente);

		Cliente salvo = clienteGateway.save(cliente);
		return clienteMapper.toResponse(salvo);
	}

	@Transactional(readOnly = true)
	public List<ClienteResponse> listar() {
		return clienteGateway.findAll()
				.stream()
				.map(clienteMapper::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public ClienteResponse buscarPorId(Long id) {
		Cliente cliente = buscarClientePorId(id);
		return clienteMapper.toResponse(cliente);
	}

	@Transactional
	public ClienteResponse atualizar(Long id, ClienteRequest request) {
		validarCpfCnpj(request.cpfCnpj());

		Cliente cliente = buscarClientePorId(id);

		if (!cliente.getCpfCnpj().equals(request.cpfCnpj())
				&& clienteGateway.existsByCpfCnpj(request.cpfCnpj())) {
			throw new RegraNegocioException(EXISTING_CUSTOMER);
		}

		preencherCliente(request, cliente);

		Cliente atualizado = clienteGateway.save(cliente);
		return clienteMapper.toResponse(atualizado);
	}

	@Transactional(readOnly = true)
	public ClienteResponse buscarPorDocumento(String documento) {
		Cliente cliente = clienteGateway.findByCpfCnpj(documento)
				.orElseThrow(() -> new RecursoNaoEncontradoException(CUSTOMER_NOT_FOUND));

		return clienteMapper.toResponse(cliente);
	}

	@Transactional
	public void inativar(Long id) {
		Cliente cliente = buscarClientePorId(id);

		boolean possuiOrdemAtiva = ordemServicoGateway
				.existsByClienteIdAndStatusIn(id, StatusOrdemServico.statusAtivos());

		if (possuiOrdemAtiva) {
			throw new RegraNegocioException(CANNOT_INACTIVATE_CUSTOMER_WITH_ACTIVE_SERVICE_ORDERS);
		}

		cliente.inativar();
		clienteGateway.save(cliente);
	}

	private Cliente buscarClientePorId(Long id) {
		return clienteGateway.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException(CUSTOMER_NOT_FOUND));
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

	private void validarCpfCnpjDuplicado(String cpfCnpj) {
		if (clienteGateway.existsByCpfCnpj(cpfCnpj)) {
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
