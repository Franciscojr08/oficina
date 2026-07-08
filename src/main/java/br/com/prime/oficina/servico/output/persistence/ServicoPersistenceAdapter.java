package br.com.prime.oficina.servico.output.persistence;

import br.com.prime.oficina.servico.application.gateway.ServicoGateway;
import br.com.prime.oficina.servico.domain.Servico;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ServicoPersistenceAdapter implements ServicoGateway {

	private final ServicoRepository servicoRepository;

	@Override
	public Servico save(Servico servico) {
		return servicoRepository.save(servico);
	}

	@Override
	public Servico saveAndFlush(Servico servico) {
		return servicoRepository.saveAndFlush(servico);
	}

	@Override
	public List<Servico> findAll() {
		return servicoRepository.findAll();
	}

	@Override
	public Optional<Servico> findById(Long id) {
		return servicoRepository.findById(id);
	}

	@Override
	public boolean existsByNomeIgnoreCase(String nome) {
		return servicoRepository.existsByNomeIgnoreCase(nome);
	}
}
