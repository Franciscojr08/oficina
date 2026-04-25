package br.com.prime.oficina.ordemServico.application;

import lombok.Getter;

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
}
