package br.com.prime.oficina.shared.observabilidade;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Coloca o identificador de correlação da requisição no contexto de log.
 *
 * <p>O valor vem do cabeçalho {@code x-correlation-id}, que o API Gateway preenche a partir do
 * authorizer. Herdar em vez de gerar é o que permite seguir uma mesma requisição do log do Gateway
 * até o log desta aplicação. Quando a chamada não passa pelo Gateway, o identificador é gerado
 * aqui, para que nenhuma requisição fique sem rastro.</p>
 *
 * <p>Roda antes de todos os outros filtros para que até uma falha de autenticação apareça no log
 * já correlacionada.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CABECALHO = "x-correlation-id";

    /** Nome da chave no MDC; vira um campo de mesmo nome em cada linha do log estruturado. */
    public static final String CHAVE_DE_LOG = "correlationId";

    /** Um cabeçalho absurdamente longo viraria lixo em toda linha de log. */
    private static final int TAMANHO_MAXIMO = 128;

    /**
     * Sequências longas de dígitos na URL são documentos: {@code /clientes/documento/{documento}}
     * carrega CPF ou CNPJ no caminho, e log não é lugar para dado pessoal em claro.
     */
    private static final Pattern DOCUMENTO_NA_ROTA = Pattern.compile("\\d{8,}");

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String correlationId = resolver(request);

        MDC.put(CHAVE_DE_LOG, correlationId);
        response.setHeader(CABECALHO, correlationId);

        long inicio = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            log.info(
                    "requisicao_atendida metodo={} rota={} status={} duracaoMs={}",
                    request.getMethod(),
                    rotaSemDocumento(request),
                    response.getStatus(),
                    System.currentTimeMillis() - inicio
            );

            // Sem isso o valor vaza para a próxima requisição atendida pela mesma thread.
            MDC.remove(CHAVE_DE_LOG);
        }
    }

    private String rotaSemDocumento(HttpServletRequest request) {
        String rota = request.getRequestURI();

        return DOCUMENTO_NA_ROTA.matcher(rota).replaceAll("***");
    }

    private String resolver(HttpServletRequest request) {
        String recebido = request.getHeader(CABECALHO);

        if (recebido == null || recebido.isBlank()) {
            return UUID.randomUUID().toString();
        }

        String limpo = recebido.trim();

        return limpo.length() > TAMANHO_MAXIMO ? limpo.substring(0, TAMANHO_MAXIMO) : limpo;
    }
}
