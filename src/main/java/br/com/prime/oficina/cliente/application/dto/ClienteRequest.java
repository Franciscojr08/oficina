package br.com.prime.oficina.cliente.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ClienteRequest(

		@NotBlank(message = "Nome é obrigatório")
		@Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
		String nome,

		@NotBlank(message = "CPF/CNPJ é obrigatório")
		@Size(max = 20, message = "CPF/CNPJ deve ter no máximo 20 caracteres")
		String cpfCnpj,

		@Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
		String telefone,

		@Email(message = "E-mail inválido")
		@Size(max = 150, message = "E-mail deve ter no máximo 150 caracteres")
		String email,

		@NotBlank(message = "O CEP é obrigatório")
		@Size(max = 8, message = "O CEP deve ter no máximo 8 caracteres")
		String cep,

		@NotBlank(message = "O logradouro é obrigatório")
		@Size(max = 150, message = "O logradouro deve ter no máximo 150 caracteres")
		String logradouro,

		@NotBlank(message = "O bairro é obrigatório")
		@Size(max = 100, message = "O bairro deve ter no máximo 100 caracteres")
		String bairro,

		@NotBlank(message = "A cidade é obrigatória")
		@Size(max = 100, message = "A cidade deve ter no máximo 100 caracteres")
		String cidade,

		@NotBlank(message = "A uf do estado é obrigatória")
		@Size(min = 2, max = 2, message = "A uf deve ter 2 caracteres")
		String uf,

		@NotNull(message = "A data de nascimento é obrigatória")
		LocalDate data_nascimento
) {
}
