# MiniMarket Plus

Backend API REST para la gestión de un minimarket, desarrollada con Spring Boot 3 e integración completa de seguridad mediante Spring Security + JWT. Incluye suite de pruebas unitarias e integración con cobertura medida por JaCoCo.

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
| JUnit 5 | Incluido en starter-test | Framework de pruebas unitarias |
| Mockito | Incluido en starter-test | Simulación de dependencias |
| JaCoCo | 0.8.11 | Medición de cobertura de código |
| Maven | 3.x | Gestión de dependencias |

---

## Estructura del Proyecto

```
├── 📁 .mvn
│   └── 📁 wrapper
│       └── 📄 maven-wrapper.properties
├── 📁 src
│   ├── 📁 main
│   │   ├── 📁 java
│   │   │   └── 📁 com
│   │   │       └── 📁 minimarket
│   │   │           ├── 📁 controller                   # Controladores REST
│   │   │           ├── 📁 entity                       # Entidades JPA
│   │   │           ├── 📁 repository                   # Interfaces Spring Data JPA
│   │   │           ├── 📁 security                     # Capa de seguridad
│   │   │           │   ├── 📁 config                     # Configuración de seguridad (JWT stateless)
│   │   │           │   ├── 📁 filter                     # Filtro JWT por request
│   │   │           │   ├── 📁 model                      # CustomUserDetails, LoginRequest
│   │   │           │   ├── 📁 service                    # CustomUserDetailsService
│   │   │           │   └── 📁 util                       # JwtUtil.java
│   │   │           ├── 📁 service                      # Interfaces de servicios
│   │   │           │   └── 📁 impl                       # Implementaciones
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
│                   ├── 📁 entity
│                   │   └── ☕ EntityTest.java              # Pruebas de entidades
│                   ├── 📁 service
│                   │   └── 📁 impl
│                   │       ├── ☕ UsuarioServiceImplTest.java
│                   │       └── ☕ VentaServiceImplTest.java
│                   ├── ☕ MinimarketApplicationTests.java
│                   └── ☕ MinimarketIntegrationTest.java   # Pruebas de integración
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
- Postman o similar para pruebas de endpoints

---

## Ejecución

```bash
# Compilar
./mvnw clean install

# Ejecutar
./mvnw spring-boot:run
```

- API disponible en: `http://localhost:8080`
- Consola H2 (solo ADMIN): `http://localhost:8080/h2-console`

---

## Pruebas

### Ejecutar todas las pruebas

```bash
./mvnw test
```

Ejecuta las 37 pruebas distribuidas en 4 clases y genera automáticamente el reporte de cobertura JaCoCo.

### Ejecutar una clase específica

```bash
./mvnw test -Dtest=EntityTest
./mvnw test -Dtest=UsuarioServiceImplTest
./mvnw test -Dtest=VentaServiceImplTest
./mvnw test -Dtest=MinimarketIntegrationTest
```

### Reporte de cobertura JaCoCo

Disponible en `target/site/jacoco/index.html` tras ejecutar `./mvnw test`.

| Paquete | Cobertura |
|---|---|
| `com.minimarket.entity` | 97% |
| `com.minimarket.security.config` | 100% |
| `com.minimarket.security.model` | 100% |
| `com.minimarket.security.service` | 73% |
| `com.minimarket.security.filter` | 36% |
| `com.minimarket.service.impl` | 35% |
| **Total** | **52%** |

### Resumen de pruebas

| Clase | Tipo | Pruebas | Resultado |
|---|---|---|---|
| `UsuarioServiceImplTest` | Unitaria (Mockito) | 8 | ✅ Todas pasan |
| `VentaServiceImplTest` | Unitaria (Mockito) | 10 | ✅ Todas pasan |
| `EntityTest` | Unitaria (dominio) | 8 | ✅ Todas pasan |
| `MinimarketIntegrationTest` | Integración (SpringBootTest) | 10 | ✅ Todas pasan |
| **Total** | | **37** | **✅ 0 fallos** |

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

---

## Seguridad Implementada

| Mecanismo | Implementación | Protege contra |
|---|---|---|
| JWT Stateless | jjwt 0.11.5 + HS256 | Acceso no autorizado, session hijacking |
| BCrypt | BCryptPasswordEncoder | Robo de contraseñas en BD |
| X-Content-Type-Options | Header HTTP | MIME sniffing / XSS |
| Content-Security-Policy | Header HTTP | XSS, inyección de contenido |
| HSTS | Header HTTP | Downgrade attacks |
| @PreAuthorize | Spring Method Security | Acceso no autorizado por rol |
| Session STATELESS | SessionCreationPolicy | Session fixation attacks |

---

## Payload del JWT

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
