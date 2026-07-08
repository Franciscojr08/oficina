package br.com.prime.oficina.apipublica.usecase;

import br.com.prime.oficina.apipublica.AcompanharOrdemServicoPublicaResponse;
import br.com.prime.oficina.apipublica.AcompanharOrdemServicoPublicaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AcompanharOrdemServicoPublicaUseCase {

	private final AcompanharOrdemServicoPublicaService service;

	public AcompanharOrdemServicoPublicaResponse acompanharPorCodigo(String codigo) {
		return service.acompanharPorCodigo(codigo);
	}
}