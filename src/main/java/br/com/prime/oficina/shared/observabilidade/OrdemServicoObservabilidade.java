package br.com.prime.oficina.shared.observabilidade;

import br.com.prime.oficina.ordemservico.application.StatusOrdemServico;
import br.com.prime.oficina.ordemservico.domain.OrdemServico;
import com.newrelic.api.agent.NewRelic;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Eventos customizados de negócio da Ordem de Serviço, enviados pro New Relic.
 *
 * <p>Existe separado do {@code OrdemServicoStatusService} porque essas chamadas não são regra de
 * negócio — são só telemetria. Isolar aqui deixa claro o que alimenta os dashboards pedidos no
 * desafio (volume diário de OS, tempo médio por status, erros de processamento) e facilita trocar
 * de ferramenta de observabilidade no futuro sem tocar no fluxo de negócio.</p>
 *
 * <p>Todas as chamadas de {@link NewRelic} viram no-op quando o agent Java não está anexado ao
 * processo (testes locais, ambiente sem instrumentação) — não há acoplamento rígido.</p>
 */
@Component
public class OrdemServicoObservabilidade {

	private static final String EVENTO_STATUS_ALTERADO = "OrdemServicoStatusAlterado";
	private static final String EVENTO_ETAPA_CONCLUIDA = "OrdemServicoEtapaConcluida";
	private static final String EVENTO_TRANSICAO_INVALIDA = "OrdemServicoTransicaoInvalida";

	public void registrarMudancaStatus(OrdemServico ordemServico, StatusOrdemServico statusAnterior, StatusOrdemServico statusNovo) {
		Map<String, Object> atributos = atributosBase(ordemServico);
		atributos.put("statusAnterior", statusAnterior == null ? "N/A" : statusAnterior.name());
		atributos.put("statusNovo", statusNovo.name());

		NewRelic.getAgent().getInsights().recordCustomEvent(EVENTO_STATUS_ALTERADO, atributos);
	}

	/**
	 * Duração de uma etapa nomeada do ciclo de vida da OS — é o que alimenta o dashboard de "tempo
	 * médio de execução por status" (Diagnóstico, Execução, Finalização) pedido no desafio.
	 */
	public void registrarDuracaoEtapa(OrdemServico ordemServico, String etapa, LocalDateTime inicio, LocalDateTime fim) {
		if (inicio == null || fim == null) {
			return;
		}

		Map<String, Object> atributos = atributosBase(ordemServico);
		atributos.put("etapa", etapa);
		atributos.put("duracaoMs", Duration.between(inicio, fim).toMillis());

		NewRelic.getAgent().getInsights().recordCustomEvent(EVENTO_ETAPA_CONCLUIDA, atributos);
	}

	public void registrarTransicaoInvalida(OrdemServico ordemServico, String acao, String motivo) {
		Map<String, Object> atributos = atributosBase(ordemServico);
		atributos.put("acao", acao);
		atributos.put("motivo", motivo);

		NewRelic.getAgent().getInsights().recordCustomEvent(EVENTO_TRANSICAO_INVALIDA, atributos);
	}

	private Map<String, Object> atributosBase(OrdemServico ordemServico) {
		Map<String, Object> atributos = new HashMap<>();
		atributos.put("ordemServicoId", ordemServico.getId());
		atributos.put("codigo", ordemServico.getCodigo());

		return atributos;
	}
}
