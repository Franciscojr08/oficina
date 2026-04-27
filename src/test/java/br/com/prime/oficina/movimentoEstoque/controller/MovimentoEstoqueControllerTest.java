package br.com.prime.oficina.movimentoEstoque.controller;

import br.com.prime.oficina.config.IntegrationTest;
import br.com.prime.oficina.movimentoEstoque.application.MovimentoEstoqueResponse;
import br.com.prime.oficina.movimentoEstoque.application.MovimentoEstoqueService;
import br.com.prime.oficina.movimentoEstoque.domain.TipoMovimentoEstoque;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
public class MovimentoEstoqueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovimentoEstoqueService service;

    @Test
    void testListarPorItem() throws Exception {
        when(service.listarPorItem(1L)).thenReturn(List.of(criarMovimentoEstoque()));

        mockMvc.perform(get("/oficina/v1/movimentacoes-estoque/item/{itemId}", 1L))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testListarPorItemETipo() throws Exception {
        when(service.listarPorItemETipo(1L, TipoMovimentoEstoque.ENTRADA)).thenReturn(List.of(criarMovimentoEstoque()));

        mockMvc.perform(get("/oficina/v1/movimentacoes-estoque/item/{itemId}/tipo/{tipo}", 1L, TipoMovimentoEstoque.ENTRADA))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testListarTodos() throws Exception {
        when(service.listarTodos()).thenReturn(List.of(criarMovimentoEstoque()));

        mockMvc.perform(get("/oficina/v1/movimentacoes-estoque"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    private MovimentoEstoqueResponse criarMovimentoEstoque() {
        return new MovimentoEstoqueResponse(
                1L,
                2L,
                "ItemEstoque",
                TipoMovimentoEstoque.AJUSTE,
                10,
                "Observacao",
                3L,
                LocalDateTime.now()
        );
    }

}
