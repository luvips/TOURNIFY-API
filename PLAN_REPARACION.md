# 🔧 PLAN DE REPARACIÓN - TOURNIFY API

## 📌 RESUMEN EJECUTIVO

Este plan detalla las correcciones necesarias para arreglar todos los errores identificados en la aplicación Tournify API, organizados por prioridad y dependencias.

---

## 🎯 FASE 1: CORRECCIONES CRÍTICAS DE INFRAESTRUCTURA

### 1.1 Arreglar Configuración de Base de Datos
**Archivo:** `src/main/resources/application.conf`
**Prioridad:** 🔴 CRÍTICA - Bloquea runtime

**Cambios necesarios:**
```hocon
# ANTES:
jdbcUrl = "jdbc:postgresql://100.25.51.198/"
password = ""

# DESPUÉS:
jdbcUrl = "jdbc:postgresql://100.25.51.198:5432/tournify"
password = ${?DB_PASSWORD}  # Se debe configurar variable de entorno
```

**Razón:**
- La URL no especifica la base de datos destino
- La contraseña vacía causará fallo de autenticación
- Necesitamos agregar el puerto explícitamente para claridad

---

### 1.2 Arreglar Inyección de Dependencias para BCryptAuthService
**Archivo:** `src/main/kotlin/com/torneos/infrastructure/configuration/DI.kt`
**Prioridad:** 🔴 CRÍTICA - Bloquea runtime

**Problema Actual:**
```kotlin
single<AuthServicePort> { BCryptAuthService(get()) }
```
Koin no puede resolver `ApplicationConfig` automáticamente porque viene del contexto de Application.

**Solución:**
Cambiar el módulo para que reciba el config como parámetro:

```kotlin
// DI.kt - MODIFICAR función
fun getAppModule(config: ApplicationConfig) = module {
    // Services
    single<AuthServicePort> { BCryptAuthService(config) }
    single<FileStoragePort> { S3Service(config) }

    // ... resto igual
}
```

**Y actualizar Application.kt:**
```kotlin
// Application.kt - MODIFICAR
fun Application.module() {
    install(Koin) {
        modules(getAppModule(environment.config))
    }
    // ... resto
}
```

**Impacto:** Permite que los servicios accedan a la configuración correctamente.

---

### 1.3 Unificar Versión de Ktor Client
**Archivo:** `build.gradle.kts`
**Prioridad:** 🟡 MEDIA - No bloquea, pero es inconsistente

**Cambio:**
```kotlin
// ANTES:
implementation("io.ktor:ktor-client-cio:3.0.0")

// DESPUÉS:
implementation("io.ktor:ktor-client-cio:$ktor_version")
```

**Razón:** Mantener consistencia con las demás dependencias Ktor (v3.3.2)

---

## 🎯 FASE 2: IMPLEMENTAR REPOSITORIOS FALTANTES

### 2.1 Crear PostgresRegistrationRepository
**Archivo:** `src/main/kotlin/com/torneos/infrastructure/adapters/output/persistence/repositories/PostgresRegistrationRepository.kt`
**Prioridad:** 🔴 CRÍTICA - Bloquea compilación

**Contexto:**
- La tabla `TeamRegistrationsTable` YA EXISTE
- El repositorio `TeamRepository` ya tiene métodos relacionados pero mezclados
- Necesitamos extraer la funcionalidad específica de registros

**Implementación:**
Crear clase que implemente todos los métodos de `RegistrationRepository`:

```kotlin
package com.torneos.infrastructure.adapters.output.persistence.repositories

import com.torneos.domain.models.TeamRegistration
import com.torneos.domain.enums.RegistrationStatus
import com.torneos.domain.enums.PaymentStatus
import com.torneos.domain.ports.RegistrationRepository
import com.torneos.infrastructure.adapters.output.persistence.DatabaseFactory.dbQuery
import com.torneos.infrastructure.adapters.output.persistence.tables.TeamRegistrationsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

class PostgresRegistrationRepository : RegistrationRepository {

    private fun ResultRow.toTeamRegistration() = TeamRegistration(
        id = this[TeamRegistrationsTable.id],
        tournamentId = this[TeamRegistrationsTable.tournamentId],
        teamId = this[TeamRegistrationsTable.teamId],
        groupId = this[TeamRegistrationsTable.groupId],
        registrationDate = this[TeamRegistrationsTable.registrationDate],
        status = this[TeamRegistrationsTable.status],
        paymentStatus = this[TeamRegistrationsTable.paymentStatus],
        additionalInfo = this[TeamRegistrationsTable.additionalInfo],
        seedNumber = this[TeamRegistrationsTable.seedNumber],
        approvedAt = this[TeamRegistrationsTable.approvedAt],
        approvedBy = this[TeamRegistrationsTable.approvedBy]
    )

    override suspend fun create(registration: TeamRegistration): TeamRegistration = dbQuery {
        TeamRegistrationsTable.insert {
            it[id] = registration.id
            it[tournamentId] = registration.tournamentId
            it[teamId] = registration.teamId
            it[groupId] = registration.groupId
            it[status] = registration.status
            it[paymentStatus] = registration.paymentStatus
            it[additionalInfo] = registration.additionalInfo
            it[seedNumber] = registration.seedNumber
            it[approvedAt] = registration.approvedAt
            it[approvedBy] = registration.approvedBy
        }
        registration
    }

    override suspend fun findById(id: UUID): TeamRegistration? = dbQuery {
        TeamRegistrationsTable.selectAll().where { TeamRegistrationsTable.id eq id }
            .map { it.toTeamRegistration() }
            .singleOrNull()
    }

    override suspend fun findAll(): List<TeamRegistration> = dbQuery {
        TeamRegistrationsTable.selectAll()
            .map { it.toTeamRegistration() }
    }

    override suspend fun update(registration: TeamRegistration): TeamRegistration? = dbQuery {
        val rows = TeamRegistrationsTable.update({ TeamRegistrationsTable.id eq registration.id }) {
            it[status] = registration.status
            it[paymentStatus] = registration.paymentStatus
            it[groupId] = registration.groupId
            it[seedNumber] = registration.seedNumber
            it[approvedAt] = registration.approvedAt
            it[approvedBy] = registration.approvedBy
        }
        if (rows > 0) registration else null
    }

    override suspend fun delete(id: UUID): Boolean = dbQuery {
        TeamRegistrationsTable.deleteWhere { TeamRegistrationsTable.id eq id } > 0
    }

    override suspend fun findByTournamentId(tournamentId: UUID): List<TeamRegistration> = dbQuery {
        TeamRegistrationsTable.selectAll().where { TeamRegistrationsTable.tournamentId eq tournamentId }
            .map { it.toTeamRegistration() }
    }

    override suspend fun findByTeamId(teamId: UUID): List<TeamRegistration> = dbQuery {
        TeamRegistrationsTable.selectAll().where { TeamRegistrationsTable.teamId eq teamId }
            .map { it.toTeamRegistration() }
    }

    override suspend fun findByTournamentAndTeam(tournamentId: UUID, teamId: UUID): TeamRegistration? = dbQuery {
        TeamRegistrationsTable.selectAll()
            .where {
                (TeamRegistrationsTable.tournamentId eq tournamentId) and
                (TeamRegistrationsTable.teamId eq teamId)
            }
            .map { it.toTeamRegistration() }
            .singleOrNull()
    }

    override suspend fun updateStatus(id: UUID, status: RegistrationStatus): Boolean = dbQuery {
        TeamRegistrationsTable.update({ TeamRegistrationsTable.id eq id }) {
            it[TeamRegistrationsTable.status] = status
        } > 0
    }

    override suspend fun updatePaymentStatus(id: UUID, status: PaymentStatus): Boolean = dbQuery {
        TeamRegistrationsTable.update({ TeamRegistrationsTable.id eq id }) {
            it[paymentStatus] = status
        } > 0
    }
}
```

**Actualizar DI.kt:**
```kotlin
// Descomentar línea 28
single<RegistrationRepository> { PostgresRegistrationRepository() }
```

---

### 2.2 Crear PostgresTournamentGroupRepository
**Archivo:** `src/main/kotlin/com/torneos/infrastructure/adapters/output/persistence/repositories/PostgresTournamentGroupRepository.kt`
**Prioridad:** 🟡 MEDIA - Feature incompleta

**Implementación:**
```kotlin
package com.torneos.infrastructure.adapters.output.persistence.repositories

import com.torneos.domain.models.TournamentGroup
import com.torneos.domain.ports.TournamentGroupRepository
import com.torneos.infrastructure.adapters.output.persistence.DatabaseFactory.dbQuery
import com.torneos.infrastructure.adapters.output.persistence.tables.TournamentGroupsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

class PostgresTournamentGroupRepository : TournamentGroupRepository {

    private fun ResultRow.toTournamentGroup() = TournamentGroup(
        id = this[TournamentGroupsTable.id],
        tournamentId = this[TournamentGroupsTable.tournamentId],
        groupName = this[TournamentGroupsTable.groupName],
        displayOrder = this[TournamentGroupsTable.displayOrder],
        createdAt = this[TournamentGroupsTable.createdAt]
    )

    override suspend fun create(group: TournamentGroup): TournamentGroup = dbQuery {
        TournamentGroupsTable.insert {
            it[id] = group.id
            it[tournamentId] = group.tournamentId
            it[groupName] = group.groupName
            it[displayOrder] = group.displayOrder
        }
        group
    }

    override suspend fun findByTournament(tournamentId: UUID): List<TournamentGroup> = dbQuery {
        TournamentGroupsTable.selectAll()
            .where { TournamentGroupsTable.tournamentId eq tournamentId }
            .orderBy(TournamentGroupsTable.displayOrder to SortOrder.ASC)
            .map { it.toTournamentGroup() }
    }

    override suspend fun findById(id: UUID): TournamentGroup? = dbQuery {
        TournamentGroupsTable.selectAll().where { TournamentGroupsTable.id eq id }
            .map { it.toTournamentGroup() }
            .singleOrNull()
    }

    override suspend fun delete(id: UUID): Boolean = dbQuery {
        TournamentGroupsTable.deleteWhere { TournamentGroupsTable.id eq id } > 0
    }
}
```

**Actualizar DI.kt:**
```kotlin
// Agregar después de MatchRepository
single<TournamentGroupRepository> { PostgresTournamentGroupRepository() }
```

---

### 2.3 Crear PostgresStandingRepository (Opcional)
**Archivo:** `src/main/kotlin/com/torneos/infrastructure/adapters/output/persistence/repositories/PostgresStandingRepository.kt`
**Prioridad:** 🟢 BAJA - Funcionalidad ya existe en MatchRepository

**Opciones:**
1. **Mantener en MatchRepository** (recomendado): Ya implementado, funciona
2. **Crear repo separado**: Mejor separación de responsabilidades

**Si se decide separar:**
```kotlin
package com.torneos.infrastructure.adapters.output.persistence.repositories

import com.torneos.domain.models.GroupStanding
import com.torneos.domain.ports.StandingRepository
import com.torneos.infrastructure.adapters.output.persistence.DatabaseFactory.dbQuery
import com.torneos.infrastructure.adapters.output.persistence.tables.GroupStandingsTable
import org.jetbrains.exposed.sql.*
import java.util.UUID

class PostgresStandingRepository : StandingRepository {

    override suspend fun getStandingsByGroup(groupId: UUID): List<GroupStanding> = dbQuery {
        GroupStandingsTable.selectAll()
            .where { GroupStandingsTable.groupId eq groupId }
            .orderBy(
                GroupStandingsTable.points to SortOrder.DESC,
                GroupStandingsTable.goalDifference to SortOrder.DESC
            )
            .map { it.toGroupStanding() }
    }

    override suspend fun updateStandings(groupId: UUID): Boolean {
        // TODO: Implementar lógica de recalcular tabla basado en partidos
        return true
    }

    private fun ResultRow.toGroupStanding() = GroupStanding(
        id = this[GroupStandingsTable.id],
        groupId = this[GroupStandingsTable.groupId],
        teamId = this[GroupStandingsTable.teamId],
        played = this[GroupStandingsTable.played],
        won = this[GroupStandingsTable.won],
        drawn = this[GroupStandingsTable.drawn],
        lost = this[GroupStandingsTable.lost],
        goalsFor = this[GroupStandingsTable.goalsFor],
        goalsAgainst = this[GroupStandingsTable.goalsAgainst],
        goalDifference = this[GroupStandingsTable.goalDifference],
        points = this[GroupStandingsTable.points],
        position = this[GroupStandingsTable.position]
    )
}
```

---

## 🎯 FASE 3: CORRECCIONES DE CÓDIGO

### 3.1 Completar Implementación de getMatchEvents
**Archivo:** `src/main/kotlin/com/torneos/infrastructure/adapters/output/persistence/repositories/PostgresMatchRepository.kt`
**Prioridad:** 🟡 MEDIA

**Cambiar línea 96-98:**
```kotlin
override suspend fun getMatchEvents(matchId: UUID): List<MatchResult> = dbQuery {
    MatchResultsTable.selectAll()
        .where { MatchResultsTable.matchId eq matchId }
        .orderBy(MatchResultsTable.eventTime to SortOrder.ASC)
        .map { it.toMatchResult() }
}

// Agregar helper
private fun ResultRow.toMatchResult() = MatchResult(
    id = this[MatchResultsTable.id],
    matchId = this[MatchResultsTable.matchId],
    teamId = this[MatchResultsTable.teamId],
    playerId = this[MatchResultsTable.playerId],
    eventType = this[MatchResultsTable.eventType],
    eventTime = this[MatchResultsTable.eventTime]
)
```

---

## 🎯 FASE 4: VALIDACIÓN Y PRUEBAS

### 4.1 Verificar Compilación
```bash
./gradlew clean build
```

**Checklist:**
- [ ] Sin errores de compilación
- [ ] Todas las dependencias resueltas
- [ ] Shadow JAR se genera correctamente

---

### 4.2 Verificar Conexión a Base de Datos
**Pre-requisitos:**
- Configurar variable de entorno `DB_PASSWORD`
- Asegurar que la BD `tournify` existe en PostgreSQL
- Verificar que las tablas estén creadas

**Comandos:**
```bash
# Configurar password (Linux/Mac)
export DB_PASSWORD="tu_password_real"

# Windows PowerShell
$env:DB_PASSWORD="tu_password_real"

# Ejecutar aplicación
./gradlew run
```

---

### 4.3 Probar Endpoints Críticos

#### Test 1: Registro de Usuario
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

#### Test 2: Login
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!"
  }'
```

#### Test 3: Crear Torneo (con token)
```bash
curl -X POST http://localhost:8081/tournaments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "name": "Torneo de Prueba",
    "sportId": "<UUID>",
    "startDate": "2025-12-01T10:00:00Z",
    "maxTeams": 16
  }'
```

#### Test 4: Inscribir Equipo
```bash
curl -X POST http://localhost:8081/tournaments/<TOURNAMENT_ID>/join \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "teamId": "<TEAM_UUID>"
  }'
```

---

## 🎯 FASE 5: MEJORAS OPCIONALES (Post-Corrección)

### 5.1 Mejorar Manejo de Errores
- Agregar excepciones personalizadas
- Logging estructurado con contexto
- Respuestas HTTP consistentes

### 5.2 Optimizaciones de Base de Datos
- Agregar índices en columnas frecuentemente consultadas
- Implementar paginación en todos los endpoints de listado
- Cache para consultas frecuentes

### 5.3 Seguridad
- Validación de input más robusta
- Rate limiting
- CORS configurado correctamente
- Rotación de JWT secrets

---

## 📊 CHECKLIST FINAL

### Pre-Implementación
- [ ] Backup de código actual
- [ ] Crear rama git para correcciones
- [ ] Revisar este plan con el equipo

### Durante Implementación
- [ ] Fase 1.1: Config DB ✅
- [ ] Fase 1.2: DI para AuthService ✅
- [ ] Fase 1.3: Unificar versión Ktor ✅
- [ ] Fase 2.1: PostgresRegistrationRepository ✅
- [ ] Fase 2.2: PostgresTournamentGroupRepository ✅
- [ ] Fase 2.3: PostgresStandingRepository (opcional) ⚠️
- [ ] Fase 3.1: getMatchEvents completo ✅

### Post-Implementación
- [ ] Compilación exitosa
- [ ] Tests unitarios pasan
- [ ] Tests de integración funcionan
- [ ] Documentación actualizada
- [ ] Merge a main

---

## ⚠️ RIESGOS Y MITIGACIONES

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| BD no existe en servidor | Media | Alto | Crear script de inicialización |
| Credenciales AWS inválidas | Alta | Medio | Validar al inicio, fallback graceful |
| Password BD no configurado | Alta | Alto | Documentar variables de entorno |
| Conflictos de merge | Baja | Medio | Trabajar en rama separada |

---

## 📝 NOTAS ADICIONALES

1. **TeamRepository vs RegistrationRepository**:
   - Actualmente TeamRepository tiene métodos de registro mezclados
   - Al crear PostgresRegistrationRepository, NO eliminar los métodos de TeamRepository
   - Considerar refactor futuro para eliminar duplicación

2. **Configuración de Entorno**:
   - Crear archivo `.env.example` con todas las variables
   - Documentar variables en README
   - Considerar usar Docker para desarrollo local

3. **Base de Datos**:
   - Verificar que todas las tablas tengan sus ENUM types creados en PostgreSQL
   - Considerar migration scripts (Flyway/Liquibase) para futuro

4. **S3 Service**:
   - Credenciales "TEST_KEY" solo funcionan en desarrollo
   - Para producción, usar IAM Roles en EC2 (no hardcodear credentials)

---

## 🚀 ORDEN DE EJECUCIÓN RECOMENDADO

1. ✅ Configurar variables de entorno (DB_PASSWORD)
2. ✅ Aplicar Fase 1 (Configuración e infraestructura)
3. ✅ Aplicar Fase 2.1 (PostgresRegistrationRepository) - CRÍTICO
4. ✅ Compilar y verificar
5. ✅ Aplicar Fase 2.2 y 2.3 (Otros repositorios)
6. ✅ Aplicar Fase 3 (Correcciones menores)
7. ✅ Tests completos
8. ✅ Deploy a staging

---

**Estimación de tiempo:** 2-3 horas de implementación + 1 hora de testing

**Riesgo general:** 🟢 BAJO (cambios bien definidos, sin breaking changes mayores)
