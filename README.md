# ifs25020-p5-pbo2026-springboot-cashflow

Aplikasi Cashflow (catatan keuangan pribadi): Spring Boot 4.1.2, Vaadin 25.3.4, JDBC PostgreSQL, Clean Architecture.

## Menjalankan

1. Pasang JDK 25 dan PostgreSQL.
2. Buat database sebelum run:
   ```sql
   CREATE DATABASE pbo2026_cashflow;
   ```
3. Salin `.env.example` menjadi `.env`, lalu sesuaikan `DB_*` (user/password PostgreSQL Anda).
4. Jalankan:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
5. Buka http://localhost:8080 , login admin default: `admin` / `admin` (dari `ROOT_USERNAME` / `ROOT_PASSWORD`).

Tes + Jacoco: `.\mvnw.cmd clean verify`

## Struktur

```
org.delcom.ifs25020springbootcashflow
├── domain/       entity, exception, repository (port), PasswordHasher   (tanpa Spring/Vaadin)
├── usecase/      AuthUseCase, FinanceUseCase, UserUseCase                (tanpa UI)
├── adapter/      presenter, repository (InMemory + Jdbc + SchemaInitializer), storage
├── framework/    auth (AuthSession), view (Vaadin)
├── config/       AppConfig (composition root + /uploads/**)
├── DotEnv.java
└── App.java
```

## Rute

| Rute | Akses |
|---|---|
| `/login`, `/register` | publik (login → redirect ke `/`) |
| `/`, `/profile` | USER & ADMIN |
| `/users`, `/user-transactions` | ADMIN saja (USER dialihkan ke `/`) |
| `/uploads/**` | publik (foto profil + bukti transaksi) |
