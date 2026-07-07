package br.com.prime.oficina.item.infrastructure;

import br.com.prime.oficina.item.domain.Item;
import br.com.prime.oficina.item.domain.TipoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByTipo(TipoItem tipo);

    @Query("""
		select exists (
		    select 1
		    from Item i
		    where
				i.tipo = :tipo
		        and lower(coalesce(i.descricao, '')) = lower(coalesce(:descricao, ''))
		        and lower(i.unidadeMedida) = lower(:unidadeMedida))
	""")
    boolean existsDuplicado(TipoItem tipo, String descricao, String unidadeMedida);

    @Query("""
        select exists (
            select 1
            from Item i
            where
                i.tipo = :tipo
                and lower(coalesce(i.descricao, '')) = lower(coalesce(:descricao, ''))
                and lower(i.unidadeMedida) = lower(:unidadeMedida)
                and i.id <> :id)
    """)
    boolean existsDuplicadoNaAtualizacao(Long id, TipoItem tipo, String descricao, String unidadeMedida);
}