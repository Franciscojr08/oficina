package br.com.prime.oficina.servico.entrypoint.controller;

import br.com.prime.oficina.config.ControllerIntegrationTestSupport;
import br.com.prime.oficina.config.IntegrationTest;
import br.com.prime.oficina.security.JwtService;
import br.com.prime.oficina.servico.application.dto.ServicoRequest;
import br.com.prime.oficina.servico.application.dto.ServicoResponse;
import br.com.prime.oficina.servico.application.usecase.ServicoUseCase;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class ServicoControllerTest extends ControllerIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ServicoUseCase service;

    @Test
    void testCriar() throws Exception {
        ServicoRequest request = criarServicoRequest();

        when(service.criar(request)).thenReturn(criarServico());

        mockMvc.perform(post("/servicos")
                        .with(csrf())
                        .header("Authorization", bearerTokenAdmin(jwtService))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void testListar() throws Exception {
        when(service.listar()).thenReturn(List.of(criarServico()));

        mockMvc.perform(get("/servicos")
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testBuscarPorId() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(criarServico());

        mockMvc.perform(get("/servicos/{id}", 1L)
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testAtualizar() throws Exception {
        ServicoRequest request = criarServicoRequest();

        when(service.atualizar(1L, request)).thenReturn(criarServico());

        mockMvc.perform(put("/servicos/{id}", 1L)
                        .with(csrf())
                        .header("Authorization", bearerTokenAdmin(jwtService))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testInativar() throws Exception {
        doNothing().when(service).inativar(1L);

        mockMvc.perform(delete("/servicos/{id}", 1L)
                        .with(csrf())
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isNoContent());

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