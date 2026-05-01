package br.com.prime.oficina.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiGroupsConfig {

    @Bean
    public GroupedOpenApi autenticacaoOpenApi() {
        return GroupedOpenApi.builder()
                .group("autenticacao")
                .pathsToMatch("/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi gestaousuariosOpenApi() {
        return GroupedOpenApi.builder()
                .group("gestao-usuarios")
                .pathsToMatch("/usuarios/**")
                .build();
    }

    @Bean
    public GroupedOpenApi clienteOpenApi() {
        return GroupedOpenApi.builder()
                .group("cliente")
                .pathsToMatch("/clientes/**")
                .build();
    }

    @Bean
    public GroupedOpenApi veiculoOpenApi() {
        return GroupedOpenApi.builder()
                .group("veiculo")
                .pathsToMatch("/veiculos/**")
                .build();
    }

    @Bean
    public GroupedOpenApi servicoOpenApi() {
        return GroupedOpenApi.builder()
                .group("servico")
                .pathsToMatch("/servicos/**")
                .build();
    }

    @Bean
    public GroupedOpenApi itemOpenApi() {
        return GroupedOpenApi.builder()
                .group("item")
                .pathsToMatch("/itens/**")
                .build();
    }

    @Bean
    public GroupedOpenApi movimentoestoqueOpenApi() {
        return GroupedOpenApi.builder()
                .group("movimento-estoque")
                .pathsToMatch("/movimentacoes-estoque/**")
                .build();
    }

    @Bean
    public GroupedOpenApi estoqueOpenApi() {
        return GroupedOpenApi.builder()
                .group("estoque")
                .pathsToMatch("/estoques/**")
                .build();
    }

    @Bean
    public GroupedOpenApi ordemServicoOpenApi() {
        return GroupedOpenApi.builder()
                .group("ordem-servico")
                .pathsToMatch("/ordens/**")
                .build();
    }

    @Bean
    public GroupedOpenApi relatorioOpenApi() {
        return GroupedOpenApi.builder()
                .group("relatorio")
                .pathsToMatch("/relatorios/**")
                .build();
    }

    @Bean
    public GroupedOpenApi apipublicaOpenApi() {
        return GroupedOpenApi.builder()
                .group("api-publica")
                .pathsToMatch("/public/**")
                .build();
    }
}
