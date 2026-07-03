package br.com.prime.oficina.ordemservico.application;

import br.com.prime.oficina.ordemservico.domain.OrdemServico;
import br.com.prime.oficina.ordemservico.infrastructure.OrdemServicoRepository;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static br.com.prime.oficina.shared.exception.ExceptionMessage.INVALID_STATUS_FOR_MODIFICATION;

@Service
@RequiredArgsConstructor
public class OrdemServicoStatusService {

	private final OrdemServicoRepository ordemServicoRepository;
	private final HistoricoOrdemServicoService historicoOrdemServicoService;

	public void validarStatus(OrdemServico ordemServico, StatusOrdemServico statusEsperado, String acao) {
		if (ordemServico.getStatus() != statusEsperado) {
			throw new RegraNegocioException(
					"Não é possível %s, pois a ordem de serviço está %s"
							.formatted(acao, ordemServico.getStatus().getDescricao())
			);
		}
	}

	public void atualizarStatus(OrdemServico ordemServico, StatusOrdemServico status) {
		ordemServico.setStatus(status);

		if (status.deveAtualizarDatas()) {
			aplicarRegrasDeStatus(ordemServico, status);
		}

		ordemServicoRepository.saveAndFlush(ordemServico);
		historicoOrdemServicoService.registrar(ordemServico, status);
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
