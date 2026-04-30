package br.com.prime.oficina.estoque.controller;

import br.com.prime.oficina.config.ControllerIntegrationTestSupport;
import br.com.prime.oficina.config.IntegrationTest;
import br.com.prime.oficina.estoque.application.EstoqueRequest;
import br.com.prime.oficina.estoque.application.EstoqueResponse;
import br.com.prime.oficina.estoque.application.EstoqueService;
import br.com.prime.oficina.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class EstoqueControllerTest extends ControllerIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private EstoqueService service;

    @Test
    void testListarTodos() throws Exception {
        when(service.listarTodos()).thenReturn(List.of(criarEstoque()));

        mockMvc.perform(get("/estoques")
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testBuscarPorItem() throws Exception {
        when(service.buscarPorItem(1L)).thenReturn(criarEstoque());

        mockMvc.perform(get("/estoques/item/{itemId}", 1L)
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testAtualizarPorItem() throws Exception {
        EstoqueRequest request = criarEstoqueRequest();

        when(service.atualizarPorItem(1L, request)).thenReturn(criarEstoque());

        mockMvc.perform(put("/estoques/item/{itemId}", 1L)
                        .header("Authorization", bearerTokenAdmin(jwtService))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
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