package br.com.prime.oficina.estoque.infrastructure;

import br.com.prime.oficina.estoque.domain.Estoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    Optional<Estoque> findByItemId(Long itemId);

	@Modifying
	@Query("""
        UPDATE Estoque e
        SET e.quantidade = e.quantidade - :qtd
        WHERE e.id = :id AND e.quantidade >= :qtd
    """)
	int baixarEstoque(@Param("id") Long id, @Param("qtd") int qtd);
}