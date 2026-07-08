package br.com.prime.oficina.veiculo.output.persistence;

import br.com.prime.oficina.veiculo.application.gateway.VeiculoGateway;
import br.com.prime.oficina.veiculo.domain.Veiculo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VeiculoPersistenceAdapter implements VeiculoGateway {

	private final VeiculoRepository veiculoRepository;

	@Override
	public Veiculo save(Veiculo veiculo) {
		return veiculoRepository.save(veiculo);
	}

	@Override
	public List<Veiculo> findAll() {
		return veiculoRepository.findAll();
	}

	@Override
	public Optional<Veiculo> findById(Long id) {
		return veiculoRepository.findById(id);
	}

	@Override
	public boolean existsByPlaca(String placa) {
		return veiculoRepository.existsByPlaca(placa);
	}

	@Override
	public Optional<Veiculo> findByPlaca(String placa) {
		return veiculoRepository.findByPlaca(placa);
	}

	@Override
	public List<Veiculo> findByClienteId(Long clienteId) {
		return veiculoRepository.findByClienteId(clienteId);
	}
}
