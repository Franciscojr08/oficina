package br.com.prime.oficina.auth;

import br.com.prime.oficina.auth.gestaousuarios.application.UsuarioRequest;
import br.com.prime.oficina.auth.gestaousuarios.application.UsuarioResponse;
import br.com.prime.oficina.auth.gestaousuarios.application.usecase.UsuarioUseCase;
import br.com.prime.oficina.auth.gestaousuarios.domain.RoleUsuario;
import br.com.prime.oficina.config.ControllerIntegrationTestSupport;
import br.com.prime.oficina.config.IntegrationTest;
import br.com.prime.oficina.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class UsuarioControllerTest extends ControllerIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @MockitoBean
    private UsuarioUseCase service;

    @Test
    void testCriar() throws Exception {
        UsuarioRequest request = criarUsuarioRequest();
        when(service.criar(request)).thenReturn(criarUsuario());

        mockMvc.perform(post("/usuarios")
                        .with(csrf())
                        .header("Authorization", bearerTokenAdmin(jwtService))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    private UsuarioRequest criarUsuarioRequest() {
        return new UsuarioRequest(
                "nome",
                "bob@domain.com",
                "minhaSenha",
                RoleUsuario.ATENDENTE
        );
    }

    private UsuarioResponse criarUsuario() {
        return new UsuarioResponse(
                1L,
                "nome",
                "bob@domain.com",
                RoleUsuario.ATENDENTE,
                Boolean.TRUE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
