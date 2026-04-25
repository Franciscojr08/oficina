package br.com.prime.oficina.auth;

import br.com.prime.oficina.auth.gestaoUsuarios.domain.Usuario;
import br.com.prime.oficina.auth.gestaoUsuarios.infrastructure.UsuarioRepository;
import br.com.prime.oficina.security.JwtService;
import br.com.prime.oficina.security.domain.SecurityUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Credenciais inválidas"
                ));

        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Credenciais inválidas"
            );
        }

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Usuário inativo"
            );
        }

        String token = jwtService.gerarToken(new SecurityUserDetails(usuario));

        return ResponseEntity.ok(new LoginResponse(token, "Bearer"));
    }
}