# RFC-0001 — Escolha da nuvem: AWS

## Status

Aceito

## Contexto

O Tech Challenge Fase 3 exige API Gateway, function serverless, banco gerenciado, cluster Kubernetes
com escalabilidade e Terraform, com livre escolha de provedor. É preciso justificar formalmente a
escolha.

O ambiente disponível ao time é o **AWS Academy Learner Lab**: crédito acadêmico limitado,
credenciais temporárias que expiram em 4 horas e restrição de IAM — não é permitido criar roles nem
policies novas, apenas usar a `LabRole` existente.

## Alternativas consideradas

| Opção | Prós | Contras |
|---|---|---|
| **AWS (Academy Learner Lab)** | Cobre os cinco serviços exigidos (API Gateway, Lambda, RDS, EKS, Terraform); ambiente já fornecido pelo curso, sem custo para o time; foi a nuvem usada nas aulas | Credenciais expiram em 4h; sem permissão de criar IAM; integrações que exigem role própria ficam bloqueadas |
| Google Cloud Platform | Equivalentes maduros (API Gateway, Cloud Functions, Cloud SQL, GKE) | Exigiria conta própria e cartão de crédito; nenhum crédito acadêmico equivalente disponível ao time |
| Microsoft Azure | Equivalentes maduros (APIM, Functions, Azure SQL, AKS) | Mesmo problema de custo; APIM tem custo de entrada alto mesmo em tier básico |
| Híbrido (Kubernetes gerenciado num provedor, serverless em outro) | Aproveitaria o melhor de cada | Duplicaria a complexidade de credenciais, rede e observabilidade, sem ganho pedagógico |

## Decisão

**AWS**, via AWS Academy Learner Lab, região `us-east-1`.

## Justificativa

1. **Cobertura completa dos requisitos com serviços de primeira classe**: API Gateway HTTP API,
   Lambda, RDS PostgreSQL, EKS e provider Terraform maduro.
2. **Custo zero para o time.** O crédito acadêmico cobre o ciclo de subir, demonstrar e destruir.
3. **Continuidade.** Foi a nuvem usada nas fases anteriores e nas aulas; trocar agora gastaria tempo
   de aprendizado que o prazo da fase não tem.

## Consequências

- **Positivas**: nenhum custo direto; serviços integrados entre si, o que simplifica rede e
  permissões.
- **Negativas / trade-offs**:
  - **Credenciais de 4 horas** moldam toda a operação: pipelines de infraestrutura não conseguem
    aplicar de forma confiável (por isso `oficina-database` e `oficina-kubernetes` apenas validam
    Terraform), e o ambiente precisa ser recriado a cada sessão de demonstração.
  - **Sem criação de IAM**: a integração nativa da AWS com o New Relic ficou inviável, o que levou à
    escolha de agents dentro do cluster; e as Lambdas reutilizam a `LabRole` em vez de uma role com
    permissão mínima — o que, num ambiente real, seria inaceitável.
  - **Ambiente efêmero**: não há URL permanente de deploy, então o entregável "links para os deploys
    ativos" não se aplica.
