package br.com.prime.oficina.veiculo.application;

import br.com.prime.oficina.veiculo.application.dto.*;

import br.com.prime.oficina.veiculo.domain.Veiculo;
import org.springframework.stereotype.Component;

@Component
public class VeiculoMapper {

	public VeiculoResponse toResponse(Veiculo veiculo) {
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
}
