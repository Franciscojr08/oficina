package br.com.prime.oficina.estoque.infrastructure;

import br.com.prime.oficina.estoque.domain.Estoque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    Optional<Estoque> findByItemId(Long itemId);
}