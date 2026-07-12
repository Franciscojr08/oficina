package br.com.prime.oficina.ordemservico.application;

import br.com.prime.oficina.ordemservico.application.dto.*;

import br.com.prime.oficina.ordemservico.domain.OrdemServico;
import org.springframework.stereotype.Component;

@Component
public class OrdemServicoMapper {

	public OrdemServicoResponse toResponse(OrdemServico ordemServico) {
		return new OrdemServicoResponse(
			ordemServico.getId(),
			ordemServico.getCodigo(),
			ordemServico.getDescricaoProblema(),
			ordemServico.getObservacoesGerais(),
			ordemServico.getDescricaoServicosExecutados(),
			ordemServico.getStatus(),
			ordemServico.getValorTotalServicos(),
			ordemServico.getValorTotalItens(),
			ordemServico.getDataCadastro(),
			ordemServico.getDataEnvioAprovacao(),
			ordemServico.getDataAprovacao(),
			ordemServico.getDataInicioExecucao(),
			ordemServico.getDataFimExecucao(),
			ordemServico.getDataEntregue(),
			ordemServico.getDataCancelada()
		);
	}
}
