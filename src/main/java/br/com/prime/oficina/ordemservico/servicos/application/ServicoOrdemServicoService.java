package br.com.prime.oficina.ordemservico.servicos.application;

import br.com.prime.oficina.ordemservico.application.OrdemServicoService;
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

	private final OrdemServicoService ordemServicoService;

	private final OrdemServicoRepository ordemServicoRepository;
	private final ServicoRepository servicoRepository;
	private final ServicoOrdemServicoRepository servicoOrdemServicoRepository;

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
	public ListaServicosOrdemServicoResponse adicionarServico(Long id, ServicoOrdemServicoRequest request) {
		OrdemServico ordemServico = buscarOrdemServicoPorId(id);

		if (ordemServico.getStatus().estaEmEdicao()) {
			String mensagem = String.format(
					"Não é possível adicionar o serviço, pois a ordem de serviço está %s",
					ordemServico.getStatus().getDescricao()
			);
			throw new RegraNegocioException(mensagem);
		}

		Servico servico = buscarServicoPorId(request.servicoId());
		if (!servico.getAtivo()) {
			throw new RegraNegocioException(NOT_ACTIVE_SERVICE);
		}

		ServicoOrdemServico servicoOrdemServico = new ServicoOrdemServico();
		preencherServicoOrdemServico(servicoOrdemServico, ordemServico, servico);

		servicoOrdemServicoRepository.save(servicoOrdemServico);

		BigDecimal novoTotal = getValorTotalServicos(ordemServico, servicoOrdemServico);
		ordemServico.setValorTotalServicos(novoTotal);

		ordemServicoRepository.saveAndFlush(ordemServico);

		return listarServicosPorOrdemServico(id);
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

		ordemServicoService.validarStatus(ordemServico, StatusOrdemServico.EM_EXECUCAO, "iniciar o serviço");

		ServicoOrdemServico servicoOS = servicoOrdemServicoRepository.findByOrdemServicoIdAndServicoId(id, servicoId);

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

		ordemServicoService.validarStatus(ordemServico, StatusOrdemServico.EM_EXECUCAO, "iniciar o serviço");

		ServicoOrdemServico servicoOS = servicoOrdemServicoRepository.findByOrdemServicoIdAndServicoId(id, servicoId);

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
			ordemServicoService.atualizarStatus(ordemServico, StatusOrdemServico.FINALIZADA);
		}
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
