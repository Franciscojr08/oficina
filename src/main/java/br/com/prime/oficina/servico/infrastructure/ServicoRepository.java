package br.com.prime.oficina.servico.infrastructure;

import br.com.prime.oficina.servico.application.gateway.ServicoGateway;
import br.com.prime.oficina.servico.domain.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicoRepository extends JpaRepository<Servico, Long>, ServicoGateway {

    boolean existsByNomeIgnoreCase(String nome);
}
