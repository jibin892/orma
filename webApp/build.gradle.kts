plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
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
