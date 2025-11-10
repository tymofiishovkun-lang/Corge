package org.app.corge.data.model

data class Theme(
    val id: String,
    val name: String,
    val isPaid: Boolean,
    val purchased: Boolean,
    val previewRes: String,
    val primaryColor: Long,
    val splashText: String,
    val price: Double?
)
