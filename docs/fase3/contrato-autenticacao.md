# Contrato de autenticação — cliente por CPF

Contrato entre `oficina-lambda` (emite e valida na borda) e `oficina`
(revalida e autentica). Mudar qualquer coisa aqui sem alinhar os dois lados
quebra a autenticação.

## `POST /auth/token`

Rota aberta do API Gateway (`authorization_type = NONE`), integração
`AWS_PROXY` para a Lambda `auth-token`.

### Request

```json
{ "cpf": "529.982.247-25" }
```

CPF aceito **com ou sem máscara** — a função normaliza (`\D` removido)
antes de validar e consultar. A validação dos dígitos verificadores espelha
o `ValidadorCPF` da API Spring; as duas implementações precisam concordar,
senão o cliente recebe um token que a aplicação nunca honra.

### Respostas

| Status | Condição |
| --- | --- |
| `200` | CPF válido, cliente existe e está ativo |
| `400` | Corpo sem `cpf`, não-JSON, ou CPF com tamanho/dígitos/sequência inválidos (`111.111.111-11` cai aqui) |
| `403` | CPF válido, cliente existe, mas `ativo = false` |
| `404` | CPF válido, nenhum cliente com esse documento |
| `503` | Falha ao alcançar o RDS |

**Sucesso:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "expiraEm": "2026-09-10T14:32:05.000Z",
  "cliente": { "id": 1, "nome": "Cliente E2E Ativo" }
}
```

**Erro:**

```json
{ "erro": "CLIENTE_INATIVO", "mensagem": "...", "correlationId": "..." }
```

Toda resposta (sucesso ou erro) devolve o header `x-correlation-id`.

## Claims do JWT de cliente

| Claim | Tipo | Descrição |
| --- | --- | --- |
| `sub` | string | CPF **só dígitos** (normalizado) |
| `tipo` | string | Sempre `"CLIENTE"` — é por ela que o `JwtAuthenticationFilter` decide o caminho de autenticação |
| `clienteId` | number | ID numérico do cliente. Precisa ser número: o filtro do Spring lê a claim como `Number` e recusa o token se vier texto |
| `nome` | string | Nome do cliente, evita ida ao banco só para exibir |
| `role` | string | `"CLIENTE"` — **não concede privilégio**: a authority efetiva é sempre fixada como `ROLE_CLIENTE` pelo filtro, seja qual for o valor desta claim |
| `iat` / `exp` | number | Emissão e expiração (validade padrão 7.200.000 ms = 2h, mesma do `security.jwt.expiration`) |

Assinatura **HS256** (biblioteca `jose` do lado da Lambda, `jjwt` do lado
Spring). O algoritmo é fixado explicitamente na verificação
(`algorithms: ["HS256"]`) — aceitar o que vier no header abriria a porta
para um token com `alg: none`.

## O segredo compartilhado

O mesmo valor precisa estar em três lugares:

| Onde | Como chega |
| --- | --- |
| API Spring | `SECURITY_JWT_SECRET`, no `Secret` do Kubernetes |
| Lambda `auth-token` | variável de ambiente, definida pelo Terraform a partir de `var.jwt_secret` |
| Lambda `authorizer` | idem |

Mínimo de 32 caracteres (validação no `variables.tf`). Se divergirem, o
sintoma é um `403` na rota protegida com um token que foi emitido com
sucesso — ver [RFC-0002](../arquitetura/rfc/0002-estrategia-de-autenticacao.md).

## Validação em duas camadas

1. **Lambda `authorizer`** (borda): `jwtVerify` (assinatura + expiração),
   sem tocar no banco. Devolve `{ isAuthorized, context }` — *simple
   response*, não policy IAM. Resultado cacheado por 300s, com chave no
   header `Authorization`.
2. **Aplicação** (`JwtAuthenticationFilter`): revalida assinatura e
   expiração, lê `clienteId`/`sub`/`nome`, monta o principal
   `ClienteAutenticado` e autentica com `ROLE_CLIENTE`. Também sem tocar no
   banco para esse tipo de token.

Um token de cliente **nunca** é confrontado com a tabela `usuario` — esse
caminho é exclusivo do token de operador.

## Consulta ao cliente

A Lambda consulta o RDS **diretamente**, não pela API:

```sql
SELECT id, nome, ativo
  FROM cliente
 WHERE regexp_replace(cpf_cnpj, '\D', '', 'g') = $1
 LIMIT 1
```

Duas razões: a rota de clientes da API é protegida (usá-la criaria
dependência circular de autenticação), e a coluna `cpf_cnpj` guarda o
documento em formato livre — normalizar os dois lados é o que evita não
encontrar um cliente cadastrado com pontuação.

A migration `V10__index_cpf_cnpj_normalizado.sql` cria um índice funcional
sobre **exatamente essa expressão**. Se a expressão do `WHERE` mudar de um
caractere, o planejador do PostgreSQL deixa de usar o índice e a consulta
vira seq scan a cada login.

## Correlação de requisições

`x-correlation-id` atravessa os quatro pontos — ver
[ADR-0005](../arquitetura/adr/0005-observabilidade-da-aplicacao.md).

## Gerando um token localmente (sem AWS)

`scripts/gerar-token-cliente.py` reproduz a assinatura da Lambda, para
testar a aplicação sem infraestrutura:

```bash
python3 scripts/gerar-token-cliente.py 52998224725 1 "Cliente Teste" "$SECURITY_JWT_SECRET"
```

O segredo precisa ser idêntico ao do ambiente onde o token será usado.
