package br.com.prime.oficina.security;

import br.com.prime.oficina.auth.gestaousuarios.domain.RoleUsuario;
import br.com.prime.oficina.auth.gestaousuarios.domain.Usuario;
import br.com.prime.oficina.security.domain.SecurityUserDetails;
import br.com.prime.oficina.security.domain.TipoToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SEGREDO = "jwt-test-secret-123456789012345678901234567890";
    private static final String OUTRO_SEGREDO = "outro-segredo-123456789012345678901234567890";
    private static final long DUAS_HORAS = 7_200_000L;

    private JwtService jwtService;

    @BeforeEach
    void configurar() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SEGREDO);
        ReflectionTestUtils.setField(jwtService, "expiration", DUAS_HORAS);
    }

    @Test
    void deveMarcarTokenDeLoginComoOperador() {
        String token = jwtService.gerarToken(new SecurityUserDetails(criarUsuario()));

        assertEquals(TipoToken.OPERADOR, jwtService.extrairTipo(token));
        assertEquals("admin@oficina.com", jwtService.extrairUsername(token));
        assertEquals("ADMIN", jwtService.extrairClaims(token).get(JwtService.CLAIM_ROLE, String.class));
    }

    @Test
    void deveTratarTokenSemClaimTipoComoOperador() {
        String token = tokenSemTipo(SEGREDO);

        assertEquals(TipoToken.OPERADOR, jwtService.extrairTipo(token));
    }

    @Test
    void deveReconhecerTokenDeClienteEmitidoPelaFunctionServerless() {
        String token = tokenDeCliente(SEGREDO, DUAS_HORAS);

        assertEquals(TipoToken.CLIENTE, jwtService.extrairTipo(token));

        Claims claims = jwtService.extrairClaims(token);

        assertEquals("52998224725", claims.getSubject());
        assertEquals(7L, claims.get(JwtService.CLAIM_CLIENTE_ID, Number.class).longValue());
        assertEquals("Ana Souza", claims.get(JwtService.CLAIM_NOME, String.class));
    }

    @Test
    void deveAceitarTokenDeClienteAssinadoComOMesmoSegredo() {
        assertTrue(jwtService.tokenValido(tokenDeCliente(SEGREDO, DUAS_HORAS)));
    }

    @Test
    void deveRecusarTokenAssinadoComOutroSegredo() {
        assertFalse(jwtService.tokenValido(tokenDeCliente(OUTRO_SEGREDO, DUAS_HORAS)));
    }

    @Test
    void deveRecusarTokenExpirado() {
        assertFalse(jwtService.tokenValido(tokenDeCliente(SEGREDO, -1_000L)));
    }

    @Test
    void deveRecusarTextoQueNaoEhToken() {
        assertFalse(jwtService.tokenValido("nao-e-um-jwt"));
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

    private String tokenDeCliente(String segredo, long validadeEmMillis) {
        return Jwts.builder()
                .subject("52998224725")
                .claim("tipo", "CLIENTE")
                .claim("clienteId", 7)
                .claim("nome", "Ana Souza")
                .claim("role", "CLIENTE")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + validadeEmMillis))
                .signWith(chave(segredo), Jwts.SIG.HS256)
                .compact();
    }

    private String tokenSemTipo(String segredo) {
        return Jwts.builder()
                .subject("admin@oficina.com")
                .claim("role", "ADMIN")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + DUAS_HORAS))
                .signWith(chave(segredo), Jwts.SIG.HS256)
                .compact();
    }

    private SecretKey chave(String segredo) {
        return Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
    }
}
