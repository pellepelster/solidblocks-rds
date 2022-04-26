plugins {
    id("de.solidblocks.rds.kotlin-library-conventions")
    id("nu.studer.jooq") version "5.2.1"
}

dependencies {
    api("org.jooq:jooq:3.16.4")

    implementation("org.liquibase:liquibase-core:4.6.2")
    runtimeOnly("com.mattbertolini:liquibase-slf4j:4.0.0")

    implementation("com.zaxxer:HikariCP:3.4.5")

    jooqGenerator("org.jooq:jooq-meta-extensions-liquibase")
    jooqGenerator("org.liquibase:liquibase-core")
    jooqGenerator("org.yaml:snakeyaml:1.28")
    jooqGenerator("org.slf4j:slf4j-jdk14:1.7.30")

    testImplementation("org.hamcrest:hamcrest:2.2")
}

jooq {
    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(false)

            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN

                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"

                    target = org.jooq.meta.jaxb.Target().withPackageName("de.solidblocks.rds.controller.model")

                    database.apply {
                        name = "org.jooq.meta.extensions.liquibase.LiquibaseDatabase"
                        properties.add(
                            org.jooq.meta.jaxb.Property().withKey("scripts")
                                .withValue("src/main/resources/db/changelog/db.changelog-master.yaml")
                        )
                        properties.add(
                            org.jooq.meta.jaxb.Property().withKey("includeLiquibaseTables").withValue("false")
                        )
                    }
                }
            }
        }
    }
}
