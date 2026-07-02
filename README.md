# Proyecto Los Robles

Sistema de control de accesos y visitas para el condominio "Los Robles". Permite a los residentes generar invitaciones con código QR, a los conserjes registrar el ingreso/salida de visitantes (por QR, manual o vehicular) y declarar objetos que portan, y a los administradores gestionar usuarios, departamentos y reportes.

## Arquitectura

El proyecto está dividido en dos módulos independientes:

| Módulo | Descripción | Stack |
|---|---|---|
| [`api-backend`](api-backend) | API REST | Java 21, Spring Boot 3.3.5, Spring Security + JWT, Spring Data JPA, SQL Server |
| [`app-frontend`](app-frontend) | Aplicación web | React 19, Vite, React Router, Axios |

```
Proyecto_LosRobles/
├── api-backend/      # API REST (Spring Boot)
├── app-frontend/      # SPA (React + Vite)
└── docs/              # Documentación técnica (modelo de datos, diagramas)
```

Documentación adicional en [docs/](docs):
- [Diagrama de clases (modelo de dominio)](docs/diagrama_clases.md)
- [Diseño lógico de la base de datos](docs/diseno_logico_bd.md)
- [Diseño físico de la base de datos (DDL SQL Server)](docs/diseno_fisico_bd.sql)

## Backend (`api-backend`)

### Requisitos
- Java 21 (JDK)
- Maven (o usar el wrapper `mvnw` incluido)
- Base de datos SQL Server (local o Azure SQL)

### Configuración

1. Copia `api-backend/src/main/resources/application.properties.example` a `application.properties`.
2. Define las variables de entorno necesarias:

| Variable | Descripción |
|---|---|
| `DB_URL` | Cadena de conexión JDBC a SQL Server |
| `DB_USERNAME` | Usuario de la base de datos |
| `DB_PASSWORD` | Contraseña de la base de datos |
| `JWT_SECRET` | Clave secreta para firmar tokens JWT (mínimo 32 caracteres) |

> Nota: `spring.jpa.hibernate.ddl-auto=update` crea/actualiza el esquema automáticamente al iniciar. Para un esquema inicial manual, usar [docs/diseno_fisico_bd.sql](docs/diseno_fisico_bd.sql).

### Ejecución

```bash
cd api-backend
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080`. Documentación interactiva (Swagger/OpenAPI) en `http://localhost:8080/swagger-ui.html`.

### Pruebas

```bash
./mvnw test
```

### Estructura de paquetes (`com.losrobles.api`)

| Paquete | Responsabilidad |
|---|---|
| `config` | Seguridad (JWT, filtros), CORS, manejo global de excepciones, OpenAPI |
| `controllers` | Endpoints REST |
| `dto` | Objetos de transferencia de datos (request/response) |
| `models` | Entidades JPA (modelo de dominio) |
| `repositories` | Repositorios Spring Data JPA |
| `services` | Lógica de negocio |
| `util` | Utilidades (fechas, roles) |

### Módulos funcionales (controllers)

| Controller | Función |
|---|---|
| `AuthController` | Login y emisión de tokens JWT |
| `UsuarioController` | CRUD de usuarios (residentes, conserjes, administradores) |
| `RolController` | Gestión de roles |
| `DepartamentoController` | Gestión de departamentos/torres |
| `VisitanteController` | Gestión de visitantes (frecuentes, bloqueados) |
| `InvitacionController` | Creación y validación de invitaciones con QR |
| `RegistroAccesoController` | Registro de ingresos/salidas (QR, manual, vehicular) y objetos declarados |
| `DashboardController` | Estadísticas generales |
| `ReporteController` | Generación de reportes (Excel/PDF vía Apache POI / OpenPDF) |

### Dependencias clave

- **Spring Security + JJWT**: autenticación basada en JWT.
- **Spring Data JPA + mssql-jdbc**: persistencia sobre SQL Server (Azure SQL).
- **springdoc-openapi**: documentación interactiva de la API.
- **Apache POI / OpenPDF**: exportación de reportes a Excel y PDF.

## Frontend (`app-frontend`)

### Requisitos
- Node.js (versión compatible con Vite 8 / React 19)

### Configuración

1. Copia `app-frontend/.env.example` a `.env.local`.
2. Define `VITE_API_URL` apuntando a la URL base de la API (ej. `http://localhost:8080/api`).

### Scripts disponibles

```bash
cd app-frontend
npm install      # Instalar dependencias
npm run dev      # Servidor de desarrollo (Vite)
npm run build    # Build de producción
npm run preview  # Previsualizar build de producción
npm run lint     # Linting con ESLint
```

### Estructura de carpetas (`src`)

| Carpeta | Contenido |
|---|---|
| `api` | Configuración de Axios (interceptores, base URL, manejo de tokens) |
| `components` | Componentes compartidos (layout, rutas protegidas) |
| `features` | Módulos de la aplicación organizados por dominio |
| `features/auth` | Login y autenticación |
| `features/admin` | Panel de administración (usuarios, departamentos, roles) |
| `features/inicio` | Dashboard / página de inicio |
| `features/visitantes` | Registro de visitas, invitaciones, accesos |

### Despliegue

El frontend está configurado para desplegarse en **Vercel** (`app-frontend/vercel.json`); el backend está pensado para **Azure App Service** con **Azure SQL Database** (ver comentarios en `application.properties`).

## Modelo de datos

El dominio gira en torno a 9 entidades principales: `Usuario`, `Rol`, `Departamento`, `Visitante`, `Invitacion`, `RegistroAcceso`, `ObjetoRegistrado`, `EventoSeguridad` y `DispositivoPush`.

Resumen del flujo principal:
1. Un **residente** (`Usuario`) crea una **`Invitacion`** con código QR para un **`Visitante`**, con destino a su **`Departamento`**.
2. El **conserje** valida el QR y genera un **`RegistroAcceso`**, que puede declarar **`ObjetoRegistrado`**(s) que porta el visitante.
3. Si el QR es inválido, expirado, ya usado, o el visitante está bloqueado, se registra un **`EventoSeguridad`**.
4. Los **`DispositivoPush`** permiten enviar notificaciones a los usuarios (ej. avisar al residente del ingreso de su visita).

Ver el detalle completo en [docs/diagrama_clases.md](docs/diagrama_clases.md) y [docs/diseno_logico_bd.md](docs/diseno_logico_bd.md).
