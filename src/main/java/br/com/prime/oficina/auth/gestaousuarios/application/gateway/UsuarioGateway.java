package br.com.prime.oficina.auth.gestaousuarios.application.gateway;

import br.com.prime.oficina.auth.gestaousuarios.domain.Usuario;

public interface UsuarioGateway {

	Usuario save(Usuario usuario);

	boolean existsByEmail(String email);
}
