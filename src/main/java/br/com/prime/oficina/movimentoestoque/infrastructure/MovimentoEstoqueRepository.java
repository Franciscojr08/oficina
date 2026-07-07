package br.com.prime.oficina.movimentoestoque.infrastructure;

import br.com.prime.oficina.movimentoestoque.application.gateway.MovimentoEstoqueGateway;
import br.com.prime.oficina.movimentoestoque.domain.MovimentoEstoque;
import br.com.prime.oficina.movimentoestoque.domain.TipoMovimentoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimentoEstoqueRepository extends JpaRepository<MovimentoEstoque, Long>, MovimentoEstoqueGateway {

    List<MovimentoEstoque> findByItemIdOrderByDataMovimentacaoDesc(Long itemId);

    List<MovimentoEstoque> findByItemIdAndTipoOrderByDataMovimentacaoDesc(
            Long itemId,
            TipoMovimentoEstoque tipo
    );

    List<MovimentoEstoque> findAllByOrderByDataMovimentacaoDesc();
}