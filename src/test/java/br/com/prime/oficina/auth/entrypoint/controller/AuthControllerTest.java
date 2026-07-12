package br.com.prime.oficina.auth.entrypoint.controller;

import br.com.prime.oficina.auth.LoginRequest;
import br.com.prime.oficina.auth.LoginResponse;

import br.com.prime.oficina.auth.gestaousuarios.domain.RoleUsuario;
import br.com.prime.oficina.auth.gestaousuarios.domain.Usuario;
import br.com.prime.oficina.auth.gestaousuarios.output.persistence.UsuarioRepository;
import br.com.prime.oficina.security.JwtService;
import br.com.prime.oficina.security.domain.SecurityUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthController authController;

    @Test
    void deveRealizarLoginComSucesso() {
        Usuario usuario = criarUsuario(true);
        LoginRequest request = new LoginRequest("admin@oficina.com", "admin");

        when(usuarioRepository.findByEmail("admin@oficina.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("admin", "hash")).thenReturn(true);
        when(jwtService.gerarToken(any(SecurityUserDetails.class))).thenReturn("token-jwt");

        ResponseEntity<LoginResponse> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("token-jwt", response.getBody().token());
        assertEquals("Bearer", response.getBody().tipo());
    }

    @Test
    void deveRetornarUnauthorizedQuandoSenhaForInvalida() {
        Usuario usuario = criarUsuario(true);
        LoginRequest request = new LoginRequest("admin@oficina.com", "senha-errada");

        when(usuarioRepository.findByEmail("admin@oficina.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha-errada", "hash")).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authController.login(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verify(jwtService, never()).gerarToken(any());
    }

    @Test
    void deveRetornarForbiddenQuandoUsuarioEstiverInativo() {
        Usuario usuario = criarUsuario(false);
        LoginRequest request = new LoginRequest("admin@oficina.com", "admin");

        when(usuarioRepository.findByEmail("admin@oficina.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("admin", "hash")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authController.login(request)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(jwtService, never()).gerarToken(any());
    }

    private Usuario criarUsuario(boolean ativo) {
        Usuario usuario = new Usuario();
        usuario.setNome("Admin");
        usuario.setEmail("admin@oficina.com");
        usuario.setSenha("hash");
        usuario.setRole(RoleUsuario.ADMIN);
        usuario.setAtivo(ativo);
        return usuario;
    }
}
