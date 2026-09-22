# Plano aprovado

## Processo

A Fase 0 foi aprovada pelo usuário, incluindo um repositório independente chamado
`kanban-api`. Continuar as próximas fases conforme autorização posterior do usuário, explicando e validando cada unidade.
Cada unidade funcional recebe testes pertinentes, revisão do diff e commit próprio.
Não alterar datas nem reconstruir um histórico artificial.

## Entregas

1. Bootstrap, PostgreSQL, Flyway e Docker mínimo — concluído e validado.
2. Responsável: modelo, migration, CRUD, unicidade de e-mail e testes — concluído.
3. Projeto: domínio, status e métricas com testes de limites — concluído.
4. Projeto: persistência e CRUD com recálculo e testes — concluído.
5. Kanban: 12 transições, mensagens específicas, atomicidade e listagem por status — concluído.
6. Consolidar erros, paginação, ordenação e filtros aprovados.
7. OpenAPI, Docker final e GitHub Actions executando `mvn verify`.
8. Consolidar README, ADRs, coleção de API, diagrama e AI_USAGE.md.
9. Somente depois do núcleo validado: diferenciais selecionados.

Regras de status/métricas antecedem a conclusão do CRUD de projetos. Paginação
básica e tratamento de erros acompanham os primeiros endpoints. Testes e registros
documentais evoluem junto das funcionalidades, não apenas no fim.

## Arquitetura

Monólito Spring Boot, Java 21, Maven e PostgreSQL. Camadas pragmáticas:
`domain/model`, `domain/service`, `domain/exception`, `application/service`,
`application/dto`, `application/mapper`, `infrastructure/repository`,
`infrastructure/config`, `presentation/controller` e `presentation/exception`.

Entidades de domínio anotadas com JPA, aceitando esse acoplamento para evitar
modelos duplicados. Regras testáveis sem Spring. Interfaces apenas quando exigidas
por uma responsabilidade real. Sem Lombok/MapStruct inicialmente.

## Modelo e convenções

IDs Long. Project: name, responsibles, plannedStartDate, plannedEndDate,
actualStartDate, actualEndDate, createdAt e updatedAt. Responsible: name, email,
role, department, createdAt e updatedAt. Status, delayDays e
remainingTimePercentage são calculados e não graváveis pelo cliente.

ManyToMany unidirecional, tabela de vínculo sem atributos e par único.
Pelo menos um responsável por projeto. Secretaria inicialmente textual.
Normalizar e-mail e garantir unicidade no banco. Excluir responsável vinculado
retorna 409; excluir projeto preserva responsáveis.

Campos/métodos em inglês e camelCase, tipos em UpperCamelCase. Preservar os tokens
A_INICIAR, EM_ANDAMENTO, ATRASADO e CONCLUIDO, convenções dos frameworks e nomes
obrigatórios de arquivos. DTOs sem expor entidades JPA; schemas públicos não usam
sufixos Dto/List. Não há integração Saturno no escopo.

## Datas, status e métricas

- Precedência: CONCLUIDO, ATRASADO, EM_ANDAMENTO, A_INICIAR.
- Conclusão vence as outras regras; atraso pode decorrer de início ou término.
- Projeto iniciado, não concluído e sem atraso está em andamento, inclusive no
  dia do vencimento e quando não tem término previsto.
- Datas previstas podem ser nulas. Validar término >= início para cada par
  previsto/realizado quando ambos existirem. Datas realizadas não podem ser futuras.
- Permitir conclusão sem início realizado, preservando a transição direta exigida.
- LocalDate nas datas; Instant na auditoria; Clock configurável, inicialmente
  America/Fortaleza. Capturar hoje uma vez por operação.
- Percentual calculado pela fórmula do enunciado, limitado a 0..100, duas casas.
  Conclusão, informação insuficiente ou duração <= 0 retornam zero.
- Dias de atraso consideram somente término previsto vencido, sem conclusão.
  Um projeto atrasado por início pode ter zero dias de atraso.
- Recalcular na criação, edição e leitura. Filtro por status ocorre antes de
  paginar no banco e deve ser testado contra a política de domínio.

## Transições

Serviço específico, sem setter público de status. Carregar, calcular origem,
aplicar efeitos, validar datas, recalcular e conferir destino em uma transação.
Rejeições não persistem efeitos parciais. Mesmo status é idempotente.

- A iniciar -> Em andamento: início realizado = hoje.
- A iniciar -> Atrasado: sem efeito; bloquear antecipação e exigir atraso real.
- A iniciar -> Concluído: término realizado = hoje.
- Em andamento -> A iniciar: início realizado = null; rejeitar se resultar em atraso.
- Em andamento -> Atrasado: sem efeito; orientar ajuste das datas.
- Em andamento -> Concluído: término realizado = hoje.
- Atrasado -> A iniciar: sem efeito; orientar ajuste das datas.
- Atrasado -> Em andamento: sem efeito; orientar ajuste das datas.
- Atrasado -> Concluído: término realizado = hoje.
- Concluído -> A iniciar: término realizado = null; exigir ausência de início e atraso.
- Concluído -> Em andamento: término realizado = null; exigir início e ausência de atraso.
- Concluído -> Atrasado: término realizado = null; exigir atraso real.

A remoção automática de término em Concluído -> A iniciar segue o pedido do usuário,
que diverge do PDF nesse ponto. Transições sem efeito não fabricam status; ajustar
as datas por edição recalcula o status. O PDF cita confirmações obrigatórias sem
defini-las: não inventar um protocolo de confirmação.

## HTTP

- POST/GET /api/responsibles; GET/PUT/DELETE /api/responsibles/{id}.
- POST/GET /api/projects; GET/PUT/DELETE /api/projects/{id}.
- GET /api/kanban/projects?status=... e PATCH /api/projects/{id}/status.
- Paginação page/size/sort, limite de tamanho e desempate estável.
- Erros: 400 entrada inválida; 404 inexistente; 409 conflito de unicidade/vínculo;
  422 inconsistência de datas ou transição inviável.
- Resposta de erro: timestamp, status, error, message e path.

## Checklist de aceite final

- [ ] CRUDs, vínculos, unicidade e invariantes validados.
- [ ] Status, métricas, nulos e limites ontem/hoje/amanhã cobertos.
- [ ] Doze transições cobertas e rollback demonstrado.
- [ ] Leituras refletem passagem do tempo; paginação e totais corretos.
- [ ] Integração PostgreSQL verifica migrations, constraints e transações.
- [ ] API verifica contratos, validações e códigos HTTP.
- [ ] Docker, Swagger e CI validados.
- [ ] README, ADRs, coleção e diagrama correspondem à implementação.
- [ ] AI_USAGE descreve o processo e uma revisão humana real de sugestão da IA.
- [ ] Entrega pública no GitHub preparada.

Diferenciais possíveis: indicadores, filtros avançados, GraphQL, CRUD de Secretaria,
arquitetura documental de IA/RAG, observabilidade, autenticação e UI. Não implementar
antes do obrigatório. CI é obrigatório por solicitação do usuário.
