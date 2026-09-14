package br.com.prime.oficina.ordemservico.application;

import br.com.prime.oficina.ordemservico.application.dto.*;

import br.com.prime.oficina.ordemservico.domain.OrdemServico;
import br.com.prime.oficina.ordemservico.application.gateway.OrdemServicoGateway;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
import br.com.prime.oficina.shared.observabilidade.OrdemServicoObservabilidade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static br.com.prime.oficina.shared.exception.ExceptionMessage.INVALID_ORDER_STATUS_FOR_ACTION;
import static br.com.prime.oficina.shared.exception.ExceptionMessage.INVALID_STATUS_FOR_MODIFICATION;

@Service
@RequiredArgsConstructor
public class OrdemServicoStatusService {

	private final OrdemServicoGateway ordemServicoRepository;
	private final HistoricoOrdemServicoService historicoOrdemServicoService;
	private final OrdemServicoObservabilidade ordemServicoObservabilidade;

	public void definirStatusInicial(OrdemServico ordemServico) {
		// Não registra o evento de observabilidade aqui: a OS ainda não tem id/código nesse ponto
		// (são atribuídos no INSERT — id por sequence, código por trigger). Quem registra a criação
		// é o chamador, depois do saveAndFlush + refresh (ver OrdemServicoService.criar).
		ordemServico.setStatus(StatusOrdemServico.RECEBIDA);
	}

	public void validarEmDiagnostico(OrdemServico ordemServico, String mensagem) {
		if (!ordemServico.getStatus().estaEmDiagnostico()) {
			throw new RegraNegocioException(mensagem);
		}
	}

	public void iniciarDiagnostico(OrdemServico ordemServico) {
		transicionar(ordemServico, StatusOrdemServico.RECEBIDA, StatusOrdemServico.EM_DIAGNOSTICO, "iniciar diagnóstico");
	}

	public void solicitarAprovacao(OrdemServico ordemServico) {
		transicionar(
				ordemServico,
				StatusOrdemServico.EM_DIAGNOSTICO,
				StatusOrdemServico.AGUARDANDO_APROVACAO,
				"solicitar aprovação"
		);
	}

	public void aprovar(OrdemServico ordemServico) {
		transicionar(
				ordemServico,
				StatusOrdemServico.AGUARDANDO_APROVACAO,
				StatusOrdemServico.APROVADA,
				"aprovar a ordem de serviço"
		);
	}

	public void validarPodeReprovar(OrdemServico ordemServico) {
		validarStatus(ordemServico, StatusOrdemServico.AGUARDANDO_APROVACAO, "reprovar a ordem de serviço");
	}

	public void validarPodeIniciarExecucao(OrdemServico ordemServico) {
		validarStatus(ordemServico, StatusOrdemServico.AGUARDANDO_ITENS, "iniciar execução");
	}

	public void validarPodeExecutarServico(OrdemServico ordemServico, String acao) {
		validarStatus(ordemServico, StatusOrdemServico.EM_EXECUCAO, acao);
	}

	public void aguardarItens(OrdemServico ordemServico) {
		if (ordemServico.getStatus() == StatusOrdemServico.AGUARDANDO_ITENS) {
			return;
		}

		transicionar(ordemServico, StatusOrdemServico.APROVADA, StatusOrdemServico.AGUARDANDO_ITENS, "aguardar itens");
	}

	public void iniciarExecucao(OrdemServico ordemServico) {
		validarStatus(
				ordemServico,
				List.of(StatusOrdemServico.APROVADA, StatusOrdemServico.AGUARDANDO_ITENS),
				"iniciar execução"
		);
		atualizarStatus(ordemServico, StatusOrdemServico.EM_EXECUCAO);
	}

	public void cancelarPorReprovacao(OrdemServico ordemServico) {
		transicionar(
				ordemServico,
				StatusOrdemServico.AGUARDANDO_APROVACAO,
				StatusOrdemServico.CANCELADA,
				"reprovar a ordem de serviço"
		);
	}

	public void finalizar(OrdemServico ordemServico) {
		transicionar(ordemServico, StatusOrdemServico.EM_EXECUCAO, StatusOrdemServico.FINALIZADA, "finalizar a ordem de serviço");
	}

	public void entregar(OrdemServico ordemServico) {
		transicionar(ordemServico, StatusOrdemServico.FINALIZADA, StatusOrdemServico.ENTREGUE, "entregar");
	}

	private void transicionar(
			OrdemServico ordemServico,
			StatusOrdemServico statusAtualPermitido,
			StatusOrdemServico novoStatus,
			String acao
	) {
		validarStatus(ordemServico, statusAtualPermitido, acao);
		atualizarStatus(ordemServico, novoStatus);
	}

	private void validarStatus(OrdemServico ordemServico, StatusOrdemServico statusEsperado, String acao) {
		validarStatus(ordemServico, List.of(statusEsperado), acao);
	}

	private void validarStatus(OrdemServico ordemServico, List<StatusOrdemServico> statusPermitidos, String acao) {
		if (!statusPermitidos.contains(ordemServico.getStatus())) {
			String motivo = INVALID_ORDER_STATUS_FOR_ACTION
					.formatted(acao, ordemServico.getStatus().getDescricao());

			ordemServicoObservabilidade.registrarTransicaoInvalida(ordemServico, acao, motivo);

			throw new RegraNegocioException(motivo);
		}
	}

	private void atualizarStatus(OrdemServico ordemServico, StatusOrdemServico status) {
		StatusOrdemServico statusAnterior = ordemServico.getStatus();
		ordemServico.setStatus(status);

		if (status.deveAtualizarDatas()) {
			aplicarRegrasDeStatus(ordemServico, status);
		}

		ordemServicoRepository.saveAndFlush(ordemServico);
		historicoOrdemServicoService.registrar(ordemServico, status);

		ordemServicoObservabilidade.registrarMudancaStatus(ordemServico, statusAnterior, status);
		registrarDuracaoDeEtapa(ordemServico, status);
	}

	/**
	 * Alimenta o dashboard de "tempo médio de execução por status" pedido no desafio, mapeando as
	 * três etapas nomeadas (Diagnóstico, Execução, Finalização) pras datas que a própria entidade já
	 * mantém — sem precisar de nenhuma tabela ou consulta nova.
	 */
	private void registrarDuracaoDeEtapa(OrdemServico ordemServico, StatusOrdemServico status) {
		switch (status) {
			case AGUARDANDO_APROVACAO -> ordemServicoObservabilidade.registrarDuracaoEtapa(
					ordemServico, "DIAGNOSTICO", ordemServico.getDataCadastro(), ordemServico.getDataEnvioAprovacao()
			);
			case FINALIZADA -> ordemServicoObservabilidade.registrarDuracaoEtapa(
					ordemServico, "EXECUCAO", ordemServico.getDataInicioExecucao(), ordemServico.getDataFimExecucao()
			);
			case ENTREGUE -> ordemServicoObservabilidade.registrarDuracaoEtapa(
					ordemServico, "FINALIZACAO", ordemServico.getDataFimExecucao(), ordemServico.getDataEntregue()
			);
			// Demais status (RECEBIDA, EM_DIAGNOSTICO, AGUARDANDO_APROVACAO, APROVADA,
			// AGUARDANDO_ITENS, CANCELADA) não têm etapa de duração mapeada pro dashboard.
			default -> { /* intencional: nada a registrar */ }
		}
	}

	private void aplicarRegrasDeStatus(OrdemServico ordemServico, StatusOrdemServico status) {
		switch (status) {
			case AGUARDANDO_APROVACAO -> ordemServico.setDataEnvioAprovacao(LocalDateTime.now());
			case APROVADA -> ordemServico.setDataAprovacao(LocalDateTime.now());
			case EM_EXECUCAO -> ordemServico.setDataInicioExecucao(LocalDateTime.now());
			case FINALIZADA -> ordemServico.setDataFimExecucao(LocalDateTime.now());
			case ENTREGUE -> ordemServico.setDataEntregue(LocalDateTime.now());
			case CANCELADA -> ordemServico.setDataCancelada(LocalDateTime.now());
			default -> throw new RegraNegocioException(INVALID_STATUS_FOR_MODIFICATION);
		}
	}
}
