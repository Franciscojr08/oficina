package br.com.prime.oficina.cliente.application;

import br.com.prime.oficina.cliente.application.dto.*;

import br.com.prime.oficina.cliente.domain.Cliente;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

	public ClienteResponse toResponse(Cliente cliente) {
		return new ClienteResponse(
				cliente.getId(),
				cliente.getNome(),
				cliente.getCpfCnpj(),
				cliente.getTelefone(),
				cliente.getEmail(),
				cliente.getCep(),
				cliente.getLogradouro(),
				cliente.getBairro(),
				cliente.getCidade(),
				cliente.getUf(),
				cliente.getDataNascimento(),
				cliente.getAtivo(),
				cliente.getDataCriacao(),
				cliente.getDataAtualizacao()
		);
	}
}
