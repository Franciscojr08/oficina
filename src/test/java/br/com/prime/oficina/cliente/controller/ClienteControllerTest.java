package br.com.prime.oficina.cliente.controller;

import br.com.prime.oficina.cliente.application.ClienteRequest;
import br.com.prime.oficina.cliente.application.ClienteResponse;
import br.com.prime.oficina.cliente.application.ClienteService;
import br.com.prime.oficina.config.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static br.com.prime.oficina.util.JsonStringUtil.toJson;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
public class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService service;

    @Test
    void testCriar() throws Exception {
        when(service.criar(criarClienteRequest())).thenReturn(criarCliente());

        mockMvc.perform(post("/oficina/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(criarClienteRequest())))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void testListar() throws Exception {
        when(service.listar()).thenReturn(List.of(criarCliente()));

        mockMvc.perform(get("/oficina/v1/clientes"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testBuscarPorId() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(criarCliente());

        mockMvc.perform(get("/oficina/v1/clientes/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testAtualizar() throws Exception {
        when(service.atualizar(1L, criarClienteRequest())).thenReturn((criarCliente()));

        mockMvc.perform(put("/oficina/v1/clientes/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(criarClienteRequest())))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testBuscarPorDocumento() throws Exception {
        when(service.findByCpfCnpj("12345678901")).thenReturn((criarCliente()));

        mockMvc.perform(get("/oficina/v1/clientes/documento/{documento}", "12345678901"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testInativar() throws Exception {
        doNothing().when(service).inativar(1L);

        mockMvc.perform(delete("/oficina/v1/clientes/{id}", 1L))
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
                LocalDate.of(1999, 6,26),
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
