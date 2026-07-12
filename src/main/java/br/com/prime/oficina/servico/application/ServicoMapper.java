package br.com.prime.oficina.servico.application;

import br.com.prime.oficina.servico.application.dto.*;

import br.com.prime.oficina.servico.domain.Servico;
import org.springframework.stereotype.Component;

@Component
public class ServicoMapper {

	public ServicoResponse toResponse(Servico servico) {
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
