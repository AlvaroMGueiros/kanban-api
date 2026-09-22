# Uso de IA

## Ferramenta utilizada

Durante o desenvolvimento utilizei o OpenAI Codex como ferramenta de apoio.

O uso da IA ficou concentrado principalmente em análise do enunciado, discussão de alternativas de implementação, revisão de regras de negócio, geração de cenários de teste e apoio na documentação.

A ferramenta foi usada como suporte ao desenvolvimento, e não como fonte única das decisões. As sugestões geradas foram revisadas e, quando necessário, alteradas ou descartadas antes de entrarem na solução.

## Como organizei o trabalho

Antes de iniciar a implementação, analisei o enunciado completo e dividi o desafio em partes menores:

- estrutura inicial da aplicação;
- banco de dados, migrations e Docker;
- cadastro de responsáveis;
- regras de datas e cálculo de status;
- CRUD de projetos;
- transições do Kanban;
- filtros e paginação;
- documentação OpenAPI;
- testes;
- CI;
- indicadores e documentação final.

A ideia foi trabalhar de forma incremental. Depois de cada conjunto funcional, eu validava o comportamento esperado e somente então fazia um commit relacionado àquela alteração.

Os commits foram feitos durante o desenvolvimento normal do projeto, sem alteração artificial de datas ou tentativa de reconstruir o histórico posteriormente.

Além dos testes unitários, utilizei testes de integração com PostgreSQL através de Testcontainers, MockMvc para a API, Flyway para validar as migrations e Docker Compose para conferir a execução da aplicação em um ambiente próximo ao descrito no desafio.

Ao final, `./mvnw verify` executava 70 testes unitários e 44 testes de integração com sucesso.

## Exemplo de interação com a IA

Um dos prompts que representa bem a forma como utilizei a ferramenta foi:

> Primeiro analise o enunciado inteiro. Depois crie um plano. Implemente em pequenas etapas independentes. Após cada etapa funcional concluída e validada, faça um commit Git pequeno e semanticamente coerente. Antes de cada commit, rode os testes relacionados àquela alteração.

Esse tipo de instrução foi usado principalmente para evitar implementar todo o desafio de uma vez e manter as alterações pequenas e verificáveis.

## Sugestões que foram alteradas ou rejeitadas

Nem todas as sugestões da IA foram mantidas.

Um exemplo ocorreu na filtragem por status. Em uma das propostas, o status calculado seria filtrado depois da paginação da consulta. Isso poderia gerar páginas parcialmente vazias e um `totalElements` diferente do resultado real apresentado ao cliente.

Por esse motivo, descartei essa abordagem e levei a condição para a consulta ao PostgreSQL utilizando `Specification`, permitindo que filtro e paginação fossem aplicados no mesmo nível.

Outro caso apareceu na transição de `CONCLUIDO` para `A_INICIAR`.

Uma implementação inicial propunha remover datas realizadas automaticamente. Ao revisar a tabela oficial de transições, percebi que `CONCLUIDO -> A_INICIAR` não possui ação automática: ela deve orientar a remoção do término realizado e o ajuste das datas por meio da edição do projeto.

A implementação foi ajustada para rejeitar essa transição sem modificar o cronograma. Isso também garante que uma tentativa inválida não deixe alterações parciais persistidas.

Também houve uma diferença nos testes de auditoria envolvendo `Instant`. A aplicação trabalhava inicialmente com precisão superior à armazenada pelo PostgreSQL. Isso fazia valores semanticamente equivalentes falharem em algumas comparações de integração.

A solução foi normalizar os instantes para precisão de microssegundos antes da persistência e das comparações.

## Considerações

A IA ajudou principalmente a acelerar análise, revisão e geração de alternativas, mas as decisões finais foram baseadas no comportamento exigido pelo desafio e confirmadas através dos testes.

Em alguns casos, os próprios testes foram importantes para mostrar que uma sugestão aparentemente válida não correspondia ao comportamento esperado.
