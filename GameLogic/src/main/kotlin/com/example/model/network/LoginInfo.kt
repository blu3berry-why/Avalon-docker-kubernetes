package com.example.model.network

import com.example.model.db.User
import kotlinx.serialization.Serializable

@Serializable
data class LoginInfo(
    val username: String?,
    val password: String?,
    val email: String?,
    var friends: MutableList<LoginInfo>?,
)
