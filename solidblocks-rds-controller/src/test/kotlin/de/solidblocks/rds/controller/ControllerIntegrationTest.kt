package de.solidblocks.rds.controller

import de.solidblocks.rds.base.Database
import de.solidblocks.rds.test.ManagementTestDatabaseExtension
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.awaitility.kotlin.await
import org.hamcrest.Matchers
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration

@ExtendWith(ManagementTestDatabaseExtension::class)
@EnabledIfEnvironmentVariable(named = "HCLOUD_TOKEN", matches = ".*")
class ControllerIntegrationTest {

    @Test
    fun testManageProviders(database: Database) {
        val controller = Controller(database)

        Given {
            port(8080)
        } When {
            get("/api/v1/providers")
        } Then {
            statusCode(200)
            body("providers", empty<String>())
        }

        Given {
            port(8080)
            body(
                """
                {
                    "name": "provider1",
                    "apiKey": "provider1"
                }
            """.trimIndent()
            )
        } When {
            post(
                "/api/v1/providers"
            )
        } Then {
            statusCode(422)
            body("messages[0].attribute", equalTo("apiKey"))
            body("messages[0].code", equalTo("invalid"))
        }

        Given {
            port(8080)
            body(
                """
                {
                    "name": "provider1",
                    "apiKey": "${System.getenv("HCLOUD_TOKEN")}"
                }
            """.trimIndent()
            )
        } When {
            post(
                "/api/v1/providers"
            )
        } Then {
            statusCode(201)
            body("provider.status", equalTo("UNKNOWN"))
        }

        val id: String = Given {
            port(8080)
        } When {
            get("/api/v1/providers")
        } Then {
            statusCode(200)
            body("providers", hasSize<String>(1))
            body("providers[0].name", equalTo("provider1"))
        } Extract {
            path("providers[0].id")
        }

        Given {
            port(8080)
        } When {
            get("/api/v1/providers/${id}")
        } Then {
            statusCode(200)
            body("provider.name", equalTo("provider1"))
        }

        await.atMost(Duration.ofSeconds(120)).until {
            val status: String = Given {
                port(8080)
            } When {
                get("/api/v1/providers/${id}")
            } Then {
                statusCode(200)
                body("provider.name", equalTo("provider1"))
            } Extract {
                path("provider.status")
            }

            status == "HEALTHY"
        }
    }
}
