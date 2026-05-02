package br.com.prime.oficina.relatorio.controller;

import br.com.prime.oficina.config.ControllerIntegrationTestSupport;
import br.com.prime.oficina.config.IntegrationTest;
import br.com.prime.oficina.relatorio.application.RelatorioResponse;
import br.com.prime.oficina.relatorio.application.RelatorioService;
import br.com.prime.oficina.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class RelatorioControllerTest extends ControllerIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private RelatorioService service;

    @Test
    void testCalcularTempoMedioOS() throws Exception {
        when(service.calcularTempoMedioOS()).thenReturn(criarRelatorioResponse());

        mockMvc.perform(get("/relatorios/ordens-servico/tempo-medio")
                        .with(csrf())
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testCalcularTempoMedioServico() throws Exception {
        when(service.calcularTempoMedioServicos()).thenReturn(criarRelatorioResponse());

        mockMvc.perform(get("/relatorios/ordens-servico/tempo-medio-servicos")
                        .with(csrf())
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    private RelatorioResponse criarRelatorioResponse() {
        return new RelatorioResponse(
                2D,
                "2 horas"
        );
    }
}
