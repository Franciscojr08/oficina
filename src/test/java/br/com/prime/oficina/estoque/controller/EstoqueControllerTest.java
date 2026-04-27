package br.com.prime.oficina.estoque.controller;

import br.com.prime.oficina.config.IntegrationTest;
import br.com.prime.oficina.estoque.application.EstoqueRequest;
import br.com.prime.oficina.estoque.application.EstoqueResponse;
import br.com.prime.oficina.estoque.application.EstoqueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static br.com.prime.oficina.util.JsonStringUtil.toJson;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
public class EstoqueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EstoqueService service;

    @Test
    void testListarTodos() throws Exception {
        when(service.listarTodos()).thenReturn(List.of(criarEstoque()));

        mockMvc.perform(get("/oficina/v1/estoques"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testBuscarPorItem() throws Exception {
        when(service.buscarPorItem(1L)).thenReturn(criarEstoque());

        mockMvc.perform(get("/oficina/v1/estoques/item/{itemId}", 1L))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testAtualizarPorItem() throws Exception {
        when(service.atualizarPorItem(1L, criarEstoqueRequest())).thenReturn(criarEstoque());

        mockMvc.perform(get("/oficina/v1/estoques/item/{itemId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(criarEstoqueRequest())))
                .andDo(print())
                .andExpect(status().isOk());
    }

    private EstoqueResponse criarEstoque() {
        return new EstoqueResponse(
                1L,
                2L,
                "ItemEstoque",
                10,
                10,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private EstoqueRequest criarEstoqueRequest() {
        return new EstoqueRequest(10, 10);
    }

}
