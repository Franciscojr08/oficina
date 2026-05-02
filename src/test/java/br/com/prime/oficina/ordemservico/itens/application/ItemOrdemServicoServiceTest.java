package br.com.prime.oficina.ordemservico.itens.application;

import br.com.prime.oficina.item.domain.Item;
import br.com.prime.oficina.ordemservico.domain.OrdemServico;
import br.com.prime.oficina.ordemservico.itens.domain.ItemOrdemServico;
import br.com.prime.oficina.ordemservico.itens.infrastructure.ItemOrdemServicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemOrdemServicoServiceTest {

    @Mock
    private ItemOrdemServicoRepository repository;

    @InjectMocks
    private ItemOrdemServicoService service;

    @Test
    void testListerItensPorOrdemServico() {
        ItemOrdemServico item = criarItemServico();
        List<ItemOrdemServico> list = List.of(item);

        when(repository.findByOrdemServicoId(anyLong())).thenReturn(list);

        var out = service.listarItensPorOrdemServico(1L);

        assertThat(out).usingRecursiveAssertion().isEqualTo(criarListaItens());
    }

    private ItemOrdemServico criarItemServico() {
        ItemOrdemServico itemOrdemServico = new ItemOrdemServico();
        Item item = new Item();
        item.setId(1L);
        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setCodigo("OS-1");

        itemOrdemServico.setItem(item);
        itemOrdemServico.setOrdemServico(ordemServico);
        itemOrdemServico.setId(1L);
        itemOrdemServico.setQuantidade(10);
        itemOrdemServico.setValorUnitario(BigDecimal.TEN);

        return itemOrdemServico;
    }

    private ListaItensOrdemServicoResponse criarListaItens() {
        ItemOrdemServicoResponse item = new ItemOrdemServicoResponse(
                "OS-1",
                1L,
                null,
                10,
                BigDecimal.TEN,
                BigDecimal.valueOf(100)
        );

        return new ListaItensOrdemServicoResponse(List.of(item), BigDecimal.valueOf(100));
    }


}
