# Diagrama de Sequência — Autenticação (Fase 3)

Dois fluxos: a emissão do token de cliente pela Lambda `auth-token` e a
validação de uma requisição protegida (Lambda `authorizer` na borda +
revalidação independente na aplicação).

## 1. Emissão do token de cliente

```mermaid
sequenceDiagram
    actor C as Cliente
    participant GW as API Gateway
    participant AT as Lambda auth-token
    participant DB as RDS PostgreSQL

    C->>GW: POST /auth/token {"cpf": "529.982.247-25"}
    Note over GW: rota aberta (authorization_type = NONE)
    GW->>AT: AWS_PROXY (payload 2.0)
    AT->>AT: herda x-correlation-id ou usa requestId
    AT->>AT: cpfEhValido() + normalizarCpf() → só dígitos

    alt CPF ausente, corpo inválido ou dígitos verificadores errados
        AT-->>GW: 400 {erro, mensagem, correlationId}
        GW-->>C: 400
    else CPF válido
        AT->>DB: SELECT id, nome, ativo FROM cliente<br/>WHERE regexp_replace(cpf_cnpj,'\D','','g') = $1
        alt falha de conexão com o banco
            DB--xAT: erro
            AT-->>GW: 503 base indisponível
            GW-->>C: 503
        else nenhuma linha
            DB-->>AT: vazio
            AT-->>GW: 404 cliente não encontrado
            GW-->>C: 404
        else cliente com ativo = false
            DB-->>AT: linha (ativo=false)
            AT-->>GW: 403 cliente inativo
            GW-->>C: 403
        else cliente ativo
            DB-->>AT: linha (ativo=true)
            AT->>AT: assina HS256 (jose):<br/>sub=cpf, tipo=CLIENTE, clienteId, nome, role
            AT-->>GW: 200 {token, tipo:"Bearer", expiraEm, cliente:{id,nome}}
            GW-->>C: 200
        end
    end
```

O log da `auth-token` registra `cpf` como **digital SHA-256 truncada em 12
caracteres**, nunca em claro — permite correlacionar tentativas do mesmo
documento sem gravar dado pessoal.

## 2. Requisição protegida (`$default`)

```mermaid
sequenceDiagram
    actor C as Cliente
    participant GW as API Gateway ($default)
    participant AZ as Lambda authorizer
    participant LB as LoadBalancer (EKS)
    participant API as oficina-api (Spring)

    C->>GW: GET /oficina/v1/ordens/cliente/{id}<br/>Authorization: Bearer <token>

    alt header Authorization ausente
        Note over GW: identity_sources exige o header —<br/>o Gateway nem invoca o authorizer
        GW-->>C: 401 Unauthorized
    else header presente
        alt resultado em cache (mesmo token, < 300s)
            Note over GW: authorizer_result_ttl_in_seconds = 300
        else primeira vez
            GW->>AZ: REQUEST authorizer (payload 2.0)
            AZ->>AZ: jwtVerify(token, segredo, {algorithms:["HS256"]})
            alt assinatura inválida, expirado ou "Bearer" ausente
                AZ-->>GW: {isAuthorized: false, context: {}}
                GW-->>C: 403 Forbidden
            else token válido
                AZ-->>GW: {isAuthorized: true,<br/>context: {tipo, clienteId, correlationId}}
            end
        end
        GW->>LB: HTTP_PROXY para http://<lb-dns><br/>overwrite header x-correlation-id = $context.authorizer.correlationId
        LB->>API: encaminha
        API->>API: CorrelationIdFilter → MDC (todo log da requisição sai correlacionado)
        API->>API: JwtAuthenticationFilter lê a claim "tipo"
        alt tipo = CLIENTE
            API->>API: jwtService.tokenValido() — assinatura + expiração, sem ir ao banco
            API->>API: principal ClienteAutenticado, authority fixa ROLE_CLIENTE
        else tipo = OPERADOR (ou claim ausente, por compatibilidade)
            API->>API: recarrega o usuário do banco e valida o token contra ele
        end
        alt revalidação falha (ex.: segredo divergente entre Lambda e API)
            API-->>C: 403
        else revalidação ok
            API->>API: regra de negócio
            API-->>C: 200 + corpo
        end
    end
```

### Por que 401 num caso e 403 no outro

Não é escolha da aplicação: o `identity_sources = ["$request.header.Authorization"]`
faz o **API Gateway** responder `401` sozinho quando o header não existe,
sem sequer invocar o authorizer. Quando o header existe mas o token é
inválido, quem responde é o authorizer com `isAuthorized: false`, e o
Gateway traduz isso em `403`.

### O que o `context` do authorizer significa (e o que não significa)

O authorizer devolve `tipo`, `clienteId` e `correlationId` no `context`. Só
o `correlationId` é efetivamente consumido (o Gateway o injeta no header
para o backend). `tipo` e `clienteId` servem para log — **não são fonte de
autorização**, porque a aplicação revalida o token por conta própria de
qualquer forma. Isso é deliberado, ver
[ADR-0001 do oficina-lambda](https://github.com/rremiao/oficina-lambda/blob/main/docs/adr/0001-dupla-validacao-jwt.md).

## Rotas que não passam pelo authorizer

Definidas em `local.rotas_abertas` (`oficina-lambda/infra/main.tf`):

| Rota | Por quê |
| --- | --- |
| `POST /auth/token` | É onde o token nasce; não há token para validar antes dela |
| `POST /oficina/v1/auth/login` | Troca de credencial por token do operador — mesmo motivo |
| `ANY /oficina/v1/public/{proxy+}` | Rotas públicas por definição |
| `GET /oficina/v1/swagger-ui.html` e `/swagger-ui/{proxy+}` | Documentação navegável |
| `GET /oficina/v1/api-docs` e `/api-docs/{proxy+}` | Contrato OpenAPI (também usado pelas probes) |

O cadastro de usuários (`/oficina/v1/usuarios`) **continua sob o
authorizer** — foi decisão explícita não abri-lo.

## Login de funcionário

Segue exatamente como na Fase 2: `POST /oficina/v1/auth/login` com e-mail e
senha, a aplicação valida o hash BCrypt e emite um token com
`tipo = OPERADOR`. A única diferença é que agora a requisição chega pelo
Gateway em vez de direto no LoadBalancer.

Ver também [RFC-0002 — estratégia de autenticação](rfc/0002-estrategia-de-autenticacao.md) e
[ADR-0004 — dois tipos de token](adr/0004-dois-tipos-de-token-jwt.md).
