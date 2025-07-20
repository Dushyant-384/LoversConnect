package com.arnav.loversconnect

// Message.kt
data class Message(
    var id: String? = null, // Add this ID field
    val text: String = "",
    val senderId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)