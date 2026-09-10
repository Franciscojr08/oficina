package br.com.prime.oficina.shared.observabilidade;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    void limparContexto() {
        MDC.clear();
    }

    @Test
    @DisplayName("herda o identificador enviado pelo API Gateway")
    void herdaOIdentificadorRecebido() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/clientes");
        request.addHeader(CorrelationIdFilter.CABECALHO, "trace-abc-123");

        MockHttpServletResponse response = new MockHttpServletResponse();

        String[] visto = new String[1];

        filter.doFilter(request, response, (req, res) ->
                visto[0] = MDC.get(CorrelationIdFilter.CHAVE_DE_LOG));

        assertThat(visto[0]).isEqualTo("trace-abc-123");
        assertThat(response.getHeader(CorrelationIdFilter.CABECALHO)).isEqualTo("trace-abc-123");
    }

    @Test
    @DisplayName("gera um identificador quando a chamada nao passa pelo Gateway")
    void geraQuandoNaoRecebe() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/clientes");
        MockHttpServletResponse response = new MockHttpServletResponse();

        String[] visto = new String[1];

        filter.doFilter(request, response, (req, res) ->
                visto[0] = MDC.get(CorrelationIdFilter.CHAVE_DE_LOG));

        assertThat(visto[0]).isNotBlank();
        assertThat(UUID.fromString(visto[0])).isNotNull();
        assertThat(response.getHeader(CorrelationIdFilter.CABECALHO)).isEqualTo(visto[0]);
    }

    @Test
    @DisplayName("ignora cabecalho em branco e gera um identificador")
    void ignoraCabecalhoEmBranco() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/clientes");
        request.addHeader(CorrelationIdFilter.CABECALHO, "   ");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> { });

        assertThat(response.getHeader(CorrelationIdFilter.CABECALHO)).isNotBlank();
        assertThat(response.getHeader(CorrelationIdFilter.CABECALHO)).isNotEqualTo("   ");
    }

    @Test
    @DisplayName("corta um cabecalho longo demais para nao poluir cada linha de log")
    void cortaCabecalhoLongo() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/clientes");
        request.addHeader(CorrelationIdFilter.CABECALHO, "x".repeat(500));

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> { });

        assertThat(response.getHeader(CorrelationIdFilter.CABECALHO)).hasSize(128);
    }

    @Test
    @DisplayName("limpa o contexto ao final para nao vazar para a proxima requisicao")
    void limpaOContextoAoFinal() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/clientes");
        request.addHeader(CorrelationIdFilter.CABECALHO, "trace-abc-123");

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> { });

        assertThat(MDC.get(CorrelationIdFilter.CHAVE_DE_LOG)).isNull();
    }

    @Test
    @DisplayName("limpa o contexto mesmo quando a requisicao falha")
    void limpaOContextoQuandoFalha() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/clientes");

        FilterChain chain = mock(FilterChain.class);

        try {
            doAnswer(invocacao -> {
                throw new IllegalStateException("falha no meio da cadeia");
            }).when(chain).doFilter(any(), any());

            filter.doFilter(request, new MockHttpServletResponse(), chain);
        } catch (Exception esperada) {
            // A falha e o proposito do teste.
        }

        assertThat(MDC.get(CorrelationIdFilter.CHAVE_DE_LOG)).isNull();
    }

    @Test
    @DisplayName("nao registra o documento presente na rota")
    void naoRegistraODocumentoDaRota() throws Exception {
        ListAppender<ILoggingEvent> linhas = capturarLog();

        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/clientes/documento/52998224725");

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> { });

        String registrado = linhas.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .reduce("", (a, b) -> a + b);

        assertThat(registrado).doesNotContain("52998224725");
        assertThat(registrado).contains("/clientes/documento/***");
    }

    private ListAppender<ILoggingEvent> capturarLog() {
        LoggerContext contexto = (LoggerContext) LoggerFactory.getILoggerFactory();

        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.setContext(contexto);
        appender.start();

        ch.qos.logback.classic.Logger logger = contexto.getLogger(CorrelationIdFilter.class);
        logger.setLevel(Level.INFO);
        logger.addAppender(appender);

        return appender;
    }

    @Test
    @DisplayName("continua a cadeia de filtros")
    void continuaACadeia() throws Exception {
        FilterChain chain = mock(FilterChain.class);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/clientes");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}
