# Family Finance

Backend-система для учета личных и семейных финансов. Проект позволяет пользователям регистрироваться, входить в систему, создавать семейные финансовые группы, управлять категориями, добавлять доходы и расходы, а также строить отчеты за выбранные периоды.

## Быстрый запуск через Docker

### Требования

- Docker Desktop или Docker Engine с Docker Compose
- Git
- Интернет для первой сборки
- Свободные локальные порты: `5433`, `5434`, `8081`, `8082`, `8083`, `9092`

Docker Compose скачает публичные базовые образы:

- `postgres:16-alpine`
- `apache/kafka:3.9.1`
- `eclipse-temurin:21-jdk-alpine`
- `eclipse-temurin:21-jre-alpine`
### Запуск

```bash
git clone <repository-url>
cd family-finance
cp .env.example .env
docker compose up --build
```

Файл `.env` необязателен, потому что в `docker-compose.yml` уже есть локальные значения по умолчанию. Но с `.env` конфигурация становится явной и ее проще менять.

После запуска сервисы будут доступны по адресам:

- Auth service: `http://localhost:8081`
- Finance service: `http://localhost:8082`
- Report service и UI: `http://localhost:8083`

Проверка состояния сервисов:

- `http://localhost:8081/actuator/health`
- `http://localhost:8082/actuator/health`
- `http://localhost:8083/actuator/health`

Swagger/OpenAPI UI:

- `http://localhost:8081/swagger-ui.html`
- `http://localhost:8082/swagger-ui.html`
- `http://localhost:8083/swagger-ui.html`

### Остановка

```bash
docker compose down
```

Остановить контейнеры и удалить тома с данными баз:

```bash
docker compose down -v
```

## Технологии
- Java 21
- Gradle multi-module project
- Spring Boot 3.5
- Spring WebFlux
- Spring Security
- OAuth2 Resource Server и JWT
- Bean Validation
- Spring Data R2DBC
- PostgreSQL
- Flyway migrations
- Apache Kafka
- Caffeine cache
- Springdoc OpenAPI / Swagger UI
- Docker и Docker Compose
- JUnit 5, Reactor Test, Spring Boot Test
- Testcontainers для интеграционных тестов

## Смысл проекта

Приложение представляет собой backend для управления личными и семейными финансами.

Основные сценарии:

- регистрация и авторизация пользователя;
- авторизация через JWT;
- управление семьей или финансовой группой;
- добавление и удаление участников семьи;
- управление категориями доходов и расходов;
- создание, изменение, удаление и фильтрация операций;
- построение отчетов за выбранные периоды;
- экспорт отчетов в CSV;
- простой UI для отчетов, который отдает `report-service`.

Система разделена на несколько сервисов, чтобы показать микросервисный подход и межсервисное взаимодействие:

- `auth-service` отвечает за пользователей и авторизацию;
- `finance-service` отвечает за основные финансовые данные;
- `report-service` строит отчеты на основе финансовых данных;
- Kafka используется для асинхронных событий об изменении финансовых данных;
- REST используется для синхронного взаимодействия между сервисами.

## Сервисы Docker Compose

`docker-compose.yml` запускает:

- `auth-db`: PostgreSQL-база для `auth-service`;
- `finance-db`: PostgreSQL-база для `finance-service`;
- `kafka`: локальный Kafka broker;
- `auth-service`: API авторизации на порту `8081`;
- `finance-service`: API финансов на порту `8082`;
- `report-service`: API отчетов и UI на порту `8083`.