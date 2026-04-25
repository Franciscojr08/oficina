package br.com.prime.oficina.ordemServico.infrastructure;

import br.com.prime.oficina.ordemServico.application.StatusOrdemServico;
import br.com.prime.oficina.ordemServico.domain.OrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {

    List<OrdemServico> findByClienteId(Long clienteId);

    Optional<OrdemServico> findByCodigo(String codigo);

    List<OrdemServico> findByStatus(StatusOrdemServico status);

    boolean existsByCodigo(String codigo);
}
