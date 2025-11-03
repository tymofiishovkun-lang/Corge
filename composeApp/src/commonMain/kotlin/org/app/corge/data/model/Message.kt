package org.app.corge.data.model

data class Message(
    val id: Long,
    val orderIndex: Int,
    val type: MessageType,
    val category: String,
    val textEn: String,
    val illustrationName: String?,
    val ritual: String?,
    val whyItMatters: String?,
    val recommendedTime: String?,
    val durationSeconds: Int?,
    val breathingRelated: Boolean
)

enum class MessageType { PHRASE, TASK, BREATHING }