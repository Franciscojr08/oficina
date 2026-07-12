package br.com.prime.oficina.servico.application.usecase;

import br.com.prime.oficina.servico.application.dto.ServicoRequest;
import br.com.prime.oficina.servico.application.dto.ServicoResponse;
import br.com.prime.oficina.servico.application.ServicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicoUseCase {

	private final ServicoService servicoService;

	public ServicoResponse criar(ServicoRequest request) {
		return servicoService.criar(request);
	}

	public List<ServicoResponse> listar() {
		return servicoService.listar();
	}

	public ServicoResponse buscarPorId(Long id) {
		return servicoService.buscarPorId(id);
	}

	public ServicoResponse atualizar(Long id, ServicoRequest request) {
		return servicoService.atualizar(id, request);
	}

	public void inativar(Long id) {
		servicoService.inativar(id);
	}
}