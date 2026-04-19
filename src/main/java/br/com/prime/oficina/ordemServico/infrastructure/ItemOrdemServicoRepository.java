package br.com.prime.oficina.ordemServico.infrastructure;

import br.com.prime.oficina.item.domain.Item;
import br.com.prime.oficina.ordemServico.domain.ItemOrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemOrdemServicoRepository extends JpaRepository<ItemOrdemServico, Long> {

    Optional<ItemOrdemServico> findByItem(Item item);

    Optional<ItemOrdemServico> findByOrdemServicoId(Long ordemServicoId);
}
