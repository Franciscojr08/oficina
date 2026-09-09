package br.com.prime.oficina.security.domain;

/**
 * Principal das requisições autenticadas por token de cliente.
 *
 * <p>Os dados vêm inteiramente das claims do token, sem consulta ao banco. O token é emitido pela
 * function serverless somente depois de confirmar que o cliente existe e está ativo.</p>
 *
 * @param id   identificador do cliente na tabela {@code cliente}
 * @param cpf  CPF sem pontuação, usado como {@code subject} do token
 * @param nome nome do cliente, para exibição
 */
public record ClienteAutenticado(Long id, String cpf, String nome) {
}
