package com.marconi.note

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform