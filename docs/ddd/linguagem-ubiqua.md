# Linguagem Ubiqua - Oficina API

Este documento define os termos do dominio usados no projeto Oficina API.
A Linguagem Ubiqua deve ser usada de forma consistente por negocio, documentacao,
diagramas DDD, Event Storming, codigo, testes e endpoints da API.

## Objetivo

A Linguagem Ubiqua reduz ambiguidades entre pessoas de negocio e equipe tecnica.
No contexto da oficina, os mesmos nomes devem representar os mesmos conceitos em
conversas, fluxos, diagramas, codigo e documentacao.

Exemplo aplicado:

- No negocio: "A ordem de servico foi aprovada pelo cliente".
- No Event Storming: evento `OrdemServicoAprovada`.
- Na API: endpoint `PATCH /ordens/{id}/aprovar`.
- No codigo: metodo `aprovarOrdemServico`.
- No banco/modelo: entidade `OrdemServico`.

## Contexto do Dominio

O sistema apoia a operacao de uma oficina mecanica. Ele permite cadastrar
clientes, veiculos, servicos, itens, controlar estoque, criar ordens de servico,
acompanhar seu ciclo de vida e consultar indicadores operacionais.

Os principais subdominios tratados pelo projeto sao:

- Atendimento e abertura de OS.
- Execucao e acompanhamento da OS.
- Catalogo de servicos.
- Gestao de pecas, itens e insumos.
- Controle de estoque.
- Autenticacao e gestao de usuarios.
- Relatorios operacionais.

## Glossario Principal

| Termo | Definicao no dominio | Representacao no projeto |
| --- | --- | --- |
| Cliente | Pessoa ou empresa que solicita atendimento na oficina. | `Cliente`, `/clientes` |
| Veiculo | Automovel pertencente a um cliente e atendido pela oficina. | `Veiculo`, `/veiculos` |
| Ordem de Servico | Registro principal do atendimento. Contem cliente, veiculo, problema, status, itens, servicos, valores e datas. | `OrdemServico`, `/ordens` |
| OS | Abreviacao de Ordem de Servico. Deve ser usada apenas quando o contexto ja estiver claro. | Mesmo conceito de `OrdemServico` |
| Codigo da OS | Identificador publico da ordem de servico usado para consulta e acompanhamento. | Campo `codigo`, `/public/ordens/codigo/{codigo}` |
| Problema relatado | Descricao inicial do defeito, sintoma ou necessidade informada pelo cliente. | Campo `descricaoProblema` |
| Observacoes gerais | Informacoes complementares registradas durante o atendimento. | Campo `observacoesGerais` |
| Diagnostico | Etapa em que a oficina avalia o veiculo e identifica servicos e itens necessarios. | Acao `/ordens/{id}/iniciar-diagnostico` |
| Orcamento | Composicao de servicos, itens e valores proposta ao cliente antes da execucao. | OS com itens, servicos e totais |
| Aprovacao | Aceite do cliente para execucao da ordem de servico. | `/ordens/{id}/aprovar` |
| Reprovacao | Recusa do cliente sobre o orcamento ou execucao proposta. | `/ordens/{id}/reprovar` |
| Execucao | Etapa em que os servicos aprovados sao realizados pela oficina. | Status `EM_EXECUCAO` |
| Entrega | Encerramento do atendimento com devolucao do veiculo ao cliente. | `/ordens/{id}/entregar` |
| Servico | Atividade executada pela oficina, como revisao, troca, alinhamento ou diagnostico. | `Servico`, `/servicos` |
| Servico da OS | Servico vinculado a uma ordem especifica, com status e datas proprias. | `ServicoOrdemServico` |
| Item | Produto controlado pelo sistema e usado pela oficina. Pode representar peca ou insumo. | `Item`, `/itens` |
| Peca | Item aplicado diretamente no reparo ou manutencao do veiculo. | `TipoItem.PECA` |
| Insumo | Item consumivel usado pela oficina durante a operacao. | `TipoItem` conforme enum do projeto |
| Item da OS | Item vinculado a uma ordem de servico, com quantidade e valor unitario. | `ItemOrdemServico` |
| Estoque | Quantidade disponivel de um item. | `Estoque`, `/estoques` |
| Estoque minimo | Quantidade minima recomendada para um item. | Campo `estoqueMinimo` |
| Movimentacao de estoque | Registro de entrada, saida ou ajuste de um item no estoque. | `MovimentoEstoque`, `/movimentacoes-estoque` |
| Entrada de estoque | Aumento da quantidade disponivel de um item. | `TipoMovimentoEstoque.ENTRADA` |
| Saida de estoque | Baixa da quantidade disponivel, normalmente por uso em OS aprovada. | `TipoMovimentoEstoque.SAIDA` |
| Ajuste de estoque | Correcao manual da quantidade disponivel de um item. | `TipoMovimentoEstoque.AJUSTE` |
| Atendente | Usuario operacional responsavel por cadastros e atendimento. | `RoleUsuario.ATENDENTE` |
| Administrador | Usuario com permissao administrativa. | `RoleUsuario.ADMIN` |
| Relatorio operacional | Indicador usado para acompanhar desempenho da oficina. | `/relatorios` |
| Tempo medio da OS | Tempo medio entre inicio e fim de execucao de ordens finalizadas ou entregues. | `/relatorios/ordens-servico/tempo-medio` |
| Tempo medio dos servicos | Tempo medio entre inicio e fim dos servicos finalizados. | `/relatorios/ordens-servico/tempo-medio-servicos` |

## Estados da Ordem de Servico

| Status | Significado |
| --- | --- |
| `RECEBIDA` | A ordem foi criada e registrada pela oficina. |
| `EM_DIAGNOSTICO` | O veiculo esta em analise tecnica. |
| `AGUARDANDO_APROVACAO` | A composicao da OS foi enviada ou esta pronta para decisao do cliente. |
| `EM_EXECUCAO` | A OS foi aprovada e esta sendo executada. |
| `FINALIZADA` | A execucao foi concluida, mas o veiculo ainda nao foi entregue. |
| `ENTREGUE` | O veiculo foi entregue ao cliente. |
| `CANCELADA` | A OS foi encerrada sem continuidade, geralmente por reprovacao ou cancelamento. |

## Estados do Servico da OS

| Status | Significado |
| --- | --- |
| `PENDENTE` | O servico foi adicionado a OS, mas ainda nao foi iniciado. |
| `INICIADO` | O servico esta em execucao. |
| `FINALIZADO` | O servico foi concluido. |
| `CANCELADO` | O servico nao sera executado ou foi cancelado junto com a OS. |

## Comandos do Dominio

Comandos representam intencoes de usuarios ou do sistema. No Event Storming,
normalmente ficam antes dos eventos.

### Fluxo de OS

- `CriarOrdemServico`
- `IniciarDiagnostico`
- `AdicionarItemNaOrdemServico`
- `AdicionarServicoNaOrdemServico`
- `SolicitarAprovacao`
- `AprovarOrdemServico`
- `ReprovarOrdemServico`
- `IniciarServico`
- `FinalizarServico`
- `EntregarVeiculo`
- `AcompanharOrdemServicoPorCodigo`

### Fluxo de pecas, itens e estoque

- `CadastrarItem`
- `AtualizarItem`
- `InativarItem`
- `AtualizarEstoque`
- `RegistrarEntradaEstoque`
- `RegistrarSaidaEstoque`
- `RegistrarAjusteEstoque`
- `ConsultarMovimentacoesEstoque`

### Fluxo de cadastros auxiliares

- `CadastrarCliente`
- `AtualizarCliente`
- `InativarCliente`
- `CadastrarVeiculo`
- `AtualizarVeiculo`
- `InativarVeiculo`
- `CadastrarServico`
- `AtualizarServico`
- `InativarServico`

## Eventos do Dominio

Eventos representam fatos que ja aconteceram no dominio.

### Criacao e acompanhamento da OS

- `OrdemServicoCriada`
- `DiagnosticoIniciado`
- `ItemAdicionadoNaOrdemServico`
- `ServicoAdicionadoNaOrdemServico`
- `AprovacaoSolicitada`
- `OrdemServicoAprovada`
- `OrdemServicoReprovada`
- `OrdemServicoCancelada`
- `ServicoIniciado`
- `ServicoFinalizado`
- `OrdemServicoFinalizada`
- `VeiculoEntregue`
- `OrdemServicoConsultadaPeloCliente`

### Gestao de pecas e insumos

- `ItemCadastrado`
- `ItemAtualizado`
- `ItemInativado`
- `EstoqueAtualizado`
- `EntradaEstoqueRegistrada`
- `SaidaEstoqueRegistrada`
- `AjusteEstoqueRegistrado`
- `EstoqueBaixadoPorAprovacaoDaOS`
- `EstoqueInsuficienteIdentificado`

## Regras de Negocio

As regras abaixo devem ser expressas com os termos da Linguagem Ubiqua.

- Uma OS so pode ser criada para cliente ativo.
- Uma OS so pode ser criada para veiculo ativo.
- O veiculo informado na OS deve pertencer ao cliente informado.
- Uma OS em execucao, finalizada, entregue ou cancelada nao deve receber novos itens ou servicos.
- Uma OS deve passar por diagnostico antes de solicitar aprovacao.
- Uma OS so deve ser aprovada quando estiver aguardando aprovacao.
- Uma OS reprovada deve ser cancelada.
- Ao aprovar uma OS, os itens vinculados devem gerar saida de estoque.
- A aprovacao da OS deve falhar quando o estoque de algum item for insuficiente.
- Um item inativo nao deve ser adicionado a uma OS.
- Um servico inativo nao deve ser adicionado a uma OS.
- Um servico da OS deve ser iniciado antes de ser finalizado.
- A OS so deve ser entregue quando sua execucao estiver concluida.
- O cliente pode acompanhar a OS pelo codigo publico da OS.

## Atores

| Ator | Responsabilidade |
| --- | --- |
| Cliente | Solicita atendimento e acompanha a OS pelo codigo publico. |
| Atendente | Registra clientes, veiculos, OS, itens e servicos. |
| Administrador | Gerencia usuarios, cadastros e operacao da oficina. |
| Sistema | Gera codigo da OS, valida regras, calcula totais e registra movimentacoes de estoque. |

## Politicas e Processos

Politicas sao regras ou processos que reagem a eventos.

| Evento | Politica aplicada |
| --- | --- |
| `OrdemServicoAprovada` | Baixar estoque dos itens vinculados a OS. |
| `EstoqueInsuficienteIdentificado` | Impedir aprovacao da OS. |
| `ServicoFinalizado` | Verificar se todos os servicos da OS foram concluidos. |
| `OrdemServicoFinalizada` | Permitir entrega do veiculo. |
| `OrdemServicoReprovada` | Cancelar OS e servicos pendentes/iniciados. |

## Mapeamento entre Linguagem e Codigo

| Linguagem Ubiqua | Codigo principal | Endpoint |
| --- | --- | --- |
| Cliente | `Cliente`, `ClienteService` | `/clientes` |
| Veiculo | `Veiculo`, `VeiculoService` | `/veiculos` |
| Ordem de Servico | `OrdemServico`, `OrdemServicoService` | `/ordens` |
| Item | `Item`, `ItemService` | `/itens` |
| Estoque | `Estoque`, `EstoqueService` | `/estoques` |
| Movimentacao de estoque | `MovimentoEstoque`, `MovimentoEstoqueService` | `/movimentacoes-estoque` |
| Servico | `Servico`, `ServicoService` | `/servicos` |
| Servico da OS | `ServicoOrdemServico`, `ServicoOrdemServicoService` | `/ordens/{id}/servicos` |
| Item da OS | `ItemOrdemServico`, `ItemOrdemServicoService` | `/ordens/{id}/itens` |
| Acompanhamento publico | `AcompanharOrdemServicoPublicaService` | `/public/ordens/codigo/{codigo}` |
| Relatorio operacional | `RelatorioService` | `/relatorios` |
| Usuario | `Usuario`, `UsuarioService` | `/usuarios` |

## Termos que devem ser evitados

Para manter consistencia, evitar sinonimos que criem ambiguidade.

| Evitar | Usar |
| --- | --- |
| Pedido | Ordem de Servico ou OS |
| Chamado | Ordem de Servico ou OS |
| Produto | Item, Peca ou Insumo |
| Material | Item, Peca ou Insumo |
| Baixa manual | Saida de estoque ou Ajuste de estoque, conforme o caso |
| Finalizar atendimento | Entregar veiculo, quando a OS ja estiver concluida |
| Usuario funcionario | Atendente |

## Aplicacao nos Diagramas DDD

Nos diagramas DDD, os nomes devem seguir os termos deste documento.

Sugestao de agrupamento por Bounded Context:

| Bounded Context | Conceitos principais |
| --- | --- |
| Atendimento | Cliente, Veiculo, Ordem de Servico, Codigo da OS |
| Execucao da OS | Diagnostico, Servico da OS, Item da OS, Aprovacao, Entrega |
| Estoque | Item, Peca, Insumo, Estoque, Movimentacao de estoque |
| Identidade e Acesso | Usuario, Atendente, Administrador |
| Relatorios | Tempo medio da OS, Tempo medio dos servicos |

## Aplicacao no Event Storming

### Fluxo: Criacao e acompanhamento da OS

1. `CadastrarCliente`
2. `ClienteCadastrado`
3. `CadastrarVeiculo`
4. `VeiculoCadastrado`
5. `CriarOrdemServico`
6. `OrdemServicoCriada`
7. `IniciarDiagnostico`
8. `DiagnosticoIniciado`
9. `AdicionarItemNaOrdemServico`
10. `ItemAdicionadoNaOrdemServico`
11. `AdicionarServicoNaOrdemServico`
12. `ServicoAdicionadoNaOrdemServico`
13. `SolicitarAprovacao`
14. `AprovacaoSolicitada`
15. `AprovarOrdemServico`
16. `OrdemServicoAprovada`
17. `EstoqueBaixadoPorAprovacaoDaOS`
18. `IniciarServico`
19. `ServicoIniciado`
20. `FinalizarServico`
21. `ServicoFinalizado`
22. `OrdemServicoFinalizada`
23. `EntregarVeiculo`
24. `VeiculoEntregue`
25. `AcompanharOrdemServicoPorCodigo`
26. `OrdemServicoConsultadaPeloCliente`

### Fluxo: Gestao de pecas e insumos

1. `CadastrarItem`
2. `ItemCadastrado`
3. `AtualizarEstoque`
4. `EntradaEstoqueRegistrada`
5. `RegistrarAjusteEstoque`
6. `AjusteEstoqueRegistrado`
7. `AprovarOrdemServico`
8. `EstoqueBaixadoPorAprovacaoDaOS`
9. `ConsultarMovimentacoesEstoque`

## Criterios de Consistencia

- Classes, metodos e testes devem usar os nomes do dominio sempre que possivel.
- Endpoints devem representar recursos do dominio, como `/ordens`, `/itens` e `/estoques`.
- Mensagens de erro devem usar termos conhecidos pelo negocio.
- Diagramas devem usar os mesmos nomes dos comandos, eventos e agregados.
- Novos termos devem ser adicionados a este documento antes de serem espalhados pelo codigo.
