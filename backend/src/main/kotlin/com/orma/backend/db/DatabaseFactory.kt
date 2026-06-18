package com.orma.backend.db

import com.orma.backend.config.AppConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway

object DatabaseFactory {
    fun dataSource(config: AppConfig): HikariDataSource? {
        val jdbcUrl = config.databaseUrl ?: return null

        val hikariConfig = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            username = config.databaseUser
            password = config.databasePassword
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
            minimumIdle = 1
            poolName = "orma-postgres"
        }

        return HikariDataSource(hikariConfig)
    }

    fun migrate(dataSource: HikariDataSource?) {
        dataSource ?: return

        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load()
            .migrate()
    }
}
