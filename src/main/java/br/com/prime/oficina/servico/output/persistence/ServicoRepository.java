package br.com.prime.oficina.servico.output.persistence;

import br.com.prime.oficina.servico.domain.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicoRepository extends JpaRepository<Servico, Long> {

    boolean existsByNomeIgnoreCase(String nome);
}
