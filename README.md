# stock_api

此專案主要提供台股歷史資料查詢 API。

## 專案資訊

- 原始 C# 專案：<https://github.com/jiansoft/stock_api>
- 相關資料服務：<https://github.com/jiansoft/stock_crawler>
- UI Demo：<https://jiansoft.mooo.com/stock/revenues>
- 原始 API Demo：<https://jiansoft.freeddns.org/swagger>

## 技術棧

- Java 25
- Spring Boot 4.1.0-M2
- springdoc-openapi 3.0.2
- Spring JDBC
- PostgreSQL
- gRPC
- SLF4J + Log4j2（Async Loggers）

## 執行環境設定

目前資料庫連線預設值放在 `application.yaml`，內容是本機 PostgreSQL：

```text
server=127.0.0.1;port=5432;uid=postgres;pwd=postgres;database=stock;
```

啟動時會先讀取這個預設值，再載入專案根目錄的 `.env`；如果 `.env` 內有 `APP_DATABASE_CONNECTION`，就會覆寫 `app.database.connection`。

`.env` 範例如下：

```dotenv
APP_DATABASE_CONNECTION=server=127.0.0.1;port=5432;uid=Qoo;pwd=Qoo;database=stock;
```

## 啟動方式

執行本機開發環境：

```bash
./gradlew bootRun
```

執行測試：

```bash
./gradlew test
```

產生可執行 jar：

```bash
./gradlew bootJar
```

## Docker

專案已提供 `Dockerfile`，會直接使用已編譯完成的 `jar` 建立映像檔，不會在 Docker 內重新編譯。

在專案目錄內先完成：

```bash
./gradlew bootJar
```

再建立映像檔：

```bash
docker build -t stock_api:latest .
```

若你是把 `stock_api.jar` 單獨上傳到 prod，並和 `Dockerfile` 放在同一層目錄，可改用：

```bash
docker build --build-arg JAR_FILE=stock_api.jar -t stock_api:latest .
```

啟動容器時可直接使用環境變數，或用 `.env` 檔提供設定：

```bash
docker run -d -p 7000:7000 --env-file .env --name stock_api stock_api:latest
```

若要直接用 Compose 啟動，專案也提供 `docker-compose.yml`：

```bash
docker compose up -d --build
```

Compose 會：

- 使用目前的 `Dockerfile`
- 直接打包 `build/libs/stock_api.jar`
- 自動載入 `.env`
- 將容器內 `/app/logs` 對應到主機的 `./logs`

若你要沿用原 C# 專案的操作習慣，也可使用 `control.sh`：

```bash
chmod +x control.sh
./control.sh docker_build
./control.sh docker_start
```

支援指令：

- `./control.sh docker_build`
- `./control.sh docker_stop`
- `./control.sh docker_start`
- `./control.sh docker_restart`

可透過環境變數覆寫：

- `IMAGE_NAME`
- `CONTAINER_NAME`
- `HOST_PORT`
- `LOG_DIR`

## Swagger / OpenAPI

本機啟動後，可使用以下路徑查看 Swagger：

- Swagger UI：<http://localhost:7000/swagger/index.html>
- OpenAPI JSON：<http://localhost:7000/swagger/v1/swagger.json>

## API 清單

1. 股票基本資料 `/api/stock/details`
2. 股票產業分類 `/api/stock/industry`
3. 股利發放記錄 `/api/stock/dividend/{stockSymbol}`
4. 最後收盤資料 `/api/stock/last_daily_quote`
5. 歷史收盤資料 `/api/stock/historical_daily_quote/{date}`
6. 每月營收資料 `/api/stock/revenue_on/{monthOfYear}`
7. 個股營收資料 `/api/stock/revenue_by/{stockSymbol}`
8. 休市日資料 `/api/stock/holiday_schedule/{year}`
9. 台灣加權股價指數 `/api/twse/taiex`

## 免責聲明

本網提供之所有資訊內容均僅供參考，不涉及買賣投資之依據。使用者在進行投資決策時，務必自行審慎評估，
並自負投資風險及盈虧，如依本網提供之資料交易致生損失，本網不負擔任何賠償及法律責任。您自行負責依據
自身投資目標及個人、財務狀況，確定任何投資、證券或任何其他投資產品服務是否適合自身的需要。
本網站所載或本網站上、通過本網站提供的任何服務、內容、資訊及／或資料在任何情況下均不得被解釋為提供投資、
法律意見或提供投資服務。特請造訪此類網頁的人士就有關任何本網資料是否適合其投資需求徵詢適當獨立專業意見。
