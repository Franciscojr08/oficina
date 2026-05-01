package br.com.prime.oficina.auth;

import br.com.prime.oficina.auth.gestaoUsuarios.domain.Usuario;
import br.com.prime.oficina.auth.gestaoUsuarios.infrastructure.UsuarioRepository;
import br.com.prime.oficina.security.JwtService;
import br.com.prime.oficina.security.domain.SecurityUserDetails;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticacao", description = "Endpoints de login e autenticacao JWT")
@RequiredArgsConstructor
@SecurityRequirements
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/login")
    @Operation(
            summary = "Realizar login",
            description = "Autentica o usuario e retorna um token JWT para acesso as rotas administrativas.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Login realizado com sucesso",
                            content = @Content(
                                    schema = @Schema(implementation = LoginResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "token": "eyJhbGciOiJIUzI1NiJ9...",
                                              "tipo": "Bearer"
                                            }
                                            """)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Credenciais invalidas"),
                    @ApiResponse(responseCode = "403", description = "Usuario inativo")
            }
    )
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Credenciais invalidas"
                ));

        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Credenciais invalidas"
            );
        }

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Usuario inativo"
            );
        }

        String token = jwtService.gerarToken(new SecurityUserDetails(usuario));

        return ResponseEntity.ok(new LoginResponse(token, "Bearer"));
    }
}
