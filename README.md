# SEAL Hackathon — Backend

Spring Boot 3.3 + JPA + Spring Security + JWT.

## Yêu cầu

- Java 17+
- Maven 3.9+ (hoặc dùng IDE để chạy)
- PostgreSQL 17 (chạy qua `docker compose up -d postgres` ở root repo)

## Cấu trúc package

```
vn.edu.fpt.seal
├── SealHackathonApplication.java
├── config/                       # AppProperties, etc.
├── security/                     # JwtService, JwtAuthenticationFilter, SecurityConfig
├── common/
│   ├── entity/BaseEntity.java
│   ├── enums/                    # tất cả enum khớp PostgreSQL
│   └── exception/                # ApiException + GlobalExceptionHandler
└── modules/
    ├── auth/                     # register, login, refresh, /me
    ├── user/                     # User, Role, repos
    ├── university/               # University, Campus
    ├── event/                    # (Phase 2)
    ├── track/                    # (Phase 2)
    ├── round/                    # (Phase 2)
    ├── team/                     # (Phase 2)
    ├── submission/               # (Phase 2)
    ├── scoring/                  # (Phase 2)
    └── ranking/                  # (Phase 2)
```

## Chạy

```bash
# Từ root repo, khởi động Postgres
docker compose up -d postgres

# Vào backend
cd backend

# Cài Maven nếu chưa có (macOS):
#   brew install maven

mvn spring-boot:run
```

API: http://localhost:8080/api  
Swagger: http://localhost:8080/api/swagger-ui.html

## Test endpoint nhanh

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"triet@fpt.edu.vn","password":"Password123!","fullName":"Dinh Minh Triet","studentType":"fpt"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"triet@fpt.edu.vn","password":"Password123!"}'
```

## Lưu ý JPA Enum + Postgres

Schema dùng Postgres ENUM types (`account_status`, `student_type`, ...). Để JPA map đúng, các entity dùng `hypersistence-utils` với `@Type(PostgreSQLEnumType.class)`. Tên Java enum constants phải khớp **chính xác** (case-sensitive) với enum value trong Postgres.

## ddl-auto = validate

Mình cố tình không cho Hibernate tự sửa schema. Schema được quản lý qua `database/schema.sql`. Khi đổi schema:

1. Sửa `database/schema.sql`
2. `docker compose down -v && docker compose up -d postgres` để re-init
3. Hoặc dùng migration tool (Flyway/Liquibase) — sẽ add ở Phase 2 nếu cần
