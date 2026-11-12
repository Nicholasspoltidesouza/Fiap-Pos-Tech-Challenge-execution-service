# Execution Service — Tech Challenge FIAP (Fase 4)

Microserviço de **Execução e Produção** da oficina mecânica. Possui banco **PostgreSQL** próprio, gerencia a fila de trabalho no chão de oficina e participa da **saga** orquestrada pelo OS Service via **RabbitMQ**.

## Papel na saga

1. O **OS Service** publica `execucao.iniciar` quando a ordem está pronta para produção.
2. Este serviço cria um job em `EM_FILA`, publica `execucao.iniciada` e avança manualmente (API do chão) ou por etapas: `EM_DIAGNOSTICO` → `EM_REPARO` → `FINALIZADA`.
3. Ao finalizar, publica `execucao.finalizada` para o OS Service continuar o fluxo.
4. Em falha/compensação, escuta `compensar.execucao` e cancela o job (`CANCELADA`), removendo-o da fila.

```text
OS Service  --execucao.iniciar-->  Execution Service
OS Service  <--execucao.iniciada-- Execution Service
OS Service  <--execucao.finalizada-- Execution Service
OS Service  --compensar.execucao--> Execution Service
```

## Stack

- Java 25, Spring Boot 4.0.3, Maven
- Spring Web, Validation, Data JPA, AMQP, Actuator, Prometheus
- springdoc-openapi (Swagger UI)
- JaCoCo (meta 80% no pacote de usecases), JUnit 5, Mockito, Cucumber BDD
- Docker + manifests Kubernetes + GitHub Actions

## Portas e endpoints

| Recurso | Valor |
|---|---|
| HTTP | `8083` |
| Swagger | `http://localhost:8083/swagger-ui.html` |
| Health | `GET /actuator/health` |
| Prometheus | `GET /actuator/prometheus` |

### REST

- `GET /api/execucoes/fila`
- `GET /api/execucoes/{id}`
- `GET /api/execucoes/por-ordem/{ordemServicoId}`
- `POST /api/execucoes/{id}/iniciar-diagnostico`
- `POST /api/execucoes/{id}/iniciar-reparo`
- `POST /api/execucoes/{id}/finalizar`

### RabbitMQ

- Exchange: `oficina.events` (topic)
- Consome: filas `execution.execucao.iniciar`, `execution.compensar.execucao`
- Routing keys de entrada: `execucao.iniciar`, `compensar.execucao`
- Publica: `execucao.iniciada`, `execucao.finalizada`

Envelope de evento:

```json
{
  "eventId": "uuid",
  "eventType": "ExecucaoIniciada",
  "ocorridoEm": "2026-01-01T10:00:00Z",
  "ordemServicoId": "uuid",
  "clienteId": "uuid",
  "payload": {}
}
```

## Domínio

`ExecucaoTrabalho` com status: `EM_FILA`, `EM_DIAGNOSTICO`, `EM_REPARO`, `FINALIZADA`, `CANCELADA`.

Campos: `id`, `ordemServicoId`, `clienteId`, `status`, `posicaoFila`, `diagnosticoNotas`, `reparoNotas`, `createdAt`, `updatedAt`, `finalizadoEm`.

## Como rodar localmente

```bash
cp .env.example .env
# Suba PostgreSQL e RabbitMQ compatíveis com o .env
mvn spring-boot:run
```

Testes:

```bash
mvn verify
```

Docker:

```bash
docker build -t execution-service:latest .
```

Kubernetes:

```bash
kubectl apply -f k8s/
```

Coleção Postman: `postman/execution-service.postman_collection.json`.

## Estrutura

Arquitetura em camadas (`domain`, `application`, `infrastructure`, `presentation`), alinhada aos demais microserviços da Fase 4.
