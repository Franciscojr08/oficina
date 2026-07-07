package br.com.prime.oficina.auth.gestaousuarios.infrastructure;

import br.com.prime.oficina.auth.gestaousuarios.application.gateway.UsuarioGateway;
import br.com.prime.oficina.auth.gestaousuarios.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>, UsuarioGateway {
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}