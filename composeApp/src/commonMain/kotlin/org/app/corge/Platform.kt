package org.app.corge

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform