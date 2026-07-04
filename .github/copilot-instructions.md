# Copilot instructions for Proyecto_LosRobles

Purpose: short, actionable guidance to help Copilot sessions navigate, build, test and make targeted changes in this repository.

---

## Quick commands

Backend (Spring Boot, Maven wrapper)
- Start dev (Unix/macOS):
  - cd api-backend && ./mvnw spring-boot:run
- Start dev (Windows PowerShell / CMD):
  - cd api-backend && .\mvnw.cmd spring-boot:run
- Run full test suite:
  - ./mvnw test  (or .\mvnw.cmd test on Windows)
- Run a single test class or method:
  - ./mvnw -Dtest=InvitacionServiceTest test
  - ./mvnw -Dtest=InvitacionServiceTest#miMetodo test
  - Multiple classes: -Dtest=ClassA,ClassB
  (Use the wrapper to avoid relying on a system Maven)
- Build package (skip tests):
  - ./mvnw -DskipTests package

Frontend (React + Vite, Node)
- Install deps: cd app-frontend && npm install
- Dev server: npm run dev
- Dev server (container/network): npm run dev -- --host 0.0.0.0
- Build: npm run build
- Lint: npm run lint
- Lint a single file: npm run lint -- src/path/to/File.jsx

Develop with Docker Compose (dev):
- docker compose up --build
- To run only backend: docker compose up --build backend

Environment templates
- Backend: api-backend/.env.example -> copy to api-backend/.env and set DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET
- Frontend: app-frontend/.env.example -> copy to app-frontend/.env.local and set VITE_API_URL

---

## High-level architecture (overview)
- Two independent modules:
  - api-backend: Java 21 + Spring Boot 3.x, REST API, Spring Security (JWT), Spring Data JPA, SQL Server
  - app-frontend: React 19 + Vite SPA, Axios for API calls, deployed to Vercel (vercel.json)
- Local dev: docker-compose starts both services; backend listens on :8080, frontend on :5173
- API docs: backend exposes OpenAPI/Swagger at `/swagger-ui.html` when running
- Data model: docs/diagrama_clases.md and docs/diseno_logico_bd.md provide entity and relational diagrams

Primary flow: residente creates Invitacion (QR) -> conserje valida QR -> RegistroAcceso created; blocked/invalid actions generate EventoSeguridad. Push devices handled via DispositivoPush.

---

## Key conventions and locate-these-files
- Java package layout: com.losrobles.api.{config,controllers,dto,models,repositories,services,util}
  - Security & JWT: under `config` package; look there for filters and JWT utils
- DTOs live in `dto` and are used by controllers for request/response shapes
- Entities are JPA `models` used by Spring Data repositories; tests are in `src/test/java`
- DB: SQL Server usage; application.properties example in `api-backend/src/main/resources/application.properties.example`. Note `spring.jpa.hibernate.ddl-auto=update` is used for local schema evolution.
- Reports use Apache POI / OpenPDF (see ReporteController)
- Frontend structure: `src/features` groups domain modules (auth, admin, visitantes, inicio). Axios setup and interceptors are in `src/api` — useful first place when tracing auth/token flows.
- Deployment config: frontend `vercel.json`; backend intended for Azure App Service + Azure SQL (see README comments)

Naming patterns to expect:
- Controller names end with `Controller` (REST endpoints)
- Service classes end with `Service` and are the place for business rules
- Repository interfaces extend Spring Data and live in `repositories`

---

## Tests & focused runs
- Maven single-test pattern (useful for quick loop): -Dtest=ClassName#method
- If running tests in CI or automation, use the Maven wrapper (mvnw / mvnw.cmd) to ensure consistent Maven version
- Frontend has no unit test runner configured; focus on lint and manual QA or add test tooling if needed

---

## Existing AI assistant / tooling hints
- Claude config: `.claude/settings.local.json` contains permitted bash calls and example test commands (helps automated runs). Useful to mirror allowed test invocations when running CI-like commands inside assistant sessions.
- No other agent rules files found (e.g., AGENTS.md, .windsurfrules). If you add assistant configs, reference them here.

---

## Where to start for common tasks
- Fixing an API bug: start at `api-backend/src/main/java/com/losrobles/api/controllers` to find the surface endpoint, then check corresponding `services` and `repositories`.
- Changing domain model: inspect `docs/diagrama_clases.md` then `api-backend/src/main/java/.../models` and migration DDL under `api-backend/sql` or docs/diseno_fisico_bd.sql
- Frontend UI work: `app-frontend/src/features/<domain>`; token/auth issues: `app-frontend/src/api` (Axios interceptors)

---

If this file already exists, integrate missing commands above and keep these sections concise.

Created by Copilot guidance generator.
