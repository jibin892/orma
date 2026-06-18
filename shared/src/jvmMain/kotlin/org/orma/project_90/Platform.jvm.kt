package org.orma.project_90

class JvmPlatform : Platform {
    override val name: String = "Desktop JVM ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JvmPlatform()
