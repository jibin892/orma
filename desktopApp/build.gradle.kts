import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val ormaMacSign = providers.environmentVariable("ORMA_MAC_SIGN")
    .orElse(providers.gradleProperty("compose.desktop.mac.sign"))
    .map { it.equals("true", ignoreCase = true) }
    .orElse(false)
val ormaMacSigningIdentity = providers.environmentVariable("ORMA_MAC_SIGNING_IDENTITY")
    .orElse(providers.gradleProperty("compose.desktop.mac.signing.identity"))
val ormaMacSigningKeychain = providers.environmentVariable("ORMA_MAC_SIGNING_KEYCHAIN")
    .orElse(providers.gradleProperty("compose.desktop.mac.signing.keychain"))
val ormaMacSigningPrefix = providers.environmentVariable("ORMA_MAC_SIGNING_PREFIX")
    .orElse(providers.gradleProperty("compose.desktop.mac.signing.prefix"))
val ormaMacNotarizationAppleId = providers.environmentVariable("ORMA_MAC_NOTARIZATION_APPLE_ID")
    .orElse(providers.gradleProperty("compose.desktop.mac.notarization.appleID"))
val ormaMacNotarizationPassword = providers.environmentVariable("ORMA_MAC_NOTARIZATION_PASSWORD")
    .orElse(providers.gradleProperty("compose.desktop.mac.notarization.password"))
val ormaMacNotarizationTeamId = providers.environmentVariable("ORMA_MAC_NOTARIZATION_TEAM_ID")
    .orElse(providers.gradleProperty("compose.desktop.mac.notarization.teamID"))

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(projects.shared)
    implementation(compose.desktop.currentOs)
}

compose.desktop {
    application {
        mainClass = "org.orma.project_90.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Orma"
            packageVersion = "1.0.0"

            macOS {
                iconFile.set(project.file("src/main/resources/orma-app-icon.icns"))
                bundleID = "com.orma.desktop"
                signing {
                    sign.set(ormaMacSign)
                    identity.set(ormaMacSigningIdentity)
                    keychain.set(ormaMacSigningKeychain)
                    prefix.set(ormaMacSigningPrefix)
                }
                notarization {
                    appleID.set(ormaMacNotarizationAppleId)
                    password.set(ormaMacNotarizationPassword)
                    teamID.set(ormaMacNotarizationTeamId)
                }
            }
        }
    }
}
