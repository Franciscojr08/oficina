package br.com.prime.oficina.cliente.application.gateway;

import br.com.prime.oficina.cliente.domain.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteGateway {

	Cliente save(Cliente cliente);

	List<Cliente> findAll();

	Optional<Cliente> findById(Long id);

	Optional<Cliente> findByCpfCnpj(String cpfCnpj);

	boolean existsByCpfCnpj(String cpfCnpj);
}
