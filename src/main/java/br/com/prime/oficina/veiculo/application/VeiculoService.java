package br.com.prime.oficina.veiculo.application;

import br.com.prime.oficina.cliente.domain.Cliente;
import br.com.prime.oficina.cliente.infraestructure.ClienteRepository;
import br.com.prime.oficina.ordemservico.application.StatusOrdemServico;
import br.com.prime.oficina.ordemservico.infrastructure.OrdemServicoRepository;
import br.com.prime.oficina.shared.exception.RecursoDuplicadoException;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
import br.com.prime.oficina.shared.validator.ValidadorPlaca;
import br.com.prime.oficina.veiculo.domain.Veiculo;
import br.com.prime.oficina.veiculo.infrastructure.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static br.com.prime.oficina.shared.exception.ExceptionMessage.*;

@Service
@RequiredArgsConstructor
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final ClienteRepository clienteRepository;
	private final OrdemServicoRepository ordemServicoRepository;

    @Transactional
    public VeiculoResponse criar(VeiculoRequest request) {
        validarPlacaDuplicada(request.placa());

        Cliente cliente = buscarClientePorId(request.clienteId());

		validarCliente(cliente);

		Veiculo veiculo = new Veiculo();
		preencherVeiculo(veiculo, request, cliente);

        Veiculo salvo = veiculoRepository.save(veiculo);
        return toResponse(salvo);
    }

	private static void validarCliente(Cliente cliente) {
		if (!cliente.getAtivo()) {
			throw new RegraNegocioException(NOT_ACTIVE_CUSTOMER);
		}
	}

    public List<VeiculoResponse> listar() {
        return veiculoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public VeiculoResponse buscarPorId(Long id) {
        Veiculo veiculo = buscarVeiculoPorId(id);
        return toResponse(veiculo);
    }

    public List<VeiculoResponse> listarPorCliente(Long clienteId) {
        return veiculoRepository.findByClienteId(clienteId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public VeiculoResponse atualizar(Long id, VeiculoRequest request) {
		validarPlaca(request.placa());

        Veiculo veiculo = buscarVeiculoPorId(id);

        Cliente cliente = buscarClientePorId(request.clienteId());
		validarCliente(cliente);

        if (!veiculo.getPlaca().equals(request.placa())
                && veiculoRepository.existsByPlaca(request.placa())) {
            throw new RecursoDuplicadoException(DUPLICATED_VEHICLE);
        }

        preencherVeiculo(veiculo, request, cliente);
        Veiculo atualizado = veiculoRepository.save(veiculo);

        return toResponse(atualizado);
    }

    @Transactional
    public void inativar(Long id) {
        Veiculo veiculo = buscarVeiculoPorId(id);

		boolean possuiOrdemAtiva = ordemServicoRepository
				.existsByVeiculoIdAndStatusIn(id, StatusOrdemServico.statusAtivos());

		if (possuiOrdemAtiva) {
			throw new RegraNegocioException(
					"Não é possível inativar o veículo, pois ele possui ordens de serviço ativas."
			);
		}

        veiculo.setAtivo(false);
        veiculoRepository.save(veiculo);
    }

    @Transactional(readOnly = true)
    public VeiculoResponse buscarPorPlaca(String placa) {
        Veiculo veiculo = veiculoRepository.findByPlaca(placa)
                .orElseThrow(() -> new RecursoNaoEncontradoException(VEHICLE_NOT_FOUND));

        return toResponse(veiculo);
    }

    private void validarPlacaDuplicada(String placa) {
		validarPlaca(placa);

        if (veiculoRepository.existsByPlaca(placa)) {
            throw new RegraNegocioException(DUPLICATED_VEHICLE);
        }
    }

	private void validarPlaca(String placa) {
		boolean placaValida = ValidadorPlaca.isValida(placa);

		if (!placaValida) {
			throw new RegraNegocioException(INVALID_VEHICLE_PLATE);
		}
	}

    private Cliente buscarClientePorId(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(CUSTOMER_NOT_FOUND));
    }

    private Veiculo buscarVeiculoPorId(Long id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(VEHICLE_NOT_FOUND));
    }

    private VeiculoResponse toResponse(Veiculo veiculo) {
        return new VeiculoResponse(
                veiculo.getId(),
                veiculo.getCliente().getId(),
                veiculo.getPlaca(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getAno(),
                veiculo.getCor(),
                veiculo.getObservacao(),
                veiculo.getAtivo(),
                veiculo.getDataCriacao(),
                veiculo.getDataAtualizacao()
        );
    }

    private void preencherVeiculo(Veiculo veiculo, VeiculoRequest request, Cliente cliente) {
        veiculo.setCliente(cliente);
        veiculo.setPlaca(request.placa());
        veiculo.setMarca(request.marca());
        veiculo.setModelo(request.modelo());
        veiculo.setAno(request.ano());
        veiculo.setCor(request.cor());
        veiculo.setObservacao(request.observacao());
    }


}