package br.com.prime.oficina.cliente.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.prime.oficina.veiculo.domain.Veiculo;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "cliente")
public class Cliente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nome", nullable = false, length = 150)
	private String nome;

	@Column(name = "cpf_cnpj", nullable = false, unique = true, length = 20)
	private String cpfCnpj;

	@Column(name = "telefone", length = 20)
	private String telefone;

	@Column(name = "email", length = 150)
	private String email;

	@Column(name = "cep", length = 8)
	private String cep;

	@Column(name = "logradouro", length = 150)
	private String logradouro;

	@Column(name = "bairro", length = 100)
	private String bairro;

	@Column(name = "cidade", length = 100)
	private String cidade;

	@Column(name = "uf", length = 2)
	private String uf;

	@Column(name = "data_nascimento", nullable = false)
	private LocalDate dataNascimento;

	@Column(name = "ativo", nullable = false)
	private Boolean ativo = true;

	@Column(name = "data_criacao", nullable = false)
	private LocalDateTime dataCriacao;

	@Column(name = "data_atualizacao")
	private LocalDateTime dataAtualizacao;

	@OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<Veiculo> veiculos = new ArrayList<>();

	@PrePersist
	public void prePersist() {
		this.dataCriacao = LocalDateTime.now();

		if (this.ativo == null) {
			this.ativo = true;
		}
	}

	@PreUpdate
	public void preUpdate() {
		this.dataAtualizacao = LocalDateTime.now();
	}
}