package com.merkost.honq

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform