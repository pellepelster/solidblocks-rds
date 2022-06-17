package de.solidblocks.rds.controller

import de.solidblocks.rds.base.Database
import de.solidblocks.rds.controller.controllers.ControllersManager
import de.solidblocks.rds.controller.instances.RdsInstancesManager
import de.solidblocks.rds.controller.instances.RdsInstancesWorker
import de.solidblocks.rds.controller.instances.api.RdsInstanceCreateRequest
import de.solidblocks.rds.controller.model.controllers.ControllersRepository
import de.solidblocks.rds.controller.model.providers.ProvidersRepository
import de.solidblocks.rds.controller.model.instances.RdsInstancesRepository
import de.solidblocks.rds.controller.providers.HetznerApi
import de.solidblocks.rds.controller.providers.ProvidersWorker
import de.solidblocks.rds.controller.providers.ProvidersManager
import de.solidblocks.rds.controller.providers.api.ProviderCreateRequest
import de.solidblocks.rds.test.ManagementTestDatabaseExtension
import me.tomsdevsn.hetznercloud.HetznerCloudAPI
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.extension.ExtendWith

@EnabledIfEnvironmentVariable(named = "HCLOUD_TOKEN", matches = ".*")
@ExtendWith(ManagementTestDatabaseExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SolidlocksRdsIntegrationTest {

    private val logger = KotlinLogging.logger {}

    private val hetznerApi = HetznerApi(System.getenv("HCLOUD_TOKEN"))
    private val hetznerCloudAPI = HetznerCloudAPI(System.getenv("HCLOUD_TOKEN"))

    @BeforeAll
    fun beforeAll() {
        cleanTestbed()
    }

    @AfterAll
    fun afterAll() {
        cleanTestbed()
    }

    fun cleanTestbed() {
        assertThat(hetznerApi.deleteAllSSHKeys()).isTrue
        assertThat(hetznerApi.deleteAllVolumes()).isTrue

        hetznerCloudAPI.servers.servers.forEach {
            hetznerCloudAPI.deleteServer(it.id)
        }
    }

    @Test
    fun testCreateRdsInstance(database: Database) {

        val controllersManager = ControllersManager(ControllersRepository(database.dsl))
        val providersManager = ProvidersManager(ProvidersRepository(database.dsl), controllersManager)
        val providersWorker = ProvidersWorker(providersManager)

        val rdsInstancesManager = RdsInstancesManager(RdsInstancesRepository(database.dsl))
        val rdsInstancesWorker = RdsInstancesWorker(rdsInstancesManager, providersManager, controllersManager)

        val provider =
            providersManager.create(ProviderCreateRequest(name = "hetzner1", apiKey = System.getenv("HCLOUD_TOKEN")))

        val rdsInstance =
            rdsInstancesManager.create(RdsInstanceCreateRequest(name = "rds-instance1", provider.data!!.id))

        assertThat(providersWorker.work()).isTrue
        assertThat(rdsInstancesWorker.work()).isTrue
    }

    @Test
    @Disabled
    fun testCreateRdsInstanceOld() {
        assertThat(hetznerApi.hasServer("server1")).isFalse
        assertThat(hetznerApi.ensureVolume("server1-volume1")).isTrue
        assertThat(
            hetznerApi.ensureSSHKey(
                "server1-sshkey1",
                "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIM30a1OCQaueS/4U0IKOXs0Z9cozuz+04lPDlZCf8nLS pelle@fry"
            )
        ).isTrue

        assertThat(hetznerApi.ensureServer("server1", "server1-volume1", "", "server1-sshkey1")).isNotNull
        assertThat(hetznerApi.ensureServer("server1", "server1-volume1", "", "server1-sshkey1")).isNotNull
        assertThat(hetznerApi.hasServer("server1")).isTrue
        assertThat(hetznerApi.deleteServer("server1")).isTrue
        assertThat(hetznerApi.hasServer("server1")).isFalse
    }
}
