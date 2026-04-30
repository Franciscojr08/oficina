package br.com.prime.oficina.cliente.controller;

import br.com.prime.oficina.cliente.application.ClienteRequest;
import br.com.prime.oficina.cliente.application.ClienteResponse;
import br.com.prime.oficina.cliente.application.ClienteService;
import br.com.prime.oficina.config.ControllerIntegrationTestSupport;
import br.com.prime.oficina.config.IntegrationTest;
import br.com.prime.oficina.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
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
class ClienteControllerTest extends ControllerIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @MockitoBean
    private ClienteService service;

    @Test
    void testCriar() throws Exception {
        ClienteRequest request = criarClienteRequest();

        when(service.criar(request)).thenReturn(criarCliente());

        mockMvc.perform(post("/clientes")
                        .header("Authorization", bearerTokenAdmin(jwtService))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void testListar() throws Exception {
        when(service.listar()).thenReturn(List.of(criarCliente()));

        mockMvc.perform(get("/clientes")
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testBuscarPorId() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(criarCliente());

        mockMvc.perform(get("/clientes/{id}", 1L)
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testAtualizar() throws Exception {
        ClienteRequest request = criarClienteRequest();

        when(service.atualizar(1L, request)).thenReturn(criarCliente());

        mockMvc.perform(put("/clientes/{id}", 1L)
                        .header("Authorization", bearerTokenAdmin(jwtService))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testBuscarPorDocumento() throws Exception {
        when(service.findByCpfCnpj("12345678901")).thenReturn(criarCliente());

        mockMvc.perform(get("/clientes/documento/{documento}", "12345678901")
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testInativar() throws Exception {
        doNothing().when(service).inativar(1L);

        mockMvc.perform(delete("/clientes/{id}", 1L)
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(service, times(1)).inativar(1L);
    }

    private ClienteResponse criarCliente() {
        return new ClienteResponse(
                1L,
                "NomeTeste",
                "12345678901",
                "54989898989",
                "email@email.com",
                "cep",
                "Rua B",
                "Petropolis",
                "Higienopolis",
                "MG",
                LocalDate.of(1999, 6, 26),
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private ClienteRequest criarClienteRequest() {
        return new ClienteRequest(
                "João da Silva",
                "12345678901",
                "85999999999",
                "joao@email.com",
                "60000000",
                "Rua A",
                "Centro",
                "Fortaleza",
                "CE",
                LocalDate.of(1990, 1, 1)
        );
    }
}