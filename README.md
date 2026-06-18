# MiniMarket Plus

Backend API REST para la gestión de un minimarket, desarrollada con Spring Boot 3 e integración completa de seguridad mediante Spring Security + JWT.

---

## Tecnologías

| Tecnología | Versión | Descripción |
|---|---|---|
| Java | 17 | Lenguaje principal |
| Spring Boot | 3.4.1 | Framework base |
| Spring Security | 6.x | Autenticación y autorización |
| jjwt | 0.11.5 | Generación y validación de JWT |
| H2 Database | Runtime | Base de datos en memoria |
| Lombok | Latest | Reducción de boilerplate |
| Maven | 3.x | Gestión de dependencias |

---

## Estructura del Proyecto

```
├── 📁 .mvn
│   └── 📁 wrapper
│       └── 📄 maven-wrapper.properties
├── 📁 postman                          # Colección de pruebas 
│   └── ⚙️ MiniMarket_S3.postman_collection.json
├── 📁 src
│   ├── 📁 main
│   │   ├── 📁 java
│   │   │   └── 📁 com
│   │   │       └── 📁 minimarket
│   │   │           ├── 📁 controller   # Controladores REST
│   │   │           ├── 📁 entity       # Entidades JPA
│   │   │           ├── 📁 repository   # Interfaces Spring Data JPA
│   │   │           ├── 📁 security     # Capa de seguridad
│   │   │           │   ├── 📁 config     # Configuración de seguridad (JWT stateless)
│   │   │           │   ├── 📁 filter     # Filtro JWT por request
│   │   │           │   ├── 📁 model      # CustomUserDetails, LoginRequest
│   │   │           │   ├── 📁 service    # CustomUserDetailsService
│   │   │           │   └── 📁 util       # JwtUtil.java
│   │   │           └── 📁 service      # Interfaces de servicios
│   │   │               └── 📁 impl       # Implementaciones
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

## Requisitos Previos

- Java 17 o superior
- Maven 3.6 o superior
- IDE recomendado: IntelliJ IDEA o VS Code con Extension Pack for Java
- Postman o similar para pruebas

---

## Ejecución

```bash
# Clonar el repositorio
git clone https://github.com/<usuario>/minimarket-plus.git
cd minimarket-plus

# Compilar
mvn clean install

# Ejecutar
mvn spring-boot:run
```

- API disponible en: `http://localhost:8080`
- Consola H2 (solo ADMIN): `http://localhost:8080/h2-console`

---

## Usuarios de Prueba

Cargados automáticamente por `data.sql` al iniciar la aplicación:

| Usuario | Contraseña | Rol | Acceso |
|---|---|---|---|
| `admin` | `admin123` | ROLE_ADMIN | Acceso total |
| `empleado` | `empleado123` | ROLE_EMPLEADO | Inventario, ventas, detalle ventas |
| `cliente` | `cliente123` | ROLE_CLIENTE | Carrito y consulta de productos |

---

## Endpoints

### Autenticación (públicos)

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/auth/login` | Retorna JWT |
| POST | `/auth/registro` | Registra usuario con ROLE_CLIENTE |

### Productos y Categorías

| Método | Endpoint | Rol requerido |
|---|---|---|
| GET | `/api/productos` | Público |
| GET | `/api/productos/{id}` | Público |
| POST | `/api/productos` | ADMIN |
| PUT | `/api/productos/{id}` | ADMIN |
| DELETE | `/api/productos/{id}` | ADMIN |
| GET | `/api/categorias` | Público |
| GET | `/api/categorias/{id}` | Público |
| POST | `/api/categorias` | ADMIN |
| PUT | `/api/categorias/{id}` | ADMIN |
| DELETE | `/api/categorias/{id}` | ADMIN |

### Inventario, Ventas y Detalle Ventas

| Método | Endpoint | Rol requerido |
|---|---|---|
| GET / POST / PUT | `/api/inventario/**` | ADMIN, EMPLEADO |
| DELETE | `/api/inventario/{id}` | ADMIN |
| GET / POST | `/api/ventas/**` | ADMIN, EMPLEADO |
| GET / POST / PUT | `/api/detalle-ventas/**` | ADMIN, EMPLEADO |
| DELETE | `/api/detalle-ventas/{id}` | ADMIN |

### Carrito

| Método | Endpoint | Rol requerido |
|---|---|---|
| GET / POST / PUT / DELETE | `/api/carrito/**` | ADMIN, CLIENTE |

### Usuarios

| Método | Endpoint | Rol requerido |
|---|---|---|
| GET / POST / PUT / DELETE | `/api/usuarios/**` | ADMIN |

---

## Autenticación

### 1. Obtener el token JWT

```http
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

Respuesta:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 2. Usar el token en requests protegidos

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 3. CSRF Token

Con CSRF habilitado, las operaciones POST / PUT / DELETE requieren además el header `X-XSRF-TOKEN`:

1. Realizar cualquier GET — la respuesta incluirá la cookie `XSRF-TOKEN`
2. Copiar el valor de esa cookie
3. Incluirlo en el header `X-XSRF-TOKEN` de la siguiente request

---

## Seguridad Implementada

| Mecanismo | Implementación | Protege contra |
|---|---|---|
| JWT Stateless | jjwt 0.11.5 + HS256 | Acceso no autorizado, session hijacking |
| BCrypt | BCryptPasswordEncoder | Robo de contraseñas en BD |
| CSRF Token | CookieCsrfTokenRepository | Cross-Site Request Forgery |
| X-Content-Type-Options | Header HTTP | MIME sniffing / XSS |
| Content-Security-Policy | Header HTTP | XSS, inyección de contenido |
| HSTS | Header HTTP | Downgrade attacks |
| @PreAuthorize | Spring Method Security | Acceso no autorizado por rol |
| Session STATELESS | SessionCreationPolicy | Session fixation attacks |

---

## Payload del JWT

El token incluye los roles del usuario en el claim `roles`:

```json
{
  "sub": "admin",
  "roles": ["ROLE_ADMIN"],
  "iat": 1748000000,
  "exp": 1748036000
}
```

---

## Datos Iniciales

El archivo `data.sql` inserta automáticamente al arrancar:

- 3 roles: `ROLE_ADMIN`, `ROLE_EMPLEADO`, `ROLE_CLIENTE`
- 3 usuarios con contraseñas hasheadas en BCrypt
- Asignación de un rol por usuario
