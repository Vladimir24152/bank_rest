# Система управления банковскими картами

## Описание
REST API для управления банковскими картами, переводами средств и пользователями с системой безопасности на основе JWT.

## Быстрый старт

### Требования
- Java 17+
- Docker & Docker Compose
- Maven 3.9+

## Клонирование репозитория
git clone <repository-url>
cd bankcards

## Запуск всех сервисов
docker-compose up --build

## Приложение будет доступно по адресу:
### http://localhost:8080

## OpenAPI спецификация
### Полная документация API доступна в формате OpenAPI:

docs/openapi.yaml - YAML спецификация

http://localhost:8080/swagger-ui.html - Интерактивная документация

http://localhost:8080/v3/api-docs - JSON спецификация


# Основные эндпоинты
## Аутентификация
POST /api/v1/auth/sign-up - Регистрация пользователя

POST /api/v1/auth/sign-in - Авторизация

POST /api/v1/auth/give-admin/{userId} - Назначение роли ADMIN (только для админов)

## Карты
POST /api/v1/card/create - Создание карты (ADMIN)

GET /api/v1/card/{cardId} - Получение карты

GET /api/v1/card/get-all - Все карты (ADMIN)

POST /api/v1/card/update/{cardId} - Обновление карты (ADMIN)

POST /api/v1/card/blocked/{cardId} - Запрос на блокировку

## Переводы и баланс
GET /api/v1/card/balance/{cardId} - Получение баланса

POST /api/v1/card/transfer - Перевод между картами

# Безопасность
## Роли
ROLE_ADMIN: Полный доступ ко всем операциям

ROLE_USER: Доступ только к своим картам и операциям

## Аутентификация
Зарегистрируйтесь или авторизуйтесь для получения JWT токена

Добавьте токен в заголовок запроса:

Authorization: Bearer {your-jwt-token}

# База данных
## Миграции
### Миграции выполняются автоматически через Liquibase:

Конфигурация: src/main/resources/db/migration/db.changelog-master.yml

Схема создается автоматически при запуске

## Основные таблицы
### users - Пользователи и роли

### cards - Банковские карты

### transfers - История переводов

# Docker
## Сервисы
### app: Spring Boot приложение (порт 8080)

### card_db: PostgreSQL база данных (порт 5432)

Команды Docker
bash
### Запуск
docker-compose up --build

### Остановка
docker-compose down

### Просмотр логов
docker-compose logs -f app

# ️ Разработка
## Структура проекта

#### src/main/java/com/example/bankcards/
#### ├── config/          # Конфигурация Spring
#### ├── controller/      # REST контроллеры
#### ├── dto/            # Data Transfer Objects
#### ├── entity/         # Сущности JPA
#### ├── repository/     # Репозитории Spring Data
#### ├── service/        # Бизнес-логика
#### └── security/       # Безопасность и JWT

## Конфигурация
### Основные настройки в application.yml:

Порт сервера: 8080

База данных: PostgreSQL

Шифрование карт: AES

JWT: HS512 алгоритм
