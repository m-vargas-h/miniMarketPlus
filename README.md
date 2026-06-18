# Minimarket API — Sprint 2

Sistema de gestión para un minimarket desarrollado con **Spring Boot 3**. Esta versión evoluciona el Sprint 1 reemplazando la autenticación por formulario con un esquema de **autenticación JWT stateless**, agregando un endpoint de registro público y habilitando **Spring Boot Actuator** para monitoreo.

---

## Novedades respecto al Sprint 1

| Aspecto | Sprint 1 | Sprint 2 |
|---|---|---|
| Autenticación | Formulario de login (sesión) | JWT Bearer Token (stateless) |
| Registro de usuarios | Solo vía `/api/usuarios` (admin) | Endpoint público `/auth/registro` |
| Login | `POST /login` de Spring Security | `POST /auth/login` → devuelve JWT |
| Sesión de servidor | Sí (`HttpSession`) | No (`STATELESS`) |
| JWT integrado | Parcial (solo `JwtUtil` vacío) | Completo (`JwtUtil` + `JwtAuthFilter`) |
| Actuator | No | Sí (`spring-boot-starter-actuator`) |
| Productos y categorías | Requieren autenticación | Acceso **público** (sin token) |

---

## Tecnologías

| Tecnología | Versión |
|---|---|
| Java | 17 |
| Spring Boot | 3.4.1 |

---

## Estructura del proyecto

```
├── 📁 .mvn
│   └── 📁 wrapper
│       └── 📄 maven-wrapper.properties
├── 📁 src
│   ├── 📁 main
│   │   ├── 📁 java
│   │   │   └── 📁 com
│   │   │       └── 📁 minimarket
│   │   │           ├── 📁 controller   # Controladores REST
│   │   │           ├── 📁 entity       # Entidades JPA
│   │   │           ├── 📁 repository   # Repositorios Spring Data
│   │   │           ├── 📁 security     # Capa de seguridad
│   │   │           │   ├── 📁 config     # Configuración de seguridad (JWT stateless)
│   │   │           │   ├── 📁 filter     # Filtro JWT por request
│   │   │           │   ├── 📁 model      # CustomUserDetails, LoginRequest
│   │   │           │   ├── 📁 service    # CustomUserDetailsService
│   │   │           │   └── 📁 util       # JwtUtil
│   │   │           ├── 📁 service      # Interfaces de servicio
│   │   │           │   └── 📁 impl       # Implementaciones
│   │   │           └── ☕ MinimarketApplication.java
│   │   └── 📁 resources
│   │       ├── 📁 static
│   │       ├── 📁 templates
│   │       ├── 📄 application.properties
│   │       └── 📄 data.sql
│   └── 📁 test
│       └── 📁 java
│           └── 📁 com
│               └── 📁 minimarket
│                   └── ☕ MinimarketApplicationTests.java
├── ⚙️ .gitattributes
├── ⚙️ .gitignore
├── 📝 README.md
├── 📄 mvnw
├── 📄 mvnw.cmd
└── ⚙️ pom.xml
```

---

## Cómo funciona la autenticación JWT

```
Cliente                        API
  │                             │
  │  POST /auth/login           │
  │  { username, password }     │
  │ ─────────────────────────►  │  Valida credenciales
  │                             │  Genera token JWT (10 horas)
  │  { "token": "eyJ..." }      │
  │ ◄─────────────────────────  │
  │                             │
  │  GET /api/carrito           │
  │  Authorization: Bearer eyJ  │
  │ ─────────────────────────►  │  JwtAuthFilter intercepta
  │                             │  Valida token → autentica usuario
  │  200 OK                     │
  │ ◄─────────────────────────  │
```

El token JWT se genera con HMAC-SHA256, tiene una vigencia de **10 horas** y debe enviarse en cada request protegido mediante el header:

```
Authorization: Bearer <token>
```

---

## Seguridad y roles

La sesión de servidor fue eliminada (`STATELESS`). El acceso a cada recurso se controla únicamente por el rol contenido en el token JWT.

| Endpoint | Acceso |
|---|---|
| `POST /auth/login` | Público (sin token) |
| `POST /auth/registro` | Público (sin token) |
| `GET /public/**` | Público (sin token) |
| `GET/POST/PUT/DELETE /api/productos/**` | Público (sin token) |
| `GET/POST/PUT/DELETE /api/categorias/**` | Público (sin token) |
| `/api/usuarios/**` | Solo `ROLE_ADMIN` |
| `/api/inventario/**` | `ROLE_ADMIN` o `ROLE_EMPLEADO` |
| `/api/ventas/**` | `ROLE_ADMIN` o `ROLE_EMPLEADO` |
| `/api/detalle-ventas/**` | `ROLE_ADMIN` o `ROLE_EMPLEADO` |
| `/api/carrito/**` | `ROLE_ADMIN` o `ROLE_CLIENTE` |
| `/h2-console/**` | Solo `ROLE_ADMIN` |

---

## Usuarios de prueba

Cargados automáticamente al iniciar la aplicación (`data.sql`):

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | ROLE_ADMIN |
| `empleado` | `empleado123` | ROLE_EMPLEADO |
| `cliente` | `cliente123` | ROLE_CLIENTE |

---

## Cómo ejecutar

### Requisitos previos

- Java 17+

### Pasos

```bash
cd Minimarket_S2

./mvnw spring-boot:run        # Linux / macOS
mvnw.cmd spring-boot:run      # Windows
```

La aplicación quedará disponible en `http://localhost:8080`.

---

## Endpoints de la API

### Autenticación — `/auth` (público)

#### `POST /auth/login`
Valida credenciales y devuelve el token JWT.

**Request:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response `200 OK`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

#### `POST /auth/registro`
Registra un nuevo usuario con rol `ROLE_CLIENTE` por defecto.

**Request:**
```json
{
  "username": "nuevo_cliente",
  "password": "mi_password"
}
```

**Response `201 Created`:**
```
"Usuario registrado exitosamente."
```

**Response `409 Conflict`** si el username ya existe.

---

### Productos — `/api/productos` *(público)*

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/productos` | Listar todos los productos |
| GET | `/api/productos/{id}` | Obtener producto por ID |
| POST | `/api/productos` | Crear producto |
| PUT | `/api/productos/{id}` | Actualizar producto |
| DELETE | `/api/productos/{id}` | Eliminar producto |

### Categorías — `/api/categorias` *(público)*

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/categorias` | Listar categorías |
| GET | `/api/categorias/{id}` | Obtener categoría por ID |
| POST | `/api/categorias` | Crear categoría |
| PUT | `/api/categorias/{id}` | Actualizar categoría |
| DELETE | `/api/categorias/{id}` | Eliminar categoría |

### Inventario — `/api/inventario` *(Admin / Empleado)*

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/inventario` | Listar movimientos |
| GET | `/api/inventario/{id}` | Obtener movimiento por ID |
| POST | `/api/inventario` | Registrar movimiento |
| PUT | `/api/inventario/{id}` | Actualizar movimiento |
| DELETE | `/api/inventario/{id}` | Eliminar movimiento |

### Ventas — `/api/ventas` *(Admin / Empleado)*

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/ventas` | Listar ventas |
| GET | `/api/ventas/{id}` | Obtener venta por ID |
| POST | `/api/ventas` | Registrar venta |

### Detalle de Ventas — `/api/detalle-ventas` *(Admin / Empleado)*

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/detalle-ventas` | Listar detalles |
| GET | `/api/detalle-ventas/{id}` | Obtener detalle por ID |
| POST | `/api/detalle-ventas` | Crear detalle |
| PUT | `/api/detalle-ventas/{id}` | Actualizar detalle |
| DELETE | `/api/detalle-ventas/{id}` | Eliminar detalle |

### Carrito — `/api/carrito` *(Admin / Cliente)*

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/carrito` | Listar ítems del carrito |
| GET | `/api/carrito/{id}` | Obtener ítem por ID |
| POST | `/api/carrito` | Agregar producto al carrito |
| PUT | `/api/carrito/{id}` | Actualizar ítem |
| DELETE | `/api/carrito/{id}` | Eliminar ítem |

### Usuarios — `/api/usuarios` *(Solo Admin)*

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/usuarios` | Listar usuarios |
| GET | `/api/usuarios/{id}` | Obtener usuario por ID |
| POST | `/api/usuarios` | Crear usuario (contraseña se encripta automáticamente) |
| PUT | `/api/usuarios/{id}` | Actualizar usuario |
| DELETE | `/api/usuarios/{id}` | Eliminar usuario |

### Público

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/public/hola` | Endpoint de prueba sin autenticación |

### Actuator — `/actuator`

| Ruta | Descripción |
|---|---|
| `/actuator/health` | Estado de la aplicación |
| `/actuator/info` | Información de la app |

---

## Ejemplo de flujo completo con Postman / curl

```bash
# 1. Obtener token
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"empleado","password":"empleado123"}'

# 2. Usar el token en requests protegidos
curl http://localhost:8080/api/inventario \
  -H "Authorization: Bearer <token_obtenido>"
```

---

## Notas de desarrollo

- La base de datos es **en memoria**: los datos se pierden al reiniciar.
- La clave secreta JWT está hardcodeada en `JwtUtil.java`. Para producción se recomienda externalizarla en `application.properties` o variables de entorno.
- El token no incluye los roles en su payload; la autorización se resuelve recargando el usuario desde la base de datos en cada request a través de `CustomUserDetailsService`.
- `@EnableMethodSecurity` está activo en `SecurityConfig`, lo que permite usar `@PreAuthorize` a nivel de método si se requiere mayor granularidad en el futuro.
