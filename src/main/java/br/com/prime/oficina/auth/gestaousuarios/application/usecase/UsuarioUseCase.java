package br.com.prime.oficina.auth.gestaousuarios.application.usecase;

import br.com.prime.oficina.auth.gestaousuarios.application.dto.UsuarioRequest;
import br.com.prime.oficina.auth.gestaousuarios.application.dto.UsuarioResponse;
import br.com.prime.oficina.auth.gestaousuarios.application.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioUseCase {

	private final UsuarioService usuarioService;

	public UsuarioResponse criar(UsuarioRequest request) {
		return usuarioService.criar(request);
	}
}