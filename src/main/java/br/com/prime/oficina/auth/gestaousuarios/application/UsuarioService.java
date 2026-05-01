package br.com.prime.oficina.auth.gestaousuarios.application;
import br.com.prime.oficina.auth.gestaousuarios.domain.Usuario;
import br.com.prime.oficina.auth.gestaousuarios.infrastructure.UsuarioRepository;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static br.com.prime.oficina.shared.exception.ExceptionMessage.EXISTING_USER_SAME_EMAIL;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new RegraNegocioException(EXISTING_USER_SAME_EMAIL);
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setRole(request.role());
        usuario.setAtivo(true);

        Usuario salvo = usuarioRepository.save(usuario);

        return new UsuarioResponse(
                salvo.getId(),
                salvo.getNome(),
                salvo.getEmail(),
                salvo.getRole(),
                salvo.getAtivo(),
                salvo.getDataCriacao(),
                salvo.getDataAtualizacao()
        );
    }
}
