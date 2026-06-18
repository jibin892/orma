package org.orma.project_90

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform