# ADR-0004 — Dois tipos de token JWT, distinguidos pela claim `tipo`

## Status

Aceito e implementado

## Contexto

Até a Fase 2, a aplicação tinha um único mecanismo de autenticação: operador com e-mail e senha,
token emitido e validado por ela mesma, com o `sub` contendo o e-mail e o usuário recarregado do
banco a cada requisição.

A Fase 3 introduziu um segundo emissor — a function `auth-token` do `oficina-lambda` — que emite
token para o **cliente**, identificado por CPF. Esse cliente não existe na tabela `usuario`: ele vive
em `cliente`, e não tem senha.

O caminho de operador não podia quebrar: havia uma suíte de testes de controller inteira apoiada
nele.

## Alternativas consideradas

| Opção | Prós | Contras |
|---|---|---|
| **Claim `tipo` diferenciando `OPERADOR` e `CLIENTE`, com ramificação no filtro** | Mudança aditiva; o caminho de operador não muda; o token diz o que é | Duas classes de principal para o resto do código considerar |
| Criar um registro em `usuario` para cada cliente | O filtro não mudaria | Duplica identidade em duas tabelas e exigiria senha fictícia para um ator que não tem senha |
| Emissor separado com outro segredo e outro filtro | Isolamento total entre os fluxos | Dois segredos para gerenciar e duas cadeias de filtro para manter, sem ganho real |
| Deduzir o tipo pelo formato do `sub` (CPF vs e-mail) | Nenhuma claim nova | Frágil e implícito: uma regra de negócio dependendo de regex sobre o assunto do token |

## Decisão

O token carrega a claim **`tipo`** (enum `TipoToken`), e o `JwtAuthenticationFilter` ramifica por
ela:

- **`OPERADOR`** — caminho pré-existente: `sub` é o username, o usuário é recarregado do banco a
  cada requisição, e as authorities vêm do registro em `usuario`.
- **`CLIENTE`** — `jwtService.tokenValido(token)` verifica assinatura e expiração, **sem tocar no
  banco**. As claims viram um principal próprio, o record `ClienteAutenticado(id, cpf, nome)`, e a
  authority concedida é **sempre `ROLE_CLIENTE`**, independentemente do valor da claim `role`.

Um token **sem** a claim `tipo` é tratado como `OPERADOR`, por compatibilidade com tokens emitidos
antes da Fase 3.

Qualquer exceção ao extrair claims resulta em "sem autenticação" — a cadeia de filtros segue e o
Spring Security decide o status conforme a regra da rota, em vez de estourar uma exceção.

## Justificativa

1. **A authority não pode vir do token.** Se o filtro confiasse na claim `role`, quem conseguisse
   forjar um token (ou trocar a claim numa reemissão) escolheria o próprio perfil. Fixar
   `ROLE_CLIENTE` no código remove essa possibilidade.
2. **Cliente não é recarregado do banco de propósito.** O ganho de validar contra a base não
   compensaria uma consulta a cada requisição — e a function já verificou existência e status no
   momento da emissão. O custo aceito é a janela de até 2h em que um cliente desativado continua com
   token válido.
3. **Compatibilidade sem migração.** Tratar token sem `tipo` como operador evitou invalidar sessões
   em curso na virada.
4. **Esta é a segunda metade da dupla validação.** O `authorizer` valida na borda; sem este
   ramo no filtro, o token de cliente chegaria à aplicação e seria tratado como operador — o `sub`
   (um CPF) seria procurado como e-mail em `usuario`, não encontrado, e a requisição falharia.

## Consequências

- **Positivas**: mudança aditiva e contida em `security`; nenhum caso de uso de domínio foi tocado;
  três suítes de teste novas (`JwtServiceTest`, `JwtAuthenticationFilterTest`,
  `TokenClienteControllerTest`) cobrem o comportamento.
- **Negativas / trade-offs**:
  - O contrato das claims passa a ser compartilhado com outro repositório. Mudar `tipo`,
    `clienteId` ou o algoritmo quebra a autenticação dos dois lados — por isso o contrato está
    documentado em `docs/fase3/contrato-autenticacao.md`.
  - `clienteId` precisa ser numérico no token: o filtro lê a claim como `Number` e recusa o token se
    vier como texto.
  - **A autorização não distingue perfis por rota.** `SecurityConfig` usa
    `.anyRequest().authenticated()` e não há `@PreAuthorize` no projeto, então um token de cliente
    alcança as mesmas rotas que um de operador. O enunciado exige autenticação, que está atendida;
    separar autorização por perfil e validar posse do recurso é o próximo passo natural, e precisa
    ser feito com a suíte de testes rodando.
