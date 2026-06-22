package com.orma.backend

import com.orma.backend.config.AppConfig
import com.orma.backend.db.DashboardRepository
import com.orma.backend.db.DatabaseFactory
import com.orma.backend.db.GstinRepository
import com.orma.backend.db.OnboardingRepository
import com.orma.backend.plugins.configureHTTP
import com.orma.backend.plugins.configureRouting
import com.orma.backend.plugins.configureSerialization
import com.orma.backend.plugins.configureStatusPages
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    val config = AppConfig.load()

    embeddedServer(Netty, host = config.host, port = config.port) {
        module(config)
    }.start(wait = true)
}

fun Application.module(config: AppConfig = AppConfig.load()) {
    val dataSource = DatabaseFactory.dataSource(config)
    if (config.runMigrations) {
        DatabaseFactory.migrate(dataSource)
    }
    closeOnStop(dataSource)

    val onboardingRepository = dataSource?.let { OnboardingRepository(it) }
    val dashboardRepository = dataSource?.let { DashboardRepository(it, config) }
    val gstinRepository = dataSource?.let { GstinRepository(it) }

    configureSerialization()
    configureHTTP(config)
    configureStatusPages()
    configureRouting(config, onboardingRepository, dashboardRepository, gstinRepository)
}

private fun Application.closeOnStop(dataSource: HikariDataSource?) {
    if (dataSource == null) return
    monitor.subscribe(ApplicationStopped) {
        dataSource.close()
    }
}
