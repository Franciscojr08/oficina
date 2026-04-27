package br.com.prime.oficina.item.controller;

import br.com.prime.oficina.config.IntegrationTest;
import br.com.prime.oficina.item.application.ItemAtualizacaoRequest;
import br.com.prime.oficina.item.application.ItemRequest;
import br.com.prime.oficina.item.application.ItemResponse;
import br.com.prime.oficina.item.application.ItemService;
import br.com.prime.oficina.item.domain.TipoItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static br.com.prime.oficina.util.JsonStringUtil.toJson;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService service;

    @Test
    void testCriar() throws Exception {
        when(service.criar(criarItemRequest())).thenReturn(criarItem());

        mockMvc.perform(post("/oficina/v1/itens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(criarItemRequest())))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void testListar() throws Exception {
        when(service.listar()).thenReturn(List.of(criarItem()));

        mockMvc.perform(get("/oficina/v1/itens"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testBuscarPorId() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(criarItem());

        mockMvc.perform(get("/oficina/v1/itens/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testListarPorTipo() throws Exception {
        when(service.listarPorTipo(TipoItem.INSUMO)).thenReturn(List.of(criarItem()));

        mockMvc.perform(get("/oficina/v1/itens/tipo/{tipo}", TipoItem.INSUMO))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testAtualizar() throws Exception {
        when(service.atualizar(1L, criarItemAtualizacaoRequest())).thenReturn(criarItem());

        mockMvc.perform(put("/oficina/v1/itens/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(criarItemAtualizacaoRequest())))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testInativar() throws Exception {
        doNothing().when(service).inativar(1L);

        mockMvc.perform(delete("/oficina/v1/itens/{id}", 1L))
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
