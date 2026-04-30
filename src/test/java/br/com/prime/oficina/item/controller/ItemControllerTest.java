package br.com.prime.oficina.item.controller;

import br.com.prime.oficina.config.ControllerIntegrationTestSupport;
import br.com.prime.oficina.config.IntegrationTest;
import br.com.prime.oficina.item.application.ItemAtualizacaoRequest;
import br.com.prime.oficina.item.application.ItemRequest;
import br.com.prime.oficina.item.application.ItemResponse;
import br.com.prime.oficina.item.application.ItemService;
import br.com.prime.oficina.item.domain.TipoItem;
import br.com.prime.oficina.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class ItemControllerTest extends ControllerIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ItemService service;

    @Test
    void testCriar() throws Exception {
        ItemRequest request = criarItemRequest();

        when(service.criar(request)).thenReturn(criarItem());

        mockMvc.perform(post("/itens")
                        .header("Authorization", bearerTokenAdmin(jwtService))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void testListar() throws Exception {
        when(service.listar()).thenReturn(List.of(criarItem()));

        mockMvc.perform(get("/itens")
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testBuscarPorId() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(criarItem());

        mockMvc.perform(get("/itens/{id}", 1L)
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testListarPorTipo() throws Exception {
        when(service.listarPorTipo(TipoItem.INSUMO)).thenReturn(List.of(criarItem()));

        mockMvc.perform(get("/itens/tipo/{tipo}", TipoItem.INSUMO)
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testAtualizar() throws Exception {
        ItemAtualizacaoRequest request = criarItemAtualizacaoRequest();

        when(service.atualizar(1L, request)).thenReturn(criarItem());

        mockMvc.perform(put("/itens/{id}", 1L)
                        .header("Authorization", bearerTokenAdmin(jwtService))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testInativar() throws Exception {
        doNothing().when(service).inativar(1L);

        mockMvc.perform(delete("/itens/{id}", 1L)
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(service, times(1)).inativar(1L);
    }

    private ItemResponse criarItem() {
        return new ItemResponse(
                1L,
                "ItemNome",
                "ItemDescricao",
                TipoItem.INSUMO,
                BigDecimal.TEN,
                "cm",
                Boolean.TRUE,
                10,
                10,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private ItemRequest criarItemRequest() {
        return new ItemRequest(
                "ItemRequest",
                "Descricao",
                TipoItem.PECA,
                BigDecimal.TEN,
                "cm",
                10,
                10,
                "Observacao"
        );
    }

    private ItemAtualizacaoRequest criarItemAtualizacaoRequest() {
        return new ItemAtualizacaoRequest(
                "ItemAtualizacaoRequest",
                "Descricao",
                TipoItem.INSUMO,
                BigDecimal.TEN,
                "cm"
        );
    }
}