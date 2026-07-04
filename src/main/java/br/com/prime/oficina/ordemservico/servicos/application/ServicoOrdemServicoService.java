package br.com.prime.oficina.ordemservico.servicos.application;

import br.com.prime.oficina.ordemservico.application.OrdemServicoStatusService;
import br.com.prime.oficina.ordemservico.application.StatusOrdemServico;
import br.com.prime.oficina.ordemservico.domain.OrdemServico;
import br.com.prime.oficina.ordemservico.infrastructure.OrdemServicoRepository;
import br.com.prime.oficina.ordemservico.servicos.domain.ServicoOrdemServico;
import br.com.prime.oficina.ordemservico.servicos.infrastructure.ServicoOrdemServicoRepository;
import br.com.prime.oficina.servico.domain.Servico;
import br.com.prime.oficina.servico.infrasctucture.ServicoRepository;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static br.com.prime.oficina.shared.exception.ExceptionMessage.*;

@Service
@RequiredArgsConstructor
public class ServicoOrdemServicoService {

	private final OrdemServicoRepository ordemServicoRepository;
	private final ServicoRepository servicoRepository;
	private final ServicoOrdemServicoRepository servicoOrdemServicoRepository;
	private final OrdemServicoStatusService ordemServicoStatusService;

	public ListaServicosOrdemServicoResponse listarServicosPorOrdemServico(Long id) {
		List<ServicoOrdemServico> lista = servicoOrdemServicoRepository.findByOrdemServicoId(id);

		List<ServicoOrdemServicoResponse> servicos = lista.stream()
				.map(this::toServicoResponse)
				.toList();

		BigDecimal total = lista.stream()
				.map(ServicoOrdemServico::getValorUnitario)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		return new ListaServicosOrdemServicoResponse(servicos, total);
	}

	@Transactional
	public void adicionarServico(Long id, ServicoOrdemServicoRequest request) {
		OrdemServico ordemServico = buscarOrdemServicoPorId(id);

		String mensagem = String.format(
			CANNOT_ADD_SERVICE_WITH_ORDER_OUTSIDE_DIAGNOSIS,
			StatusOrdemServico.EM_DIAGNOSTICO.getDescricao()
		);
		ordemServicoStatusService.validarEmDiagnostico(ordemServico, mensagem);

		adicionarServicoNaOrdem(ordemServico, request.servicoId());
	}

	@Transactional
	public void adicionarServicoDuranteCadastro(OrdemServico ordemServico, Long servicoId) {
		adicionarServicoNaOrdem(ordemServico, servicoId);
	}

	private void adicionarServicoNaOrdem(OrdemServico ordemServico, Long servicoId) {
		Servico servico = buscarServicoPorId(servicoId);
		if (Boolean.FALSE.equals(servico.getAtivo())) {
			throw new RegraNegocioException(NOT_ACTIVE_SERVICE);
		}

		ServicoOrdemServico servicoOrdemServico = new ServicoOrdemServico();
		preencherServicoOrdemServico(servicoOrdemServico, ordemServico, servico);

		servicoOrdemServicoRepository.save(servicoOrdemServico);

		BigDecimal novoTotal = getValorTotalServicos(ordemServico, servicoOrdemServico);
		ordemServico.setValorTotalServicos(novoTotal);

		ordemServicoRepository.saveAndFlush(ordemServico);
	}

	private BigDecimal getValorTotalServicos(OrdemServico ordemServico, ServicoOrdemServico servicoOrdemServico) {
		BigDecimal valorServico = servicoOrdemServico.getValorUnitario();
		return ordemServico.getValorTotalServicos().add(valorServico);
	}

	private OrdemServico buscarOrdemServicoPorId(Long id) {
		return ordemServicoRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException(SERVICE_ORDER_NOT_FOUND));
	}

	private Servico buscarServicoPorId(Long id) {
		return servicoRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException(SERVICE_NOT_FOUND));
	}

	public ServicoOrdemServicoResponse iniciarServico(Long id, Long servicoId) {
		OrdemServico ordemServico = buscarOrdemServicoPorId(id);

		ordemServicoStatusService.validarPodeExecutarServico(ordemServico, "iniciar o serviço");

		ServicoOrdemServico servicoOS = servicoOrdemServicoRepository
				.findByOrdemServicoIdAndServicoId(id, servicoId)
				.orElseThrow(() -> new RegraNegocioException(SERVICE_NOT_FOUND_FOR_ORDER));

		if (servicoOS.getStatus() != StatusServico.PENDENTE) {
			throw new RegraNegocioException(STARTED_OR_FINISHED_SERVICE);
		}

		servicoOS.setStatus(StatusServico.INICIADO);
		servicoOS.setDataInicio(LocalDateTime.now());

		servicoOrdemServicoRepository.save(servicoOS);

		return toServicoResponse(servicoOS);
	}

	public ServicoOrdemServicoResponse finalizarServico(Long id, Long servicoId) {
		OrdemServico ordemServico = buscarOrdemServicoPorId(id);

		ordemServicoStatusService.validarPodeExecutarServico(ordemServico, "finalizar o serviço");

		ServicoOrdemServico servicoOS = servicoOrdemServicoRepository
				.findByOrdemServicoIdAndServicoId(id, servicoId)
				.orElseThrow(() -> new RegraNegocioException(SERVICE_NOT_FOUND_FOR_ORDER));

		if (servicoOS.getStatus() != StatusServico.INICIADO) {
			throw new RegraNegocioException(FINISHED_OR_CANCELED_SERVICE);
		}

		servicoOS.setStatus(StatusServico.FINALIZADO);
		servicoOS.setDataFim(LocalDateTime.now());

		servicoOrdemServicoRepository.save(servicoOS);

		checarServicosOrdemServico(ordemServico);

		return toServicoResponse(servicoOS);
	}

	private void checarServicosOrdemServico(OrdemServico ordemServico) {
		boolean existeNaoFinalizado = servicoOrdemServicoRepository.existsByOrdemServicoIdAndStatusNot(
				ordemServico.getId(),
				StatusServico.FINALIZADO
		);

		if (!existeNaoFinalizado) {
			finalizarOrdemServico(ordemServico);
		}
	}

	private void finalizarOrdemServico(OrdemServico ordemServico) {
		ordemServicoStatusService.finalizar(ordemServico);
	}

	private void preencherServicoOrdemServico(
			ServicoOrdemServico servicoOrdemServico,
			OrdemServico ordemServico,
			Servico servico
	) {
		servicoOrdemServico.setOrdemServico(ordemServico);
		servicoOrdemServico.setServico(servico);
		servicoOrdemServico.setValorUnitario(servico.getValor());
		servicoOrdemServico.setStatus(StatusServico.PENDENTE);
	}

	private ServicoOrdemServicoResponse toServicoResponse(ServicoOrdemServico servicoOS) {
		return new ServicoOrdemServicoResponse(
				servicoOS.getOrdemServico().getCodigo(),
				servicoOS.getServico().getId(),
				servicoOS.getServico().getNome(),
				servicoOS.getValorUnitario(),
				servicoOS.getStatus(),
				servicoOS.getDataCadastro(),
				servicoOS.getDataInicio(),
				servicoOS.getDataFim()
		);
	}
}
