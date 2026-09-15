package br.com.prime.oficina.security;

import br.com.prime.oficina.security.domain.SecurityUserDetails;
import br.com.prime.oficina.security.domain.TipoToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    public static final String CLAIM_TIPO = "tipo";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_CLIENTE_ID = "clienteId";
    public static final String CLAIM_NOME = "nome";

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration}")
    private long expiration;

    public String gerarToken(SecurityUserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim(CLAIM_TIPO, TipoToken.OPERADOR.name())
                .claim(CLAIM_ROLE, userDetails.getRole())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(obterChaveAssinatura(), Jwts.SIG.HS256)
                .compact();
    }

    public String extrairUsername(String token) {
        return extrairClaims(token).getSubject();
    }

    /**
     * Identifica quem emitiu o token. Tokens gerados antes da claim {@code tipo} existir só podiam
     * pertencer a operadores, por isso a ausência da claim é tratada como {@link TipoToken#OPERADOR}.
     */
    public TipoToken extrairTipo(String token) {
        String tipo = extrairClaims(token).get(CLAIM_TIPO, String.class);

        if (tipo == null) {
            return TipoToken.OPERADOR;
        }

        return TipoToken.valueOf(tipo);
    }

    public boolean tokenEhValido(String token, SecurityUserDetails userDetails) {
        final String username = extrairUsername(token);
        return username.equals(userDetails.getUsername()) && !tokenExpirado(token);
    }

    /**
     * Valida apenas assinatura e expiração, sem consultar o banco.
     *
     * <p>É a validação possível para tokens de cliente, que são emitidos pela function serverless de
     * autenticação por CPF e não têm registro correspondente na tabela {@code usuario}.</p>
     */
    public boolean tokenValido(String token) {
        try {
            return !tokenExpirado(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(obterChaveAssinatura())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean tokenExpirado(String token) {
        return extrairClaims(token).getExpiration().before(new Date());
    }

    private SecretKey obterChaveAssinatura() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
