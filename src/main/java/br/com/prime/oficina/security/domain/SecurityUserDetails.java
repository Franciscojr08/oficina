package br.com.prime.oficina.security.domain;

import br.com.prime.oficina.auth.gestaoUsuarios.domain.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.List;

public class SecurityUserDetails implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String email;
    private final String senha;
    private final String role;
    private final Boolean ativo;

    public SecurityUserDetails(Usuario usuario) {
        this.email = usuario.getEmail();
        this.senha = usuario.getSenha();
        this.role = usuario.getRole().name();
        this.ativo = usuario.getAtivo();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(ativo);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    public String getRole() {
        return role;
    }
}