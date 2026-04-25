package br.com.prime.oficina.auth.gestaoUsuarios.controller;

import br.com.prime.oficina.auth.gestaoUsuarios.application.UsuarioRequest;
import br.com.prime.oficina.auth.gestaoUsuarios.application.UsuarioResponse;
import br.com.prime.oficina.auth.gestaoUsuarios.application.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//TODO: ajustar para apenas ADMIN criar atendentes
@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> criar(@RequestBody @Valid UsuarioRequest request) {
        UsuarioResponse response = usuarioService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
