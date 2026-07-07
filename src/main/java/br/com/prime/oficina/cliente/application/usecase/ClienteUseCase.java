package br.com.prime.oficina.cliente.application.usecase;

import br.com.prime.oficina.cliente.application.ClienteRequest;
import br.com.prime.oficina.cliente.application.ClienteResponse;
import br.com.prime.oficina.cliente.application.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteUseCase {

	private final ClienteService clienteService;

	public ClienteResponse criar(ClienteRequest request) {
		return clienteService.criar(request);
	}

	public List<ClienteResponse> listar() {
		return clienteService.listar();
	}

	public ClienteResponse buscarPorId(Long id) {
		return clienteService.buscarPorId(id);
	}

	public ClienteResponse atualizar(Long id, ClienteRequest request) {
		return clienteService.atualizar(id, request);
	}

	public ClienteResponse buscarPorDocumento(String documento) {
		return clienteService.findByCpfCnpj(documento);
	}

	public void inativar(Long id) {
		clienteService.inativar(id);
	}
}