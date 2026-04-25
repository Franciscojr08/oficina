package br.com.prime.oficina.auth.gestaoUsuarios.application;
import br.com.prime.oficina.auth.gestaoUsuarios.domain.Usuario;
import br.com.prime.oficina.auth.gestaoUsuarios.infrastructure.UsuarioRepository;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new RegraNegocioException("Já existe usuário cadastrado com este email");
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
