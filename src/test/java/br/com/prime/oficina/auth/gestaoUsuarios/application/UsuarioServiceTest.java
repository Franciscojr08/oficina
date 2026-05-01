package br.com.prime.oficina.auth.gestaoUsuarios.application;

import br.com.prime.oficina.auth.gestaoUsuarios.domain.RoleUsuario;
import br.com.prime.oficina.auth.gestaoUsuarios.domain.Usuario;
import br.com.prime.oficina.auth.gestaoUsuarios.infrastructure.UsuarioRepository;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void deveCriarUsuarioComSenhaCriptografada() {
        UsuarioRequest request = new UsuarioRequest("Ana", "ana@email.com", "senha123", RoleUsuario.ATENDENTE);

        when(usuarioRepository.existsByEmail("ana@email.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(10L);
            return usuario;
        });

        UsuarioResponse response = usuarioService.criar(request);

        assertEquals(10L, response.id());
        assertEquals("Ana", response.nome());
        assertEquals("ana@email.com", response.email());
        assertEquals(RoleUsuario.ATENDENTE, response.role());
        assertTrue(response.ativo());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("hash", captor.getValue().getSenha());
    }

    @Test
    void naoDeveCriarUsuarioQuandoEmailJaExistir() {
        UsuarioRequest request = new UsuarioRequest("Ana", "ana@email.com", "senha123", RoleUsuario.ATENDENTE);

        when(usuarioRepository.existsByEmail("ana@email.com")).thenReturn(true);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> usuarioService.criar(request)
        );

        assertEquals("Ja existe usuario cadastrado com este email", exception.getMessage());
        verify(passwordEncoder, never()).encode("senha123");
        verify(usuarioRepository, never()).save(any());
    }
}
