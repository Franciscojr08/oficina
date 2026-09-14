package br.com.prime.oficina.security;

import br.com.prime.oficina.auth.gestaousuarios.domain.RoleUsuario;
import br.com.prime.oficina.auth.gestaousuarios.domain.Usuario;
import br.com.prime.oficina.security.domain.ClienteAutenticado;
import br.com.prime.oficina.security.domain.SecurityUserDetails;
import br.com.prime.oficina.security.domain.TipoToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String TOKEN = "token-jwt";

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final MockFilterChain filterChain = new MockFilterChain();

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveAutenticarClienteSemConsultarATabelaUsuario() throws Exception {
        when(jwtService.extrairTipo(TOKEN)).thenReturn(TipoToken.CLIENTE);
        when(jwtService.tokenValido(TOKEN)).thenReturn(true);
        when(jwtService.extrairClaims(TOKEN)).thenReturn(claimsDeCliente(7, "52998224725", "Ana Souza"));

        filter.doFilter(requisicaoComToken(), response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);

        ClienteAutenticado cliente = assertInstanceOf(ClienteAutenticado.class, authentication.getPrincipal());

        assertEquals(7L, cliente.id());
        assertEquals("52998224725", cliente.cpf());
        assertEquals("Ana Souza", cliente.nome());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_CLIENTE")));

        verify(customUserDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    void naoDeveAutenticarClienteComTokenInvalido() throws Exception {
        when(jwtService.extrairTipo(TOKEN)).thenReturn(TipoToken.CLIENTE);
        when(jwtService.tokenValido(TOKEN)).thenReturn(false);

        filter.doFilter(requisicaoComToken(), response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void naoDeveAutenticarClienteQuandoOTokenNaoTrazIdentificador() throws Exception {
        when(jwtService.extrairTipo(TOKEN)).thenReturn(TipoToken.CLIENTE);
        when(jwtService.tokenValido(TOKEN)).thenReturn(true);
        when(jwtService.extrairClaims(TOKEN)).thenReturn(claimsDeCliente(null, "52998224725", "Ana Souza"));

        filter.doFilter(requisicaoComToken(), response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void deveAutenticarOperadorRecarregandoOUsuarioDoBanco() throws Exception {
        SecurityUserDetails userDetails = new SecurityUserDetails(criarUsuario());

        when(jwtService.extrairTipo(TOKEN)).thenReturn(TipoToken.OPERADOR);
        when(jwtService.extrairUsername(TOKEN)).thenReturn("admin@oficina.com");
        when(customUserDetailsService.loadUserByUsername("admin@oficina.com")).thenReturn(userDetails);
        when(jwtService.tokenEhValido(TOKEN, userDetails)).thenReturn(true);

        filter.doFilter(requisicaoComToken(), response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertEquals(userDetails, authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void naoDeveAutenticarOperadorQueNaoExisteMais() throws Exception {
        when(jwtService.extrairTipo(TOKEN)).thenReturn(TipoToken.OPERADOR);
        when(jwtService.extrairUsername(TOKEN)).thenReturn("removido@oficina.com");
        when(customUserDetailsService.loadUserByUsername("removido@oficina.com"))
                .thenThrow(new UsernameNotFoundException("Usuário não encontrado"));

        filter.doFilter(requisicaoComToken(), response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void deveSeguirSemAutenticacaoQuandoNaoHaHeaderAuthorization() throws Exception {
        filter.doFilter(new MockHttpServletRequest(), response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).extrairTipo(any());
    }

    private MockHttpServletRequest requisicaoComToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + TOKEN);
        return request;
    }

    private Claims claimsDeCliente(Integer clienteId, String cpf, String nome) {
        return Jwts.claims()
                .subject(cpf)
                .add("tipo", "CLIENTE")
                .add("clienteId", clienteId)
                .add("nome", nome)
                .build();
    }

    private Usuario criarUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Administrador");
        usuario.setEmail("admin@oficina.com");
        usuario.setSenha("hash");
        usuario.setRole(RoleUsuario.ADMIN);
        usuario.setAtivo(true);
        return usuario;
    }
}
