package br.com.prime.oficina.ordemservico.application;

import lombok.Getter;

import java.util.List;

@Getter
public enum StatusOrdemServico {
	RECEBIDA("Recebida"),
	EM_DIAGNOSTICO("Em diagnóstico"),
	AGUARDANDO_APROVACAO("Aguardando aprovação"),
	EM_EXECUCAO("Em execução"),
	FINALIZADA("Finalizada"),
	ENTREGUE("Entregue"),
	CANCELADA("Cancelada");

	private final String descricao;

	StatusOrdemServico(String descricao) {
		this.descricao = descricao;
	}

	public boolean estaEmEdicao() {
		return this != RECEBIDA && this != EM_DIAGNOSTICO;
	}

	public static List<StatusOrdemServico> statusAtivos() {
		return List.of(
			RECEBIDA,
			EM_DIAGNOSTICO,
			AGUARDANDO_APROVACAO,
			EM_EXECUCAO,
			FINALIZADA
		);
	}
}
