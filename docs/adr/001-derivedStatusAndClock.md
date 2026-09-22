# ADR 001: estado derivado de datas e referência temporal única

Status: aceito no planejamento.

## Problema

Status persistido fica desatualizado à meia-noite sem edição do projeto.
Permitir setter direto também possibilita divergência entre status e datas.

## Decisão

Calcular o status em ProjectStatusPolicy. Precedência: CONCLUIDO, ATRASADO,
EM_ANDAMENTO, A_INICIAR. Capturar LocalDate.now(clock) uma vez por operação e
passar essa data ao domínio, inclusive para métricas e filtros de consulta.

O Clock usa America/Fortaleza por padrão, configurável por app.timeZone.
A auditoria usa Instant com precisão de microssegundos, compatível com PostgreSQL.

Projeto iniciado com vencimento hoje continua em andamento. Sem término previsto,
também permanece em andamento quando não há atraso. Essas decisões cobrem lacunas
do enunciado. Datas previstas nulas e conclusão direta sem início são permitidas.

Percentual tem duas casas, arredondamento HALF_UP e limite 0..100. Conclusão,
duração não positiva e ausência de datas retornam zero. Dias de atraso são dias
corridos desde o término previsto, não atraso de início.

## Alternativas e consequências

Persistir status exigiria sincronização periódica e tratamento de leituras
desatualizadas. O cálculo na leitura evita esse mecanismo, ao custo de expressar
os critérios de status também na seleção SQL para paginar corretamente. Essa
representação de consulta deve ser comparada com a política de domínio por testes;
não será uma fonte independente de regras nem um filtro em memória após paginação.

Não registrar status/métricas como colunas e não usar setters para status.
Métricas de projetos concluídos não representam atraso histórico: retornam zero,
conforme solicitado.
