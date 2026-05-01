package br.com.prime.oficina.config;

import br.com.prime.oficina.auth.gestaousuarios.domain.RoleUsuario;
import br.com.prime.oficina.auth.gestaousuarios.domain.Usuario;
import br.com.prime.oficina.security.JwtService;
import br.com.prime.oficina.security.domain.SecurityUserDetails;

public abstract class ControllerIntegrationTestSupport {

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
}