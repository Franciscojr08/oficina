package br.com.prime.oficina.ordemservico.controller;

import br.com.prime.oficina.config.IntegrationTest;
import br.com.prime.oficina.ordemservico.application.OrdemServicoRequest;
import br.com.prime.oficina.ordemservico.application.OrdemServicoResponse;
import br.com.prime.oficina.ordemservico.application.OrdemServicoService;
import br.com.prime.oficina.ordemservico.application.StatusOrdemServico;
import br.com.prime.oficina.ordemservico.itens.application.ItemOrdemServicoRequest;
import br.com.prime.oficina.ordemservico.itens.application.ItemOrdemServicoResponse;
import br.com.prime.oficina.ordemservico.itens.application.ItemOrdemServicoService;
import br.com.prime.oficina.ordemservico.itens.application.ListaItensOrdemServicoResponse;
import br.com.prime.oficina.ordemservico.servicos.application.ListaServicosOrdemServicoResponse;
import br.com.prime.oficina.ordemservico.servicos.application.ServicoOrdemServicoRequest;
import br.com.prime.oficina.ordemservico.servicos.application.ServicoOrdemServicoResponse;
import br.com.prime.oficina.ordemservico.servicos.application.ServicoOrdemServicoService;
import br.com.prime.oficina.ordemservico.servicos.application.StatusServico;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class OrdemServicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrdemServicoService ordemServicoService;

    @MockitoBean
    private ItemOrdemServicoService itemOrdemServicoService;

    @MockitoBean
    private ServicoOrdemServicoService servicoOrdemServicoService;

    @Test
    void testListar() throws Exception {
        when(ordemServicoService.listar()).thenReturn(List.of(criarOrdemServico()));

        mockMvc.perform(get("/oficina/v1/ordens"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testListarPorCliente() throws Exception {
        when(ordemServicoService.listarPorCliente(1L)).thenReturn(List.of(criarOrdemServico()));

        mockMvc.perform(get("/oficina/v1/ordens/cliente/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testListarPorCodigo() throws Exception {
        when(ordemServicoService.listarPorCodigo("codigo")).thenReturn(List.of(criarOrdemServico()));

        mockMvc.perform(get("/oficina/v1/ordens/codigo/{codigo}", "codigo"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testListarPorStatus() throws Exception {
        when(ordemServicoService.listarPorStatus(StatusOrdemServico.ENTREGUE))
                .thenReturn(List.of(criarOrdemServico()));

        mockMvc.perform(get("/oficina/v1/ordens/status/{status}", StatusOrdemServico.ENTREGUE))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testCriar() throws Exception {
        when(ordemServicoService.criar(criarOrdemServicoRequest())).thenReturn(criarOrdemServico());

        mockMvc.perform(post("/oficina/v1/ordens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(criarOrdemServicoRequest())))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void testAtualizar() throws Exception {
        when(ordemServicoService.atualizar(1L, criarOrdemServicoRequest()))
                .thenReturn(criarOrdemServico());

        mockMvc.perform(put("/oficina/v1/ordens/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(criarOrdemServicoRequest())))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testAdicionarItem() throws Exception {
        when(itemOrdemServicoService.adicionarItem(1L, criarItemOrdemServicoRequest()))
                .thenReturn(criarListaItensOrdemServicoResponse());

        mockMvc.perform(post("/oficina/v1/ordens/{id}/itens", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(criarItemOrdemServicoRequest())))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testListarItensPorOrdemServico() throws Exception {
        when(itemOrdemServicoService.listarItensPorOrdemServico(1L))
                .thenReturn(criarListaItensOrdemServicoResponse());

        mockMvc.perform(get("/oficina/v1/ordens/{id}/itens", 1L))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testAdicionarServico() throws Exception {
        when(servicoOrdemServicoService.adicionarServico(1L, criarServicoOrdemServicoRequest()))
                .thenReturn(criarListaServicosOrdemServicoResponse());

        mockMvc.perform(post("/oficina/v1/ordens/{id}/servicos", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(criarServicoOrdemServicoRequest())))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testListarServicosPorOrdemServico() throws Exception {
        when(servicoOrdemServicoService.listarServicosPorOrdemServico(1L))
                .thenReturn(criarListaServicosOrdemServicoResponse());

        mockMvc.perform(get("/oficina/v1/ordens/{id}/servicos", 1L))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testAprovarOrdemServico() throws Exception {
        when(ordemServicoService.aprovarOrdemServico(1L)).thenReturn(criarOrdemServico());

        mockMvc.perform(patch("/oficina/v1/ordens/{id}/aprovar", 1L))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testReprovarOrdemServico() throws Exception {
        when(ordemServicoService.reprovarOrdemServico(1L)).thenReturn(criarOrdemServico());

        mockMvc.perform(patch("/oficina/v1/ordens/{id}/reprovar", 1L))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testIniciarDiagnostico() throws Exception {
        when(ordemServicoService.iniciarDiagnostico(1L)).thenReturn(criarOrdemServico());

        mockMvc.perform(patch("/oficina/v1/ordens/{id}/iniciar-diagnostico", 1L))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testSolicitarAprovacao() throws Exception {
        when(ordemServicoService.solicitarAprovacao(1L)).thenReturn(criarOrdemServico());

        mockMvc.perform(patch("/oficina/v1/ordens/{id}/solicitar-aprovacao", 1L))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testIniciarServico() throws Exception {
        when(servicoOrdemServicoService.iniciarServico(1L, 2L))
                .thenReturn(criarServicoOrdemServicoResponse());

        mockMvc.perform(patch("/oficina/v1/ordens/{id}/servicos/{servicoId}/iniciar", 1L, 2L))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testFinalizarServico() throws Exception {
        when(servicoOrdemServicoService.finalizarServico(1L, 2L))
                .thenReturn(criarServicoOrdemServicoResponse());

        mockMvc.perform(patch("/oficina/v1/ordens/{id}/servicos/{servicoId}/finalizar", 1L, 2L))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testEntregarOrdemServico() throws Exception {
        when(ordemServicoService.entregarOrdemServico(1L)).thenReturn(criarOrdemServico());

        mockMvc.perform(patch("/oficina/v1/ordens/{id}/entregar", 1L))
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
                List.of(criarServicoOrdemServicoResponse()),
                BigDecimal.TEN
        );
    }

    private ServicoOrdemServicoResponse criarServicoOrdemServicoResponse() {
        return new ServicoOrdemServicoResponse(
                "codigoOS",
                1L,
                "Servico",
                BigDecimal.ONE,
                StatusServico.INICIADO,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}