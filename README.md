# 🎬 Moventia

A social movie review platform built with Spring Boot and React.

---

## Prerequisites

- **Java 21**
- **Maven 3.9+** (or use the included `./mvnw` wrapper)
- **PostgreSQL 15+**
- **Node.js 18+** & npm
- **TMDB API Key** — get one free at [themoviedb.org](https://www.themoviedb.org/settings/api)

---

## 1. Create the Database

```sql
CREATE DATABASE "Moventia";
```

## 2. Configure the Backend

Edit `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/Moventia
spring.datasource.username=postgres
spring.datasource.password=YOUR_DB_PASSWORD

jwt.secret=YOUR_SECRET_KEY_MIN_32_CHARS_LONG
jwt.expiration-ms=86400000

tmdb.api.key=YOUR_TMDB_API_KEY
tmdb.api.base-url=https://api.themoviedb.org/3
tmdb.image.base-url=https://image.tmdb.org/t/p/w500
```

> Tables are auto-created on first run (`ddl-auto=update`).

## 3. Run the Backend

```bash
cd backend
./mvnw spring-boot:run
```

API starts at **http://localhost:8080**

## 4. Run the Frontend

```bash
cd frontend
npm install
npm run dev
```

App opens at **http://localhost:5173**

---

## Build for Production

```bash
# Backend
cd backend
./mvnw clean package -DskipTests
java -jar target/Moventia-0.0.1-SNAPSHOT.jar

# Frontend
cd frontend
npm run build
npm run preview
```
