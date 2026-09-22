# Diagrama de arquitetura

```mermaid
flowchart TB
    Client[REST / Swagger / Postman] --> Controller[Controllers e erros]
    Controller --> App[Serviços e mapeadores]
    App --> Domain[Entidades e regras]
    App --> Repo[JPA e Specifications]
    Repo --> Pg[(PostgreSQL)]
    Flyway[Flyway migrations] --> Pg
```

O domínio calcula status, métricas e transições sem depender de Spring. A aplicação define transações, carrega entidades e captura a data de referência. Specifications traduzem status para SQL, preservando filtros e totais antes da paginação.

```mermaid
erDiagram
    RESPONSIBLES }o--o{ PROJECTS : projectResponsibles
    RESPONSIBLES {
        bigint id PK
        varchar email UK
        varchar name
        varchar role
        varchar department
    }
    PROJECTS {
        bigint id PK
        varchar name
        date plannedStartDate
        date plannedEndDate
        date actualStartDate
        date actualEndDate
        bigint version
    }
```
