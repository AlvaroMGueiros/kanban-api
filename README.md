# Kanban API

API Java para gerenciamento de projetos em um quadro Kanban, desenvolvida em
etapas a partir do desafio técnico. A Fase 1 entrega bootstrap, banco e Docker.
O planejamento aprovado está em [docs/implementationPlan.md](docs/implementationPlan.md).

## Stack

- Java 21 e Spring Boot 3.5.16.
- Maven Wrapper 3.3.4, com Maven 3.9.11.
- Spring Web, Data JPA, Bean Validation e Actuator.
- PostgreSQL 17.11 e Flyway, com versões das bibliotecas gerenciadas pelo Spring Boot.
- JUnit 5, MockMvc e Testcontainers; Failsafe executa testes de integração.

## Executar com Docker

Pré-requisito: Docker com Compose e engine Linux em execução.
Na raiz do repositório:

```bash
docker compose up --build
```

A API responde em `http://localhost:8080/actuator/health` com `{"status":"UP"}`.
O banco fica acessível localmente na porta `5433`, evitando o padrão `5432`
frequentemente usado por instalações locais. A comunicação entre containers usa
`postgres:5432`. A aplicação aguarda o healthcheck do PostgreSQL.

As portas, banco e credenciais locais podem ser alterados copiando `.env.example`
para `.env`. Os valores de exemplo são exclusivos para desenvolvimento local;
`.env` não é versionado. As portas são publicadas somente em `127.0.0.1`.

Para iniciar em segundo plano e aguardar ambos os serviços saudáveis:

```bash
docker compose up --build --wait
docker compose ps
```

Para parar preservando os dados:

```bash
docker compose down
```

O volume `postgresData` persiste os dados. Mudar as variáveis `POSTGRES_*` após a
primeira inicialização não altera usuários e bancos já existentes nesse volume.

O Dockerfile usa build com JDK 21 e runtime JRE 21, executa a aplicação com usuário
sem privilégios e verifica sua saúde por HTTP. O build da imagem empacota sem
executar testes: a validação completa deve ocorrer antes, com `./mvnw verify`,
pois os testes de integração precisam acessar o Docker do host.

## Executar Java localmente

Pré-requisitos: JDK 21, `JAVA_HOME` configurado e Docker ativo para o banco.
Não é necessário instalar Maven globalmente.

```bash
docker compose up -d --wait postgres
./mvnw spring-boot:run
```

No PowerShell, substitua `./mvnw` por `.\mvnw.cmd`.
A configuração local padrão é `jdbc:postgresql://localhost:5433/kanban`, usuário
`kanban` e senha `kanbanLocal`. Para outro banco, exporte `DATABASE_URL`,
`DATABASE_USERNAME` e `DATABASE_PASSWORD` no processo da aplicação.
O arquivo `.env` é lido pelo Compose, não pelo Spring Boot executado diretamente.
Não execute a API local e a API do Compose na mesma porta simultaneamente.

## Testes e build

```bash
./mvnw verify
```

Esse comando compila, executa os testes, gera o JAR e executa a integração com
PostgreSQL descartável via Testcontainers. Docker é obrigatório para a integração;
a falta do engine provoca falha, sem pular testes silenciosamente.
Nenhum banco de desenvolvimento é reutilizado pelo teste.

A Fase 1 contém dois testes de integração: resposta HTTP de saúde e aplicação das
migrations em banco vazio, incluindo validação de checksum e ausência de reaplicação.
Os relatórios ficam em `target/failsafe-reports`. `./mvnw test` executará apenas
os testes unitários, que serão introduzidos com as regras de negócio nas próximas
etapas; sozinho, ele não valida a integração desta fase.

## Banco e migrations

Flyway é o único responsável por alterar o schema. A primeira migration,
`V1__createKanbanSchema.sql`, cria o schema `kanban`, destinado às tabelas de negócio.
Seu histórico fica em `public.flyway_schema_history`, permitindo que o próprio
schema de negócio seja criado por uma migration versionada.

Hibernate usa `ddl-auto=validate`, com schema padrão `kanban`, e não cria tabelas.
`open-in-view=false` delimita o acesso ao banco às operações da aplicação.
As tabelas de responsáveis e projetos serão adicionadas em migrations próprias
nas próximas fases. Não há tabelas artificiais nem carga de dados demonstrativos.

## Estrutura atual

```text
src/main/java/br/com/alvar/kanban/  # Inicialização Spring Boot
src/main/resources/               # Configuração e migrations
src/test/java/br/com/alvar/kanban/ # Integração HTTP/PostgreSQL
.mvn/wrapper/                     # Maven reproduzível
Dockerfile                       # Build e runtime Java 21
docker-compose.yml               # Aplicação, PostgreSQL e volume
docs/implementationPlan.md        # Plano e decisões aprovadas
AI_USAGE.md                      # Registro contínuo do uso de IA
```

As camadas de domínio, aplicação, infraestrutura e apresentação serão criadas
conforme surgirem responsabilidades concretas; não há pacotes vazios.

## Limitações e próximas etapas

Apenas `/actuator/health` está exposto pelo Actuator, sem detalhes internos.
CRUD, cálculo de status, métricas de projetos, Kanban, Swagger, autenticação e CI
não fazem parte da Fase 1. A próxima entrega é o gerenciamento de responsáveis.

O repositório Git é local nesta etapa. A entrega pública e os diferenciais serão
tratados depois da validação do núcleo. Consulte o plano para os critérios de aceite.
