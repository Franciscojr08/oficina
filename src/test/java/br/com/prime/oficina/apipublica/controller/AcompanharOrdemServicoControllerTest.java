package br.com.prime.oficina.apipublica.controller;

import br.com.prime.oficina.apipublica.AcompanharOrdemServicoPublicaResponse;
import br.com.prime.oficina.apipublica.AcompanharOrdemServicoPublicaService;
import br.com.prime.oficina.config.ControllerIntegrationTestSupport;
import br.com.prime.oficina.config.IntegrationTest;
import br.com.prime.oficina.ordemservico.application.StatusOrdemServico;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class AcompanharOrdemServicoControllerTest extends ControllerIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AcompanharOrdemServicoPublicaService service;

    @Test
    void testAcompanharPorCodigo() throws Exception {
        when(service.acompanharPorCodigo(anyString())).thenReturn(criarResponse());

        mockMvc.perform(get("/public/ordens/codigo/{codigo}", "codigo"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    private AcompanharOrdemServicoPublicaResponse criarResponse() {
        return new AcompanharOrdemServicoPublicaResponse(
                "response",
                "descricaoProblema",
                StatusOrdemServico.RECEBIDA,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
