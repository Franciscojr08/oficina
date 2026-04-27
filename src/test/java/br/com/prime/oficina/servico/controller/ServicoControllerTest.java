package br.com.prime.oficina.servico.controller;

import br.com.prime.oficina.config.IntegrationTest;
import br.com.prime.oficina.servico.application.ServicoRequest;
import br.com.prime.oficina.servico.application.ServicoResponse;
import br.com.prime.oficina.servico.application.ServicoService;
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
public class ServicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServicoService service;

    @Test
    void testCriar() throws Exception {
        when(service.criar(criarServicoRequest())).thenReturn(criarServico());

        mockMvc.perform(post("/oficina/v1/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(criarServicoRequest())))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void testListar() throws Exception {
        when(service.listar()).thenReturn(List.of(criarServico()));

        mockMvc.perform(get("/oficina/v1/servicos"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testBuscarPorId() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(criarServico());

        mockMvc.perform(get("/oficina/v1/servicos/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testAtualizar() throws Exception {
        when(service.atualizar(1L, criarServicoRequest())).thenReturn(criarServico());

        mockMvc.perform(put("/oficina/v1/servicos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(criarServicoRequest())))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testInativar() throws Exception {
        doNothing().when(service).inativar(1L);

        mockMvc.perform(delete("/oficina/v1/servicos/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk());

        verify(service, times(1)).inativar(1L);
    }

    private ServicoResponse criarServico() {
        return new ServicoResponse(
                1L,
                "Servico",
                "DescricaoServico",
                BigDecimal.TEN,
                Boolean.TRUE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private ServicoRequest criarServicoRequest() {
        return new ServicoRequest(
                "Servico",
                "DescricaoServico",
                BigDecimal.TEN
        );
    }
}
