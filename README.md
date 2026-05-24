# bff-gateway

BFF для Web UI: JWT → IAM introspect → прокси к доменным сервисам.

Порт **8082**, префикс `/v1/bff`. Браузер ходит только на `/v1/bff/api/*`.

## Маршруты Web UI

Полный список в коде: `BffUiRoutes.java`. Тест: `BffUiRoutesCoverageTest` (все пути зарегистрированы в gateway).

| Метод | BFF | Прокси |
|-------|-----|--------|
| GET, PATCH | `/api/me` | IAM `/me` + claims из JWT |
| POST | `/api/auth/login`, `register`, `logout`, `switch-org` | IAM `/auth/*` |
| POST, GET, PATCH | `/api/organizations`, `/{orgId}`, groups, invites | IAM |
| POST | `/api/invites/accept` | IAM |
| GET, POST | `/api/config/orgs/{orgId}/workspaces` | config-service |
| PATCH | `/api/config/workspaces/{workspaceId}` | config-service |
| POST | `/api/chat` | ai-runtime |
| GET, POST, PATCH, DELETE | `/api/conversations`, `/{id}/messages` | ai-runtime |
| GET, POST, GET, PATCH, DELETE, POST health | `/api/mcp/catalog`, `.../installations` | mcp-gateway |
| POST | `/api/policy/pending-approvals/{id}/approve`, `reject` | policy-service |

Публичные (без JWT): только `/api/auth/*`.

## Локальная пересборка backend для UI

После изменений в Java-сервисах **обязательно** пересоберите образы — иначе BFF отдаёт 404/405 от **устаревших** iam/mcp/ai:

```powershell
cd platform
.\scripts\rebuild-web-backend.ps1
```

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
