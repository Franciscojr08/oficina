package br.com.prime.oficina.veiculo.application.usecase;

import br.com.prime.oficina.veiculo.application.dto.VeiculoRequest;
import br.com.prime.oficina.veiculo.application.dto.VeiculoResponse;
import br.com.prime.oficina.veiculo.application.VeiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VeiculoUseCase {

	private final VeiculoService veiculoService;

	public VeiculoResponse criar(VeiculoRequest request) {
		return veiculoService.criar(request);
	}

	public List<VeiculoResponse> listar() {
		return veiculoService.listar();
	}

	public VeiculoResponse buscarPorId(Long id) {
		return veiculoService.buscarPorId(id);
	}

	public List<VeiculoResponse> listarPorCliente(Long clienteId) {
		return veiculoService.listarPorCliente(clienteId);
	}

	public VeiculoResponse atualizar(Long id, VeiculoRequest request) {
		return veiculoService.atualizar(id, request);
	}

	public VeiculoResponse buscarPorPlaca(String placa) {
		return veiculoService.buscarPorPlaca(placa);
	}

	public void inativar(Long id) {
		veiculoService.inativar(id);
	}
}