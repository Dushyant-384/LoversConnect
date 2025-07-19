package com.arnav.loversconnect

    data class Message(
        val text: String = "",
        val senderId: String = "",
        val timestamp: Long = System.currentTimeMillis()
    )