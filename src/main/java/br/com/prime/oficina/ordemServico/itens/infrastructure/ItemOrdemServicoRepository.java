package br.com.prime.oficina.ordemServico.itens.infrastructure;

import br.com.prime.oficina.item.domain.Item;
import br.com.prime.oficina.ordemServico.application.StatusOrdemServico;
import br.com.prime.oficina.ordemServico.itens.domain.ItemOrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemOrdemServicoRepository extends JpaRepository<ItemOrdemServico, Long> {

    Optional<ItemOrdemServico> findByItem(Item item);

    List<ItemOrdemServico> findByOrdemServicoId(Long ordemServicoId);

	@Query("""
        SELECT COALESCE(SUM(i.quantidade), 0)
        FROM ItemOrdemServico i
        WHERE i.ordemServico.id = :ordemId
        AND i.item.id = :itemId
    """)
	int sumQuantidadeByOrdemServicoIdAndItemId(
        @Param("ordemId") Long ordemId,
        @Param("itemId") Long itemId
	);

	boolean existsByItemIdAndOrdemServicoStatusIn(Long itemId,List<StatusOrdemServico> status);
}
