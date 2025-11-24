package com.torneos.domain.ports

import com.torneos.domain.models.News
import java.util.UUID

interface NewsRepository {
    suspend fun create(news: News): News
    suspend fun findAll(): List<News>
    suspend fun findById(id: UUID): News?
    suspend fun delete(id: UUID): Boolean
}