package br.com.prime.oficina.movimentoestoque.entrypoint.controller;

import br.com.prime.oficina.config.ControllerIntegrationTestSupport;
import br.com.prime.oficina.config.IntegrationTest;
import br.com.prime.oficina.movimentoestoque.application.dto.MovimentoEstoqueResponse;
import br.com.prime.oficina.movimentoestoque.application.usecase.MovimentoEstoqueUseCase;
import br.com.prime.oficina.movimentoestoque.domain.TipoMovimentoEstoque;
import br.com.prime.oficina.security.JwtService;
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
class MovimentoEstoqueControllerTest extends ControllerIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private MovimentoEstoqueUseCase service;

    @Test
    void testListarPorItem() throws Exception {
        when(service.listarPorItem(1L)).thenReturn(List.of(criarMovimentoEstoque()));

        mockMvc.perform(get("/movimentacoes-estoque/item/{itemId}", 1L)
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testListarPorItemETipo() throws Exception {
        when(service.listarPorItemETipo(1L, TipoMovimentoEstoque.ENTRADA))
                .thenReturn(List.of(criarMovimentoEstoque()));

        mockMvc.perform(get(
                        "/movimentacoes-estoque/item/{itemId}/tipo/{tipo}",
                        1L,
                        TipoMovimentoEstoque.ENTRADA
                )
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testListarTodos() throws Exception {
        when(service.listarTodos()).thenReturn(List.of(criarMovimentoEstoque()));

        mockMvc.perform(get("/movimentacoes-estoque")
                        .header("Authorization", bearerTokenAdmin(jwtService)))
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