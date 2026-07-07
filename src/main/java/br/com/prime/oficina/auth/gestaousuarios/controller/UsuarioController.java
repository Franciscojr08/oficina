package br.com.prime.oficina.auth.gestaousuarios.controller;

import br.com.prime.oficina.auth.gestaousuarios.application.UsuarioRequest;
import br.com.prime.oficina.auth.gestaousuarios.application.UsuarioResponse;
import br.com.prime.oficina.auth.gestaousuarios.application.usecase.UsuarioUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

	private final UsuarioUseCase usuarioUseCase;

    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@RequestBody @Valid UsuarioRequest request) {
	    UsuarioResponse response = usuarioUseCase.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
