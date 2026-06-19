package com.orma.backend.plugins

import com.orma.backend.config.AppConfig
import com.orma.backend.db.GstinRepository
import com.orma.backend.db.OnboardingRepository
import com.orma.backend.models.ApiInfoResponse
import com.orma.backend.routes.authRoutes
import com.orma.backend.routes.gstinRoutes
import com.orma.backend.routes.healthRoutes
import com.orma.backend.routes.mediaRoutes
import com.orma.backend.routes.onboardingRoutes
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting(
    config: AppConfig,
    onboardingRepository: OnboardingRepository?,
    gstinRepository: GstinRepository?,
) {
    routing {
        get("/") {
            call.respond(
                ApiInfoResponse(
                    service = "orma-backend",
                    status = "running",
                ),
            )
        }

        healthRoutes(config)
        authRoutes(config, onboardingRepository)
        onboardingRoutes(config, onboardingRepository)
        mediaRoutes(config, onboardingRepository)
        gstinRoutes(config, gstinRepository)
    }
}
