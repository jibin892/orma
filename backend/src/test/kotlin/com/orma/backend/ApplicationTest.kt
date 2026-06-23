package com.orma.backend

import com.orma.backend.config.AppConfig
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
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
    fun teamInviteEndpointsAreRemoved() = testApplication {
        application {
            module(AppConfig.test())
        }

        val response = client.get("/onboarding/team-invites/active")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun dashboardSummaryRequiresConfiguredDatabase() = testApplication {
        application {
            module(AppConfig.test())
        }

        val response = client.get("/dashboard/summary")

        assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
        assertContains(response.body<String>(), "\"code\":\"database_not_configured\"")
    }

    @Test
    fun notificationEnableRequiresDeviceToken() = testApplication {
        application {
            module(AppConfig.test())
        }

        val response = client.post("/onboarding/notifications") {
            contentType(ContentType.Application.Json)
            setBody("""{"enabled":true}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertContains(response.body<String>(), "\"code\":\"notification_token_required\"")
    }

    @Test
    fun metaWebhookVerificationReturnsChallengeForMatchingToken() = testApplication {
        application {
            module(AppConfig.test().copy(metaWebhookVerifyToken = "orma-test-token"))
        }

        val response = client.get(
            "/webhooks/meta?hub.mode=subscribe&hub.verify_token=orma-test-token&hub.challenge=challenge-123",
        )

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("challenge-123", response.bodyAsText())
    }

    @Test
    fun metaWebhookVerificationRejectsInvalidToken() = testApplication {
        application {
            module(AppConfig.test().copy(metaWebhookVerifyToken = "orma-test-token"))
        }

        val response = client.get(
            "/webhooks/meta?hub.mode=subscribe&hub.verify_token=wrong&hub.challenge=challenge-123",
        )

        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertContains(response.body<String>(), "\"code\":\"meta_webhook_verification_failed\"")
    }
}
