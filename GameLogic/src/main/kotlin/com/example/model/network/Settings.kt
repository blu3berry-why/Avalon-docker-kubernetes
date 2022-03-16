package com.example.model.network

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    var assassin: Boolean,
    var mordred: Boolean,
    var morgana: Boolean,
    var oberon: Boolean,
    var percival: Boolean,
    var arnold: Boolean,
)
