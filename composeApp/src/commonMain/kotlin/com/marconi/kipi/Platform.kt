package com.marconi.kipi

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform