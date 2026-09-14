package br.com.prime.oficina.security.domain;

/**
 * Origem do token JWT apresentado na requisição.
 *
 * <p>A distinção existe porque os dois tipos são validados de formas diferentes: o token de
 * operador tem um registro correspondente na tabela {@code usuario} e é recarregado do banco a
 * cada requisição, enquanto o token de cliente é emitido pela function serverless de autenticação
 * por CPF e não possui registro em {@code usuario}.</p>
 */
public enum TipoToken {

    /** Usuário interno da oficina, autenticado por e-mail e senha em {@code POST /auth/login}. */
    OPERADOR,

    /** Cliente da oficina, autenticado por CPF pela function serverless. */
    CLIENTE
}
