import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

abstract class SanitizeComposeRuntimeJs : DefaultTask() {
    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val runtimeFile: RegularFileProperty

    @TaskAction
    fun sanitize() {
        val file = runtimeFile.asFile.get()
        if (!file.exists()) return

        val bytes = file.readBytes()
        val firstNullByte = bytes.indexOf(0.toByte())
        if (firstNullByte >= 0) {
            file.writeBytes(bytes.copyOf(firstNullByte))
        }
    }
}

kotlin {
    js {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared)

            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
        }
    }
}

val stageDesktopDownloads by tasks.registering(Copy::class) {
    group = "distribution"
    description = "Stages desktop installers into the web distribution downloads directory when they exist."
    from(rootProject.layout.projectDirectory.dir("desktopApp/build/compose/binaries/main/dmg")) {
        include("Orma-1.0.0.dmg")
        rename("Orma-1.0.0.dmg", "orma-desktop-mac.dmg")
    }
    from(rootProject.layout.projectDirectory.dir("desktopApp/build/compose/binaries/main/msi")) {
        include("Orma-1.0.0.msi")
        rename("Orma-1.0.0.msi", "orma-desktop-windows.msi")
    }
    into(layout.buildDirectory.dir("dist/js/productionExecutable/downloads"))
}

val stageVercelSpaRouting by tasks.registering {
    group = "distribution"
    description = "Writes Vercel SPA fallback routing for direct catalog links."
    val outputFile = layout.buildDirectory.file("dist/js/productionExecutable/vercel.json")
    outputs.file(outputFile)
    doLast {
        outputFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText(
                """
	                {
	                  "rewrites": [
	                    { "source": "/catalog/:workspaceId", "destination": "/" },
	                    { "source": "/catalog/:workspaceId/:path*", "destination": "/" },
	                    { "source": "/((?!composeResources/.*|downloads/.*|.*\\.(?:js|css|wasm|map|png|jpg|jpeg|svg|ico|json|webmanifest|ttf|otf|woff|woff2)$).*)", "destination": "/" }
	                  ]
	                }
                """.trimIndent(),
            )
        }
    }
}

tasks.matching { it.name == "jsBrowserDistribution" }.configureEach {
    finalizedBy(stageDesktopDownloads)
    finalizedBy(stageVercelSpaRouting)
}

val sanitizeComposeRuntimeJs by tasks.registering(SanitizeComposeRuntimeJs::class) {
    group = "build"
    description = "Removes trailing null bytes from generated Compose runtime JavaScript before webpack reads it."
    runtimeFile.set(rootProject.layout.buildDirectory.file("js/packages/Orma-webApp/kotlin/androidx-compose-runtime-runtime.js"))
    dependsOn(tasks.matching { it.name == "jsProductionExecutableCompileSync" })
}

val deleteProductionSourceMaps by tasks.registering(Delete::class) {
    group = "build"
    description = "Removes production source maps from the web distribution payload."
    delete(
        layout.buildDirectory.dir("dist/js/productionExecutable").map { outputDirectory ->
            fileTree(outputDirectory) {
                include("**/*.map")
            }
        },
    )
}

tasks.matching { it.name == "jsBrowserProductionWebpack" }.configureEach {
    dependsOn(sanitizeComposeRuntimeJs)
    finalizedBy(deleteProductionSourceMaps)
}
