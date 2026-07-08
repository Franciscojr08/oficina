package br.com.prime.oficina.cliente.output.persistence;

import br.com.prime.oficina.cliente.application.gateway.ClienteGateway;
import br.com.prime.oficina.cliente.domain.Cliente;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ClientePersistenceAdapter implements ClienteGateway {

	private final ClienteRepository clienteRepository;

	@Override
	public Cliente save(Cliente cliente) {
		return clienteRepository.save(cliente);
	}

	@Override
	public List<Cliente> findAll() {
		return clienteRepository.findAll();
	}

	@Override
	public Optional<Cliente> findById(Long id) {
		return clienteRepository.findById(id);
	}

	@Override
	public Optional<Cliente> findByCpfCnpj(String cpfCnpj) {
		return clienteRepository.findByCpfCnpj(cpfCnpj);
	}

	@Override
	public boolean existsByCpfCnpj(String cpfCnpj) {
		return clienteRepository.existsByCpfCnpj(cpfCnpj);
	}
}
