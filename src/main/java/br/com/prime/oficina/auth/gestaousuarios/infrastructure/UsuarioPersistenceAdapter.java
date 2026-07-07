package br.com.prime.oficina.auth.gestaousuarios.infrastructure;

import br.com.prime.oficina.auth.gestaousuarios.application.gateway.UsuarioGateway;
import br.com.prime.oficina.auth.gestaousuarios.domain.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UsuarioPersistenceAdapter implements UsuarioGateway {

	private final UsuarioRepository usuarioRepository;

	@Override
	public Usuario save(Usuario usuario) {
		return usuarioRepository.save(usuario);
	}

	@Override
	public boolean existsByEmail(String email) {
		return usuarioRepository.existsByEmail(email);
	}
}