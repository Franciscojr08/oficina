package br.com.prime.oficina.ordemServico.infrastructure;

import br.com.prime.oficina.ordemServico.domain.OrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {

    List<OrdemServico> findByClienteId(Long clienteId);

    List<OrdemServico> findByCodigo(int codigo);

    List<OrdemServico> findByStatus(String status);

    boolean existsByCodigo(int codigo);
}
