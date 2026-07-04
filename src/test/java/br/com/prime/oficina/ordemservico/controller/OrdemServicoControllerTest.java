package br.com.prime.oficina.ordemservico.controller;

import br.com.prime.oficina.config.ControllerIntegrationTestSupport;
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
import br.com.prime.oficina.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class OrdemServicoControllerTest extends ControllerIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private OrdemServicoService ordemServicoService;

    @MockitoBean
    private ItemOrdemServicoService itemOrdemServicoService;

    @MockitoBean
    private ServicoOrdemServicoService servicoOrdemServicoService;

    @Test
    void deveListarOrdensServico() throws Exception {
        when(ordemServicoService.listar()).thenReturn(List.of(criarOrdemServico()));

        mockMvc.perform(get("/ordens")
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1L))
				.andExpect(jsonPath("$[0].codigo").value("Codigo"))
				.andExpect(jsonPath("$[0].status").value(StatusOrdemServico.RECEBIDA.name()));
    }

    @Test
    void deveListarOrdensServicoPorCliente() throws Exception {
        when(ordemServicoService.listarPorCliente(1L)).thenReturn(List.of(criarOrdemServico()));

        mockMvc.perform(get("/ordens/cliente/{id}", 1L)
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void deveListarOrdensServicoPorCodigo() throws Exception {
        when(ordemServicoService.listarPorCodigo("codigo")).thenReturn(List.of(criarOrdemServico()));

        mockMvc.perform(get("/ordens/codigo/{codigo}", "codigo")
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk())
				.andExpect(jsonPath("$[0].codigo").value("Codigo"));
    }

    @Test
    void deveListarOrdensServicoPorStatus() throws Exception {
        when(ordemServicoService.listarPorStatus(StatusOrdemServico.ENTREGUE))
                .thenReturn(List.of(criarOrdemServico()));

        mockMvc.perform(get("/ordens/status/{status}", StatusOrdemServico.ENTREGUE)
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk())
				.andExpect(jsonPath("$[0].status").value(StatusOrdemServico.RECEBIDA.name()));
    }

    @Test
    void deveCriarOrdemServicoComSucesso() throws Exception {
        OrdemServicoRequest request = criarOrdemServicoRequest();

        when(ordemServicoService.criar(request)).thenReturn(criarOrdemServico());

        mockMvc.perform(post("/ordens")
                        .header("Authorization", bearerTokenAdmin(jwtService))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.codigo").value("Codigo"))
				.andExpect(jsonPath("$.status").value(StatusOrdemServico.RECEBIDA.name()));
    }

    @Test
    void naoDeveCriarOrdemServicoQuandoServicosEstiverVazio() throws Exception {
		OrdemServicoRequest request = new OrdemServicoRequest(
				"Descricao",
				"Observacoes",
				"Descricao",
				1L,
				2L,
				List.of(),
				List.of()
		);

		mockMvc.perform(post("/ordens")
						.header("Authorization", bearerTokenAdmin(jwtService))
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("servicos: É necessário informar pelo menos um serviço"));

		verify(ordemServicoService, never()).criar(any(OrdemServicoRequest.class));
	}

    @Test
    void deveAtualizarOrdemServicoComSucesso() throws Exception {
        OrdemServicoRequest request = criarOrdemServicoRequest();

        when(ordemServicoService.atualizar(1L, request))
                .thenReturn(criarOrdemServico());

        mockMvc.perform(put("/ordens/{id}", 1L)
                        .with(csrf())
                        .header("Authorization", bearerTokenAdmin(jwtService))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.codigo").value("Codigo"));
    }

    @Test
    void deveAdicionarItemNaOrdemServico() throws Exception {
        ItemOrdemServicoRequest request = criarItemOrdemServicoRequest();

		doNothing().when(itemOrdemServicoService)
				.adicionarItem(1L, request);

		when(itemOrdemServicoService.listarItensPorOrdemServico(1L))
				.thenReturn(criarListaItensOrdemServicoResponse());

        mockMvc.perform(post("/ordens/{id}/itens", 1L)
                        .with(csrf())
                        .header("Authorization", bearerTokenAdmin(jwtService))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
				.andExpect(jsonPath("$.itens[0].itemId").value(1L))
				.andExpect(jsonPath("$.valorTotalItens").value(10));
    }

    @Test
    void naoDeveAdicionarItemQuandoQuantidadeNaoForPositiva() throws Exception {
		ItemOrdemServicoRequest request = new ItemOrdemServicoRequest(1L, 0);

		mockMvc.perform(post("/ordens/{id}/itens", 1L)
						.with(csrf())
						.header("Authorization", bearerTokenAdmin(jwtService))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("quantidade: Quantidade deve ser maior que zero"));

		verify(itemOrdemServicoService, never()).adicionarItem(1L, request);
	}

    @Test
    void deveListarItensPorOrdemServico() throws Exception {
        when(itemOrdemServicoService.listarItensPorOrdemServico(1L))
                .thenReturn(criarListaItensOrdemServicoResponse());

        mockMvc.perform(get("/ordens/{id}/itens", 1L)
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk())
				.andExpect(jsonPath("$.itens[0].itemId").value(1L))
				.andExpect(jsonPath("$.valorTotalItens").value(10));
    }

    @Test
    void deveAdicionarServicoNaOrdemServico() throws Exception {
        ServicoOrdemServicoRequest request = criarServicoOrdemServicoRequest();

		doNothing().when(servicoOrdemServicoService)
				.adicionarServico(1L, request);

		when(servicoOrdemServicoService.listarServicosPorOrdemServico(1L))
				.thenReturn(criarListaServicosOrdemServicoResponse());

        mockMvc.perform(post("/ordens/{id}/servicos", 1L)
                        .with(csrf())
                        .header("Authorization", bearerTokenAdmin(jwtService))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
				.andExpect(jsonPath("$.servicos[0].servicoId").value(1L))
				.andExpect(jsonPath("$.valorTotalServicos").value(10));
    }

    @Test
    void deveListarServicosPorOrdemServico() throws Exception {
        when(servicoOrdemServicoService.listarServicosPorOrdemServico(1L))
                .thenReturn(criarListaServicosOrdemServicoResponse());

        mockMvc.perform(get("/ordens/{id}/servicos", 1L)
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk())
				.andExpect(jsonPath("$.servicos[0].servicoId").value(1L))
				.andExpect(jsonPath("$.valorTotalServicos").value(10));
    }

    @Test
    void deveAprovarOrdemServico() throws Exception {
        when(ordemServicoService.aprovarOrdemServico(1L)).thenReturn(criarOrdemServico());

        mockMvc.perform(patch("/ordens/{id}/aprovar", 1L)
                        .with(csrf())
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void deveReprovarOrdemServico() throws Exception {
        when(ordemServicoService.reprovarOrdemServico(1L)).thenReturn(criarOrdemServico());

        mockMvc.perform(patch("/ordens/{id}/reprovar", 1L)
                        .with(csrf())
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void deveIniciarDiagnosticoDaOrdemServico() throws Exception {
        when(ordemServicoService.iniciarDiagnostico(1L)).thenReturn(criarOrdemServico());

        mockMvc.perform(patch("/ordens/{id}/iniciar-diagnostico", 1L)
                        .with(csrf())
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void deveSolicitarAprovacaoDaOrdemServico() throws Exception {
        when(ordemServicoService.solicitarAprovacao(1L)).thenReturn(criarOrdemServico());

        mockMvc.perform(patch("/ordens/{id}/solicitar-aprovacao", 1L)
                        .with(csrf())
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void deveIniciarServicoDaOrdemServico() throws Exception {
        when(servicoOrdemServicoService.iniciarServico(1L, 2L))
                .thenReturn(criarServicoOrdemServicoResponse());

        mockMvc.perform(patch("/ordens/{id}/servicos/{servicoId}/iniciar", 1L, 2L)
                        .with(csrf())
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk())
				.andExpect(jsonPath("$.servicoId").value(1L))
				.andExpect(jsonPath("$.status").value(StatusServico.INICIADO.name()));
    }

    @Test
    void deveFinalizarServicoDaOrdemServico() throws Exception {
        when(servicoOrdemServicoService.finalizarServico(1L, 2L))
                .thenReturn(criarServicoOrdemServicoResponse());

        mockMvc.perform(patch("/ordens/{id}/servicos/{servicoId}/finalizar", 1L, 2L)
                        .with(csrf())
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk())
				.andExpect(jsonPath("$.servicoId").value(1L))
				.andExpect(jsonPath("$.status").value(StatusServico.INICIADO.name()));
    }

    @Test
    void deveEntregarOrdemServico() throws Exception {
        when(ordemServicoService.entregarOrdemServico(1L)).thenReturn(criarOrdemServico());

        mockMvc.perform(patch("/ordens/{id}/entregar", 1L)
                        .with(csrf())
                        .header("Authorization", bearerTokenAdmin(jwtService)))
                .andDo(print())
                .andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1L));
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
		List<Long> servicos = List.of(1L, 2L);
		List<ItemOrdemServicoRequest> itens = List.of(
				new ItemOrdemServicoRequest(10L, 2),
				new ItemOrdemServicoRequest(20L, 1)
		);

        return new OrdemServicoRequest(
                "Descricao",
                "Observacoes",
                "Descricao",
                1L,
                2L,
				servicos,
				itens
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
