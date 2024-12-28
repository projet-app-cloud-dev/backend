import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.gradle.tasks.bundling.BootJar
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.zip.ZipInputStream

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "fr.pokecloud"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.postgresql:postgresql:42.7.4")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("com.h2database:h2:2.3.232")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named<BootJar>("bootJar") {
    archiveClassifier.set("boot")
}

tasks.named<Jar>("jar") {
    archiveClassifier.set("")
}

tasks.register<CreateDatabaseTask>("createDatabase") {
    outputDirectory.set(layout.buildDirectory.dir("databases"))
    sourceSets.main.get().resources.srcDirs(outputDirectory.get().asFile.absolutePath)
}

tasks.processResources {
    dependsOn("createDatabase")
}

abstract class CreateDatabaseTask : DefaultTask() {
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    init {
        outputs.upToDateWhen { false }
    }


    @TaskAction
    fun action() {
        val input = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build().send(
            HttpRequest.newBuilder(URI.create("https://github.com/PokemonTCG/pokemon-tcg-data/archive/refs/tags/v2.14.zip"))
                .GET().build(), HttpResponse.BodyHandlers.ofInputStream()
        )

        val pattern = Regex("^.+\\/cards\\/en\\/.+\\.json$")

        val out = BufferedOutputStream(FileOutputStream(outputDirectory.get().file("import.sql").asFile))
        var nbr = 0
        out.use {
            ZipInputStream(BufferedInputStream(input.body())).use {
                while (true) {
                    val entry = it.nextEntry ?: break
                    if (pattern.matches(entry.name)) {
                        val mapper: ObjectMapper = ObjectMapper()
                        val tree = mapper.readTree(it.readBytes())

                        val line = "INSERT INTO API_CARD(id, name, image_url) VALUES" + tree.joinToString(",") { node ->
                            val v = "($nbr, '${
                                node.get("name").textValue().replace("'", "''")
                            }', '${node.get("images").get("large").textValue()}')"
                            nbr += 1
                            v
                        } + ";"

                        out.write(line.toByteArray(Charsets.UTF_8))
                    }
                }
            }
        }
    }
}