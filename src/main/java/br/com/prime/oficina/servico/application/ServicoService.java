package br.com.prime.oficina.servico.application;

import br.com.prime.oficina.ordemServico.application.StatusOrdemServico;
import br.com.prime.oficina.ordemServico.domain.OrdemServico;
import br.com.prime.oficina.ordemServico.servicos.domain.ServicoOrdemServico;
import br.com.prime.oficina.ordemServico.servicos.infrastructure.ServicoOrdemServicoRepository;
import br.com.prime.oficina.servico.domain.Servico;
import br.com.prime.oficina.servico.infrasctucture.ServicoRepository;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final ServicoOrdemServicoRepository servicoOrdemServicoRepository;

    @Transactional
    public ServicoResponse criar(ServicoRequest request) {
        validarNomeDuplicado(request.nome());

        Servico servico = new Servico();
        preencherServico(servico, request);

		servicoRepository.save(servico);

		return toResponse(servico);
    }

    public List<ServicoResponse> listar() {
        return servicoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ServicoResponse buscarPorId(Long id) {
        Servico servico = buscarServicoPorId(id);
        return toResponse(servico);
    }

    @Transactional
    public ServicoResponse atualizar(Long id, ServicoRequest request) {
        Servico servico = buscarServicoPorId(id);

        if (!servico.getNome().equalsIgnoreCase(request.nome())
                && servicoRepository.existsByNomeIgnoreCase(request.nome())) {
            throw new RegraNegocioException("Já existe serviço cadastrado com este nome");
        }

        preencherServico(servico, request);

		servicoRepository.saveAndFlush(servico);

		return toResponse(servico);
    }

    @Transactional
    public void inativar(Long id) {
        Servico servico = buscarServicoPorId(id);

		boolean estaEmOrdemAtiva = servicoOrdemServicoRepository
				.existsByServicoIdAndOrdemServicoStatusIn(
						id,
						StatusOrdemServico.statusAtivos()
				);

		if (estaEmOrdemAtiva) {
			throw new RegraNegocioException(
					"Não é possível inativar o serviço, pois ele possui ordens de serviço ativas."
			);
		}

        servico.setAtivo(false);

        Optional<ServicoOrdemServico> servicoOrdemServico = servicoOrdemServicoRepository.findByServicoId(servico.getId());
        if(servicoOrdemServico.isPresent()) {
            ServicoOrdemServico servicoOrdemServicoAtualizado = servicoOrdemServico.get();
            OrdemServico ordemServico = servicoOrdemServicoAtualizado.getOrdemServico();
            if(StatusOrdemServico.EM_EXECUCAO.equals(ordemServico.getStatus())) throw new RegraNegocioException("Servico em execução em ordem de serviço");
        }

        servicoRepository.save(servico);
    }

    private void validarNomeDuplicado(String nome) {
        if (servicoRepository.existsByNomeIgnoreCase(nome)) {
            throw new RegraNegocioException("Já existe serviço cadastrado com este nome");
        }
    }

    private Servico buscarServicoPorId(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado"));
    }

    private void preencherServico(Servico servico, ServicoRequest request) {
        servico.setNome(request.nome());
        servico.setDescricao(request.descricao());
		servico.setValor(request.valor());
    }

    private ServicoResponse toResponse(Servico servico) {
        return new ServicoResponse(
                servico.getId(),
                servico.getNome(),
                servico.getDescricao(),
				servico.getValor(),
                servico.getAtivo(),
                servico.getDataCriacao(),
                servico.getDataAtualizacao()
        );
    }
}