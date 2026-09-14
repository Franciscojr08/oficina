package br.com.prime.oficina.config;

import br.com.prime.oficina.auth.gestaousuarios.domain.RoleUsuario;
import br.com.prime.oficina.auth.gestaousuarios.domain.Usuario;
import br.com.prime.oficina.security.JwtService;
import br.com.prime.oficina.security.domain.SecurityUserDetails;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.jdbc.Sql;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Sql(scripts = "/db/testdata/admin-user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class ControllerIntegrationTestSupport {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    protected String bearerTokenAdmin(JwtService jwtService) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Administrador");
        usuario.setEmail("admin@oficina.com");
        usuario.setSenha("senha-mockada");
        usuario.setRole(RoleUsuario.ADMIN);
        usuario.setAtivo(true);

        String token = jwtService.gerarToken(new SecurityUserDetails(usuario));

        return "Bearer " + token;
    }

    /**
     * Reproduz o token que a function serverless de autenticação por CPF emite.
     *
     * <p>As claims são escritas literalmente, e não a partir das constantes de {@code JwtService},
     * porque o emissor real é externo à aplicação: se o contrato mudar de um lado só, o teste
     * precisa falhar.</p>
     */
    protected String bearerTokenCliente(Long clienteId, String cpf, String nome) {
        return bearerTokenCliente(clienteId, cpf, nome, jwtSecret);
    }

    /** Token de cliente assinado com uma chave que a aplicação não reconhece. */
    protected String bearerTokenClienteComOutroSegredo(Long clienteId, String cpf) {
        return bearerTokenCliente(clienteId, cpf, "Cliente", "outro-segredo-123456789012345678901234567890");
    }

    private String bearerTokenCliente(Long clienteId, String cpf, String nome, String segredo) {
        String token = Jwts.builder()
                .subject(cpf)
                .claim("tipo", "CLIENTE")
                .claim("clienteId", clienteId)
                .claim("nome", nome)
                .claim("role", "CLIENTE")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 7_200_000L))
                .signWith(
                        Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8)),
                        Jwts.SIG.HS256
                )
                .compact();

        return "Bearer " + token;
    }
}
