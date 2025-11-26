package com.torneos.application.usecases.teams

import com.torneos.domain.models.TeamMember
import com.torneos.domain.ports.TeamRepository
import com.torneos.domain.ports.UserRepository

class AddMemberUseCase(
    private val teamRepository: TeamRepository,
    private val userRepository: UserRepository
) {
    suspend fun execute(member: TeamMember): TeamMember {
        // 1. Validación Estricta: El email es obligatorio
        if (member.memberEmail.isNullOrBlank()) {
            throw IllegalArgumentException("El email es obligatorio para vincular al jugador.")
        }

        // 2. Buscar usuario en la BD
        val existingUser = userRepository.findByEmail(member.memberEmail)
            ?: throw NoSuchElementException("El usuario con email '${member.memberEmail}' no está registrado en la App.")

        val realName = "${existingUser.firstName ?: ""} ${existingUser.lastName ?: ""}".trim()

        val finalName = if (realName.isNotEmpty()) realName else (member.memberName ?: "Sin Nombre")

        val memberToSave = member.copy(
            userId = existingUser.id,
            memberName = finalName
        )

        return teamRepository.addMember(memberToSave)
    }
}