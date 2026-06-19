package com.orma.backend

import com.orma.backend.config.AppConfig
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun healthEndpointReturnsOk() = testApplication {
        application {
            module(AppConfig.test())
        }

        val response = client.get("/health")

        assertEquals(HttpStatusCode.OK, response.status)
        assertContains(response.body<String>(), "\"status\":\"ok\"")
    }

    @Test
    fun activeTeamInviteRequiresConfiguredDatabase() = testApplication {
        application {
            module(AppConfig.test())
        }

        val response = client.get("/onboarding/team-invites/active")

        assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
        assertContains(response.body<String>(), "\"code\":\"database_not_configured\"")
    }
}
