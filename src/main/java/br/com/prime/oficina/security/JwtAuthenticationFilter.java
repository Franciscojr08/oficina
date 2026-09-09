package br.com.prime.oficina.security;

import br.com.prime.oficina.security.domain.ClienteAutenticado;
import br.com.prime.oficina.security.domain.SecurityUserDetails;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String ROLE_CLIENTE = "ROLE_CLIENTE";

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            autenticar(token, request);
        }

        filterChain.doFilter(request, response);
    }

    private void autenticar(String token, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication;

        try {
            authentication = switch (jwtService.extrairTipo(token)) {
                case CLIENTE -> autenticacaoDeCliente(token);
                case OPERADOR -> autenticacaoDeOperador(token);
            };
        } catch (Exception e) {
            // Token expirado, assinado com outra chave, com claims inesperadas ou de um usuário
            // que não existe mais: a requisição segue sem autenticação.
            return;
        }

        if (authentication == null) {
            return;
        }

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private UsernamePasswordAuthenticationToken autenticacaoDeOperador(String token) {
        String username = jwtService.extrairUsername(token);

        if (username == null) {
            return null;
        }

        // Recarrega o usuário do banco para refletir mudanças feitas depois da emissão do token.
        SecurityUserDetails userDetails =
                (SecurityUserDetails) customUserDetailsService.loadUserByUsername(username);

        if (!jwtService.tokenEhValido(token, userDetails)) {
            return null;
        }

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    /**
     * Autentica o token emitido pela function serverless de autenticação por CPF.
     *
     * <p>Não há consulta ao banco: o cliente não tem registro na tabela {@code usuario}, e a
     * function só emite o token depois de confirmar que o cliente existe e está ativo. A authority
     * é sempre {@code ROLE_CLIENTE}, independentemente da claim {@code role}, para que um token de
     * cliente nunca conceda privilégios de operador.</p>
     */
    private UsernamePasswordAuthenticationToken autenticacaoDeCliente(String token) {
        if (!jwtService.tokenValido(token)) {
            return null;
        }

        Claims claims = jwtService.extrairClaims(token);

        Number clienteId = claims.get(JwtService.CLAIM_CLIENTE_ID, Number.class);

        if (clienteId == null || claims.getSubject() == null) {
            return null;
        }

        ClienteAutenticado cliente = new ClienteAutenticado(
                clienteId.longValue(),
                claims.getSubject(),
                claims.get(JwtService.CLAIM_NOME, String.class)
        );

        return new UsernamePasswordAuthenticationToken(
                cliente,
                null,
                List.of(new SimpleGrantedAuthority(ROLE_CLIENTE))
        );
    }
}
