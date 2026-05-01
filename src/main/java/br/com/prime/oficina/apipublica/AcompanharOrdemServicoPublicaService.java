package br.com.prime.oficina.apipublica;

import br.com.prime.oficina.ordemservico.domain.OrdemServico;
import br.com.prime.oficina.ordemservico.infrastructure.OrdemServicoRepository;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static br.com.prime.oficina.shared.exception.ExceptionMessage.SERVICE_ORDER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AcompanharOrdemServicoPublicaService {

    private final OrdemServicoRepository ordemServicoRepository;

    public AcompanharOrdemServicoPublicaResponse acompanharPorCodigo(String codigo) {
        OrdemServico ordemServico = ordemServicoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNaoEncontradoException(SERVICE_ORDER_NOT_FOUND));

        return toResponse(ordemServico);
    }

    private AcompanharOrdemServicoPublicaResponse toResponse(OrdemServico ordemServico) {
        return new AcompanharOrdemServicoPublicaResponse(
                ordemServico.getCodigo(),
                ordemServico.getDescricaoProblema(),
                ordemServico.getStatus(),
                ordemServico.getDataCadastro(),
                ordemServico.getDataInicioExecucao(),
                ordemServico.getDataFimExecucao(),
                ordemServico.getDataEntregue(),
                ordemServico.getDataCancelada()
        );
    }
}
