package br.com.prime.oficina.servico.application;

import br.com.prime.oficina.servico.domain.Servico;
import br.com.prime.oficina.servico.infrasctucture.ServicoRepository;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository servicoRepository;

    @Transactional
    public ServicoResponse criar(ServicoRequest request) {
        validarNomeDuplicado(request.nome());

        Servico servico = new Servico();
        preencherServico(servico, request);

        Servico salvo = servicoRepository.save(servico);
        return toResponse(salvo);
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

        Servico atualizado = servicoRepository.save(servico);
        return toResponse(atualizado);
    }

    @Transactional
    public void inativar(Long id) {
        Servico servico = buscarServicoPorId(id);
        servico.setAtivo(false);
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
        servico.setPrecoBase(request.precoBase());
        servico.setTempoEstimadoMinutos(request.tempoEstimadoMinutos());
    }

    private ServicoResponse toResponse(Servico servico) {
        return new ServicoResponse(
                servico.getId(),
                servico.getNome(),
                servico.getDescricao(),
                servico.getPrecoBase(),
                servico.getTempoEstimadoMinutos(),
                servico.getAtivo(),
                servico.getDataCriacao(),
                servico.getDataAtualizacao()
        );
    }
}