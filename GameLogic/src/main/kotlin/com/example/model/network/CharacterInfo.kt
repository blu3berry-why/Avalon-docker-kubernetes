package com.example.model.network

import com.example.model.enums.ROLE
import kotlinx.serialization.Serializable

@Serializable
data class CharacterInfo(
    var name: ROLE,
    val sees: List<String>,
)
