# Kanban API

API Java para o desafio técnico de gerenciamento de projetos por Kanban.
Desenvolvimento incremental: o bootstrap é a primeira unidade da Fase 1.

## Stack

Java 21, Spring Boot 3.5.16 e Maven Wrapper. Spring Web, Bean Validation e
Actuator compõem a base; apenas o endpoint de saúde está exposto inicialmente.

## Executar

Com um JDK 21 instalado e `JAVA_HOME` configurado:

```bash
./mvnw spring-boot:run
```

No PowerShell, use `.\mvnw.cmd` no lugar de `./mvnw`.
A saúde está disponível em `http://localhost:8080/actuator/health`.

## Validar

```bash
./mvnw verify
```

O teste de inicialização verifica a resposta HTTP de saúde e a ausência de
informações internas na resposta pública. O build gera o JAR executável em `target/`.

## Próximas etapas

Conectar PostgreSQL e Flyway, validar Docker Compose e então desenvolver o CRUD
de responsáveis. Endpoints de negócio e Swagger ainda não estão implementados.
