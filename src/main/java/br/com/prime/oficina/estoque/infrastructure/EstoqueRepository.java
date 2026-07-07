package br.com.prime.oficina.estoque.infrastructure;

import br.com.prime.oficina.estoque.application.gateway.EstoqueGateway;
import br.com.prime.oficina.estoque.domain.Estoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EstoqueRepository extends JpaRepository<Estoque, Long>, EstoqueGateway {

    Optional<Estoque> findByItemId(Long itemId);

	@Modifying
	@Query("""
        UPDATE Estoque e
        SET e.quantidade = e.quantidade - :qtd
        WHERE e.id = :id AND e.quantidade >= :qtd
    """)
	int baixarEstoque(@Param("id") Long id, @Param("qtd") int qtd);

	@Query("""
        SELECT CASE WHEN COUNT(ios) = 0 THEN true ELSE false END
        FROM ItemOrdemServico ios
        JOIN ios.item i
        JOIN i.estoque e
        WHERE ios.ordemServico.id = :ordemServicoId
        AND e.quantidade < ios.quantidade
    """)
	boolean temEstoqueCompletoParaOrdem(@Param("ordemServicoId") Long ordemServicoId);
}