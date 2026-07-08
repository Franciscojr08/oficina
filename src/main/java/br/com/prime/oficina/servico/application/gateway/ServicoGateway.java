package br.com.prime.oficina.servico.application.gateway;

import br.com.prime.oficina.servico.domain.Servico;

import java.util.List;
import java.util.Optional;

public interface ServicoGateway {

	Servico save(Servico servico);

	Servico saveAndFlush(Servico servico);

	List<Servico> findAll();

	Optional<Servico> findById(Long id);

	boolean existsByNomeIgnoreCase(String nome);
}
