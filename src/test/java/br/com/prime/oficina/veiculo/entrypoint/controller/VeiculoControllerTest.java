package br.com.prime.oficina.veiculo.entrypoint.controller;

import br.com.prime.oficina.config.ControllerIntegrationTestSupport;
import br.com.prime.oficina.config.IntegrationTest;
import br.com.prime.oficina.security.JwtService;
import br.com.prime.oficina.veiculo.application.dto.VeiculoRequest;
import br.com.prime.oficina.veiculo.application.dto.VeiculoResponse;
import br.com.prime.oficina.veiculo.application.usecase.VeiculoUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class VeiculoControllerTest extends ControllerIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private VeiculoUseCase service;

    @Test
    void testCriar() throws Exception {
        VeiculoRequest request = criarVeiculoRequest();

        when(service.criar(request)).thenReturn(criarVeiculo());

        mockMvc.perform(post("/veiculos")
                        .with(csrf())
                        .header("Authorization", bearerTokenAdmin(jwtService))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void testListar() throws Exception {
        when(service.listar()).thenReturn(List.of(criarVeiculo()));

        mockMvc.perform(get("/veiculos")
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testBuscarPorId() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(criarVeiculo());

        mockMvc.perform(get("/veiculos/{id}", 1L)
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testListarPorCliente() throws Exception {
        when(service.listarPorCliente(1L)).thenReturn(List.of(criarVeiculo()));

        mockMvc.perform(get("/veiculos/cliente/{clienteId}", 1L)
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testAtualizar() throws Exception {
        VeiculoRequest request = criarVeiculoRequest();

        when(service.atualizar(1L, request)).thenReturn(criarVeiculo());

        mockMvc.perform(put("/veiculos/{id}", 1L)
                        .with(csrf())
                        .header("Authorization", bearerTokenAdmin(jwtService))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testBuscarPorPlaca() throws Exception {
        when(service.buscarPorPlaca("PLACANOVA")).thenReturn(criarVeiculo());

        mockMvc.perform(get("/veiculos/placa/{placa}", "PLACANOVA")
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testInativar() throws Exception {
        doNothing().when(service).inativar(1L);

        mockMvc.perform(delete("/veiculos/{id}", 1L)
                        .with(csrf())
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isNoContent());
    }



    private VeiculoRequest criarVeiculoRequest() {
        return new VeiculoRequest(
                1L,
                "PLACANOVA",
                "BYD",
                "SEAL",
                2026,
                "BRANCO",
                "Veiculo em bom estado"
        );
    }

    private VeiculoResponse criarVeiculo() {
        return new VeiculoResponse(
                1L,
                1L,
                "PLACANOVA",
                "BYD",
                "SEAL",
                2026,
                "BRANCO",
                "Veiculo em bom estado",
                Boolean.TRUE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

}
