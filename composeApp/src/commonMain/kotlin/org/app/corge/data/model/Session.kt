package org.app.corge.data.model

data class Session(
    val id: Long,
    val messageId: Long,
    val date: String,
    val startedAt: String?,
    val durationSeconds: Int,
    val done: Boolean,
    val note: String?
)