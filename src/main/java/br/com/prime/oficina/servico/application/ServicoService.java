package br.com.prime.oficina.servico.application;

import br.com.prime.oficina.servico.application.dto.*;

import br.com.prime.oficina.ordemservico.application.StatusOrdemServico;
import br.com.prime.oficina.ordemservico.servicos.application.gateway.ServicoOrdemServicoGateway;
import br.com.prime.oficina.servico.application.gateway.ServicoGateway;
import br.com.prime.oficina.servico.domain.Servico;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static br.com.prime.oficina.shared.exception.ExceptionMessage.*;

@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoGateway servicoGateway;
    private final ServicoOrdemServicoGateway servicoOrdemServicoGateway;
	private final ServicoMapper servicoMapper;

    @Transactional
    public ServicoResponse criar(ServicoRequest request) {
        validarNomeDuplicado(request.nome());

        Servico servico = new Servico();
        preencherServico(servico, request);

		servicoGateway.save(servico);

		return servicoMapper.toResponse(servico);
    }

    public List<ServicoResponse> listar() {
        return servicoGateway.findAll()
                .stream()
                .map(servicoMapper::toResponse)
                .toList();
    }

    public ServicoResponse buscarPorId(Long id) {
        Servico servico = buscarServicoPorId(id);
        return servicoMapper.toResponse(servico);
    }

    @Transactional
    public ServicoResponse atualizar(Long id, ServicoRequest request) {
        Servico servico = buscarServicoPorId(id);

        if (!servico.getNome().equalsIgnoreCase(request.nome())
                && servicoGateway.existsByNomeIgnoreCase(request.nome())) {
            throw new RegraNegocioException(EXISTING_SERVICE);
        }

        preencherServico(servico, request);

		servicoGateway.saveAndFlush(servico);

		return servicoMapper.toResponse(servico);
    }

    @Transactional
    public void inativar(Long id) {
        Servico servico = buscarServicoPorId(id);

		boolean estaEmOrdemAtiva = servicoOrdemServicoGateway
				.existsByServicoIdAndOrdemServicoStatusIn(
						id,
						StatusOrdemServico.statusAtivos()
				);

		if (estaEmOrdemAtiva) {
			throw new RegraNegocioException(CANNOT_INACTIVATE_SERVICE_WITH_ACTIVE_SERVICE_ORDERS);
		}

        servico.setAtivo(false);
        servicoGateway.save(servico);
    }

    private void validarNomeDuplicado(String nome) {
        if (servicoGateway.existsByNomeIgnoreCase(nome)) {
            throw new RegraNegocioException(EXISTING_SERVICE);
        }
    }

    private Servico buscarServicoPorId(Long id) {
        return servicoGateway.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(SERVICE_NOT_FOUND));
    }

    private void preencherServico(Servico servico, ServicoRequest request) {
        servico.setNome(request.nome());
        servico.setDescricao(request.descricao());
		servico.setValor(request.valor());
    }

}
