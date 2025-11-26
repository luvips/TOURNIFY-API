package com.torneos.infrastructure.adapters.output.persistence

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import javax.sql.DataSource

object DatabaseFactory {

    private var datasource: DataSource? = null

    fun init(config: ApplicationConfig) {
        if (datasource == null) {
            val driverClassName = config.property("storage.driverClassName").getString()
            val jdbcUrl = config.property("storage.jdbcUrl").getString()
            val username = config.property("storage.username").getString()
            val password = config.property("storage.password").getString()
            datasource = hikari(driverClassName, jdbcUrl, username, password)
            Database.connect(datasource!!)
        }
    }

    private fun hikari(driver: String, url: String, user: String, pass: String): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = driver
        config.jdbcUrl = url
        config.username = user
        config.password = pass
        config.maximumPoolSize = 10
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()
        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}