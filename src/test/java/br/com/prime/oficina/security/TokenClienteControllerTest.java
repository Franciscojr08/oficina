package br.com.prime.oficina.security;

import br.com.prime.oficina.cliente.application.dto.ClienteResponse;
import br.com.prime.oficina.cliente.application.usecase.ClienteUseCase;
import br.com.prime.oficina.config.ControllerIntegrationTestSupport;
import br.com.prime.oficina.config.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Garante que o token emitido pela function serverless de autenticação por CPF é aceito nas rotas
 * protegidas. Antes desta mudança o filtro procurava o subject do token na tabela {@code usuario} e
 * qualquer token de cliente era descartado.
 */
@IntegrationTest
class TokenClienteControllerTest extends ControllerIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteUseCase clienteUseCase;

    @Test
    void deveAcessarRotaProtegidaComTokenDeCliente() throws Exception {
        when(clienteUseCase.listar()).thenReturn(List.of(criarCliente()));

        mockMvc.perform(get("/clientes")
                        .header("Authorization", bearerTokenCliente(7L, "52998224725", "Ana Souza")))
                .andExpect(status().isOk());
    }

    @Test
    void deveRecusarRotaProtegidaSemToken() throws Exception {
        mockMvc.perform(get("/clientes"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void deveRecusarTokenDeClienteAssinadoComOutroSegredo() throws Exception {
        mockMvc.perform(get("/clientes")
                        .header("Authorization", bearerTokenClienteComOutroSegredo(7L, "52998224725")))
                .andExpect(status().is4xxClientError());
    }

    private ClienteResponse criarCliente() {
        return new ClienteResponse(
                7L,
                "Ana Souza",
                "52998224725",
                "11999999999",
                "ana@email.com",
                "01001000",
                "Praca da Se",
                "Se",
                "Sao Paulo",
                "SP",
                LocalDate.of(1990, 1, 1),
                true,
                LocalDateTime.now(),
                null
        );
    }
}
