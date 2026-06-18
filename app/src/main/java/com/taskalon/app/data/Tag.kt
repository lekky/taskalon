package com.taskalon.app.data

import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val id: String,
    val name: String,
)
