package com.torneos.infrastructure.adapters.input.mappers

import com.torneos.domain.models.User
import com.torneos.infrastructure.adapters.input.dtos.RegisterRequest
import com.torneos.infrastructure.adapters.input.dtos.UserDto

fun RegisterRequest.toDomain(): User {
    return User(
        username = this.username,
        email = this.email,
        passwordHash = "", // Se llenará en el Caso de Uso al hashear
        firstName = this.firstName,
        lastName = this.lastName,
        role = this.role
    )
}

fun User.toDto(): UserDto {
    return UserDto(
        id = this.id.toString(),
        username = this.username,
        email = this.email,
        firstName = this.firstName,
        lastName = this.lastName,
        role = this.role,
        avatarUrl = this.avatarUrl
    )
}