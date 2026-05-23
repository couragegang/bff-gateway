# bff-gateway

BFF для Web UI: JWT → IAM introspect → прокси к доменным сервисам.

Порт **8082**, префикс `/v1/bff`.

## Защищённые маршруты (`Authorization: Bearer`)

| Метод | BFF | Прокси |
|-------|-----|--------|
| GET | `/api/me` | IAM introspect claims |
| GET/POST | `/api/mcp/catalog`, `/api/mcp/workspaces/{id}/installations` | mcp-gateway |
| POST | `/api/chat` | ai-runtime |
| GET/POST | `/api/config/orgs/{orgId}/workspaces` | config-service |
| GET/PATCH | `/api/config/workspaces/{workspaceId}` | config-service |
| GET | `/api/policy/...` | policy-service |
| GET | `/api/audit/...` | audit-service |
| GET/POST | `/api/knowledge/...` | knowledge-service |

## Переменные

| Переменная | Default |
|------------|---------|
| `IAM_BASE_URL` | http://localhost:8080/v1/iam |
| `MCP_BASE_URL` | http://localhost:8081 |
| `AI_BASE_URL` | http://localhost:8083 |
| `CONFIG_BASE_URL` | http://localhost:8084/v1/config |
| `POLICY_BASE_URL` | http://localhost:8085/v1/policy |
| `AUDIT_BASE_URL` | http://localhost:8086/v1/audit |
| `KNOWLEDGE_BASE_URL` | http://localhost:8088/v1/knowledge |
