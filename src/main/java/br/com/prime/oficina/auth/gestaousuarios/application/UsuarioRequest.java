package br.com.prime.oficina.auth.gestaousuarios.application;

import br.com.prime.oficina.auth.gestaousuarios.domain.RoleUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(

        @NotBlank(message = "Nome e obrigatorio")
        @Size(max = 150, message = "Nome deve ter no maximo 150 caracteres")
        String nome,

        @NotBlank(message = "Email e obrigatorio")
        @Email(message = "Email invalido")
        @Size(max = 150, message = "Email deve ter no maximo 150 caracteres")
        String email,

        @NotBlank(message = "Senha e obrigatoria")
        @Size(min = 6, message = "Senha deve ter no minimo 6 caracteres")
        String senha,

        @NotNull(message = "Role e obrigatoria")
        RoleUsuario role
) {
}
