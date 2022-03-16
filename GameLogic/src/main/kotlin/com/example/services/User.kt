package com.example.services

import com.example.model.enums.ROLE
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*


fun getUsernameFromToken(call: ApplicationCall): String{
    val principal = call.principal<JWTPrincipal>()
    return principal!!.payload.getClaim("username").asString()
}

