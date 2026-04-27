package br.com.prime.oficina.ordemServico.controller;

import br.com.prime.oficina.config.IntegrationTest;
import br.com.prime.oficina.ordemservico.application.OrdemServicoRequest;
import br.com.prime.oficina.ordemservico.application.OrdemServicoResponse;
import br.com.prime.oficina.ordemservico.application.OrdemServicoService;
import br.com.prime.oficina.ordemservico.application.StatusOrdemServico;
import br.com.prime.oficina.ordemservico.itens.application.ItemOrdemServicoRequest;
import br.com.prime.oficina.ordemservico.itens.application.ItemOrdemServicoResponse;
import br.com.prime.oficina.ordemservico.itens.application.ItemOrdemServicoService;
import br.com.prime.oficina.ordemservico.itens.application.ListaItensOrdemServicoResponse;
import br.com.prime.oficina.ordemservico.servicos.application.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static br.com.prime.oficina.util.JsonStringUtil.toJson;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
public class OrdemServicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrdemServicoService service;

    @MockitoBean
    private ItemOrdemServicoService itemOrdemServicoService;

    @MockitoBean
    private ServicoOrdemServicoService servicoOrdemServicoService;


    @Test
    void testListar() throws Exception {
        when(service.listar()).thenReturn(List.of(criarOrdemServico()));

        mockMvc.perform(get("/oficina/v1/ordens"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testListarPorCliente() throws Exception {
        when(service.listarPorCliente(1L)).thenReturn(List.of(criarOrdemServico()));

        mockMvc.perform(get("/oficina/v1/cliente/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testListarPorCodigo() throws Exception {
        when(service.listarPorCodigo("codigo")).thenReturn(List.of(criarOrdemServico()));

        mockMvc.perform(get("/oficina/v1/codigo/{codigo}", "codigo"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testListarPorStatus() throws Exception {
        when(service.listarPorStatus(StatusOrdemServico.ENTREGUE)).thenReturn(List.of(criarOrdemServico()));

        mockMvc.perform(get("/oficina/v1/status/{status}", "codigo"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testCriar() throws Exception {
        when(service.criar(criarOrdemServicoRequest())).thenReturn(criarOrdemServico());

        mockMvc.perform(post("/oficina/v1/ordens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(criarItemOrdemServicoRequest())))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void testAtualizar() throws Exception {
        when(service.atualizar(1L, criarOrdemServicoRequest())).thenReturn(criarOrdemServico());

        mockMvc.perform(put("/oficina/v1/ordens/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(criarServicoOrdemServicoRequest())))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testAdicionarItem() throws Exception {
        when(itemOrdemServicoService.adicionarItem(1L, criarItemOrdemServicoRequest())).thenReturn(criarListaItensOrdemServicoResponse());

        mockMvc.perform(put("/oficina/v1/ordens/{id}/item", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(criarItemOrdemServicoRequest())))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testAdicionarServico() throws Exception {
        when(servicoOrdemServicoService.adicionarServico(1L, criarServicoOrdemServicoRequest())).thenReturn(criarListaServicosOrdemServicoResponse());

        mockMvc.perform(put("/oficina/v1/ordens/{id}/servico", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(criarServicoOrdemServicoRequest())))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testAprovarOrdemServico() throws Exception {
        when(service.aprovarOrdemServico(1L)).thenReturn(criarOrdemServico());

        mockMvc.perform(put("/oficina/v1/ordens/{id}/aprovar", 1L))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testReprovarOrdemServico() throws Exception {
        when(service.reprovarOrdemServico(1L)).thenReturn(criarOrdemServico());

        mockMvc.perform(put("/oficina/v1/ordens/{id}/reprovar", 1L))
                .andDo(print())
                .andExpect(status().isOk());
    }

    private OrdemServicoResponse criarOrdemServico() {
        return new OrdemServicoResponse(
                1L,
                "Codigo",
                "Descricao",
                "Observacoes",
                "Descricao",
                StatusOrdemServico.RECEBIDA,
                BigDecimal.TEN,
                BigDecimal.TEN,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private OrdemServicoRequest criarOrdemServicoRequest() {
        return new OrdemServicoRequest(
                "Descricao",
                "Observacoes",
                "Descricao",
                1L,
                2L
        );
    }

    private ItemOrdemServicoRequest criarItemOrdemServicoRequest() {
        return new ItemOrdemServicoRequest(1L, 10);
    }

    private ServicoOrdemServicoRequest criarServicoOrdemServicoRequest() {
        return new ServicoOrdemServicoRequest(1L);
    }

    private ListaItensOrdemServicoResponse criarListaItensOrdemServicoResponse() {
        return new ListaItensOrdemServicoResponse(
                List.of(new ItemOrdemServicoResponse(
                        "CodigoOS",
                        1L,
                        "Item",
                        10,
                        BigDecimal.TEN,
                        BigDecimal.TEN
                )),
                BigDecimal.TEN
        );
    }

    private ListaServicosOrdemServicoResponse criarListaServicosOrdemServicoResponse() {
        return new ListaServicosOrdemServicoResponse(
                List.of(new ServicoOrdemServicoResponse(
                        "codigoOS",
                        1L,
                        "Servico",
                        BigDecimal.ONE,
                        StatusServico.INICIADO,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )),
                BigDecimal.TEN
        );
    }
}
