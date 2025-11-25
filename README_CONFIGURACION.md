# 🔧 Configuración de Tournify API

## 📋 Opciones de Configuración

Tienes **dos formas** de configurar las variables de la aplicación:

### Opción 1: Variables de Entorno (Recomendado para Producción)

Configura las variables de entorno del sistema:

**Windows PowerShell:**
```powershell
$env:DB_PASSWORD="tu_password_aqui"
$env:JWT_SECRET="tu_secret_jwt_aqui"
$env:AWS_ACCESS_KEY_ID="tu_access_key"
$env:AWS_SECRET_ACCESS_KEY="tu_secret_key"
```

**Linux/Mac:**
```bash
export DB_PASSWORD="tu_password_aqui"
export JWT_SECRET="tu_secret_jwt_aqui"
export AWS_ACCESS_KEY_ID="tu_access_key"
export AWS_SECRET_ACCESS_KEY="tu_secret_key"
```

### Opción 2: Directamente en application.conf (Para Desarrollo Local)

Edita el archivo `src/main/resources/application.conf` y reemplaza los valores:

```hocon
storage {
    jdbcUrl = "jdbc:postgresql://100.25.51.198:5432/tournify"
    username = "postgres"
    password = "TU_PASSWORD_AQUI"  # ⚠️ Cambiar esto
}

jwt {
    secret = "TU_SECRET_JWT_AQUI"  # ⚠️ Cambiar esto
}

aws {
    accessKey = "TU_AWS_KEY_AQUI"  # ⚠️ Cambiar esto
    secretKey = "TU_AWS_SECRET_AQUI"  # ⚠️ Cambiar esto
}
```

> ⚠️ **IMPORTANTE**: Si usas la Opción 2, **NO** hagas commit del archivo `application.conf` con tus credenciales reales. Usa `.gitignore`.

---

## 🔐 Variables Requeridas

### Base de Datos
| Variable | Valor por Defecto | Descripción |
|----------|-------------------|-------------|
| `DB_PASSWORD` | `""` (vacío) | ⚠️ **REQUERIDO** - Password de PostgreSQL |
| `DB_USER` | `postgres` | Usuario de PostgreSQL |
| `JDBC_URL` | `jdbc:postgresql://100.25.51.198:5432/tournify` | URL de conexión |

### JWT (Autenticación)
| Variable | Valor por Defecto | Descripción |
|----------|-------------------|-------------|
| `JWT_SECRET` | `secret-key-temporal-para-desarrollo-local` | ⚠️ Cambiar en producción |
| `JWT_DOMAIN` | `https://api.tournify.com` | Dominio emisor del token |
| `JWT_AUDIENCE` | `tournify-users` | Audiencia del token |

### AWS S3 (Almacenamiento de Archivos)
| Variable | Valor por Defecto | Descripción |
|----------|-------------------|-------------|
| `AWS_ACCESS_KEY_ID` | `TEST_KEY` | ⚠️ **REQUERIDO** para producción |
| `AWS_SECRET_ACCESS_KEY` | `TEST_SECRET` | ⚠️ **REQUERIDO** para producción |
| `S3_BUCKET_NAME` | `tournify-uploads-dev` | Nombre del bucket |
| `AWS_REGION` | `us-east-1` | Región de AWS |

---

## ✅ Pre-requisitos

### 1. Base de Datos PostgreSQL

Asegúrate de que:
- PostgreSQL está instalado y corriendo
- La base de datos `tournify` existe
- El usuario tiene permisos completos

**Crear la base de datos:**
```sql
CREATE DATABASE tournify;
```

### 2. Tipos ENUM en PostgreSQL

La aplicación usa tipos enum que deben existir en PostgreSQL:
- `user_role`
- `tournament_status`
- `sport_category`
- `member_role`
- `match_status`
- `registration_status`
- `payment_status`
- `elimination_mode`

### 3. Tablas

Las tablas deben estar creadas antes de ejecutar la aplicación. Si no tienes un script de migración, las tablas necesarias son:
- `users`
- `sports`
- `teams`
- `team_members`
- `tournaments`
- `tournament_groups`
- `team_registrations`
- `tournament_followers`
- `matches`
- `match_results`
- `group_standings`

---

## 🚀 Ejecutar la Aplicación

### Compilar
```bash
./gradlew clean build
```

### Ejecutar en modo desarrollo
```bash
./gradlew run
```

### Crear JAR ejecutable
```bash
./gradlew shadowJar
```

El JAR se generará en: `build/libs/tournify-all.jar`

**Ejecutar el JAR:**
```bash
java -jar build/libs/tournify-all.jar
```

---

## 🧪 Verificar que Funciona

### Health Check
```bash
curl http://localhost:8081/
```

**Respuesta esperada:**
```
Tournify Backend is Live! 🚀
```

### Registrar un Usuario
```bash
curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test123!",
    "firstName": "Test",
    "lastName": "User"
  }'
```

### Login
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!"
  }'
```

---

## ⚠️ Problemas Comunes

### Error: "Cannot connect to database"
**Causa**: Password vacío o incorrecto
**Solución**: Configura `DB_PASSWORD` correctamente

### Error: "Unresolved reference: getAppModule"
**Causa**: El IDE no ha actualizado
**Solución**:
1. Gradle Sync/Refresh
2. Build → Rebuild Project

### Error: "relation 'users' does not exist"
**Causa**: Tablas no creadas
**Solución**: Ejecuta el script de creación de tablas en PostgreSQL

### Error: "type 'user_role' does not exist"
**Causa**: Tipos ENUM no creados
**Solución**: Crea los tipos ENUM en PostgreSQL primero

---

## 📚 Documentación Adicional

- [Plan de Reparación](PLAN_REPARACION.md) - Detalles de las correcciones realizadas
- [.env.example](.env.example) - Plantilla de variables de entorno

---

## 🔒 Seguridad en Producción

⚠️ **NUNCA** hagas commit de:
- Passwords reales en `application.conf`
- Claves AWS en el código
- JWT secrets en repositorios públicos

✅ **SÍ** usa:
- Variables de entorno del sistema
- AWS IAM Roles (en EC2)
- Secrets managers (AWS Secrets Manager, HashiCorp Vault)
- `.gitignore` para archivos de configuración local
