package br.com.prime.oficina.cliente.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.prime.oficina.cliente.application.gateway.ClienteGateway;
import br.com.prime.oficina.cliente.domain.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long>, ClienteGateway {

	Optional<Cliente> findByCpfCnpj(String cpfCnpj);

	boolean existsByCpfCnpj(String cpfCnpj);

}
