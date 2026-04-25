package br.com.prime.oficina.security;

import br.com.prime.oficina.security.domain.SecurityUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        //validar presença e formato do header(Authorization: Bearer <token>)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        //Bearer puro = 7 caracteres sem o prefixo
        String token = authHeader.substring(7);
        String username;

        // obter usuário do token
        try {
            username = jwtService.extrairUsername(token);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        //verificar se tem usuário e se ninguém autenticou essa requisição
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            //Busca usuário real do banco, evita situação de mudar perfil, desativar, deletar, depois de emitir token
            SecurityUserDetails userDetails =
                    (SecurityUserDetails) customUserDetailsService.loadUserByUsername(username);

            //validar claims do token e expiração
            if (jwtService.tokenEhValido(token, userDetails)) {

                //objeto de autenticação do spring
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                //detalhes da requisição , ip origem, sessão, request
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // adicionar no contexto a autenticação
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        //seguir fluxo da requisição
        filterChain.doFilter(request, response);
    }
}