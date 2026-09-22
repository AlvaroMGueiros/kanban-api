# Uso de IA

## Ferramenta e escopo

OpenAI Codex foi usado para analisar o enunciado, propor o plano incremental, implementar testes, executar validações e redigir documentação. O usuário aprovou o repositório independente e a continuidade das fases.

## Processo aplicado

O trabalho foi dividido em bootstrap, banco e Docker, responsáveis, regras temporais, projetos, Kanban, filtros, OpenAPI, CI, documentação e indicador. Cada incremento funcional foi testado antes de um commit Conventional Commits. O histórico usa horários reais; nenhum commit foi retrodatado.

As validações incluíram testes unitários, MockMvc com PostgreSQL real via Testcontainers, migrations Flyway, build da imagem, healthchecks do Compose e OpenAPI. Ao final, `mvnw verify` executou 70 testes unitários e 43 de integração, todos aprovados.

## Revisões e discordâncias

Sugestões iniciais da IA também foram corrigidas ao serem confrontadas com o comportamento esperado:

- Um desenho filtrava status depois da paginação. Foi descartado porque produziria páginas vazias e totais incorretos; o filtro passou a rodar no PostgreSQL por `Specification`.
- Um rascunho de `CONCLUIDO -> A_INICIAR` limpava início e término realizados. A regra aprovada manda remover somente o término; a implementação foi corrigida e rejeita a transição quando as datas restantes não resultam em `A_INICIAR`.
- A auditoria usava precisão maior que a preservada pelo PostgreSQL. Testes revelaram a diferença, e os instantes passaram a ser normalizados para microssegundos.
