# BFF Gateway

Edge API для Web UI: проверка JWT через **IAM introspect**, прокси к `mcp-gateway` и `ai-runtime`.

- Префикс: **`/v1/bff`**
- Порт: **8082**
- Защищённые маршруты: **`/v1/bff/api/**`** (нужен `Authorization: Bearer`)

```bash
./gradlew run
```
