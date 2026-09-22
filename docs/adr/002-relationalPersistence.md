# ADR 002: persistência relacional e vínculo entre projetos e responsáveis

Status: aceito.

## Contexto

Projetos precisam de um ou mais responsáveis, e cada responsável pode participar de vários projetos. O desafio exige unicidade de e-mail, integridade das datas e execução reproduzível em PostgreSQL.

## Decisão

Usar PostgreSQL com migrations Flyway e validação do schema pelo Hibernate. Modelar o vínculo N:N unidirecional por `projectResponsibles`, cuja chave primária é o par de IDs. Excluir projeto remove vínculos; excluir responsável vinculado é restringido e retorna HTTP 409.

O e-mail é normalizado para minúsculas e protegido por constraint única. Ordem das datas é validada no domínio e reforçada no banco. Projetos usam versão otimista para detectar escritas concorrentes.

## Consequências

A tabela de vínculo é adequada enquanto a participação não tem atributos. Se função, alocação ou período pertencerem ao vínculo, ele deverá virar entidade. Flyway mantém a evolução explícita; geração automática e alterações manuais de schema ficam fora do processo.
