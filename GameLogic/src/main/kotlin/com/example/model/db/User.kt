package com.example.model.db

import kotlinx.serialization.Serializable

@Serializable
data class User(
    var _id:String?,
    var username: String,
    var password: String,
    var email: String?,
    var salt: String?,
    var friends: MutableList<User>?,
)
