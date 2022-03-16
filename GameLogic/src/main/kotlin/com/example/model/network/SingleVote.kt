package com.example.model.network

import kotlinx.serialization.Serializable

@Serializable
data class SingleVote(
    val username: String,
    val uservote: Boolean,
)
