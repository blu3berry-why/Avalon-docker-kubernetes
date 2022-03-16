package com.example.model.db

import com.example.model.enums.ROLE
import kotlinx.serialization.Serializable

@Serializable
data class UserRoleMap (
    val userName: String,
    val role: ROLE
        )

