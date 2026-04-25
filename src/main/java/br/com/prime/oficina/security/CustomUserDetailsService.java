package br.com.prime.oficina.security;

import br.com.prime.oficina.auth.domain.Usuario;
import br.com.prime.oficina.auth.infrastructure.UsuarioRepository;
import br.com.prime.oficina.security.domain.SecurityUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return new SecurityUserDetails(usuario);
    }
}