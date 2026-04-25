package br.com.prime.oficina.auth.gestaoUsuarios.application;

import br.com.prime.oficina.auth.gestaoUsuarios.domain.RoleUsuario;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        RoleUsuario role,
        Boolean ativo,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
}
