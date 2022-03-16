package com.example.services

import io.ktor.application.*


val ApplicationCall.code: String
    get() = this.parameters["lobbyid"].toString()

val ApplicationCall.username: String
    get() = this.parameters["username"].toString()