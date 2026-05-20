# bff-gateway

BFF для Web UI: JWT → IAM introspect → прокси к MCP и AI.

Порт **8082**, префикс `/v1/bff`.

## Защищённые маршруты (`Authorization: Bearer`)

| Метод | BFF | Прокси |
|-------|-----|--------|
| GET | `/api/me` | IAM introspect claims |
| GET | `/api/mcp/catalog` | mcp-gateway |
| GET | `/api/mcp/workspaces/{id}/installations` | mcp-gateway |
| POST | `/api/mcp/workspaces/{id}/installations` | mcp-gateway (+ X-Org-Id из JWT) |
| POST | `/api/chat` | ai-runtime (`workspaceId` из JWT или `X-Workspace-Id`) |

## Переменные

| Переменная | Default |
|------------|---------|
| `IAM_BASE_URL` | http://localhost:8080/v1/iam |
| `MCP_BASE_URL` | http://localhost:8081/v1/mcp |
| `AI_BASE_URL` | http://localhost:8083/v1/ai |
