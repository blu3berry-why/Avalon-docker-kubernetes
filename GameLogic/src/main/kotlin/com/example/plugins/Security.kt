package com.example.plugins

import io.ktor.auth.*
import io.ktor.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.sessions.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

data class MySession(val username: String)


fun Application.configureSecurity() {

    authentication {
        jwt("auth-jwt") {
            val jwtAudience = environment.config.property("jwt.audience").getString()
            realm = environment.config.property("jwt.realm").getString()
            verifier(
                JWT
                    .require(Algorithm.HMAC256(environment.config.property("jwt.secret").getString()))
                    .withAudience(jwtAudience)
                    .withIssuer(environment.config.property("jwt.domain").getString())
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
            }
        }
    }



    install(Sessions) {
        cookie<MySession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }

    routing {
        /*authenticate("auth-jwt") {
            //test
            get("/test") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal!!.payload.getClaim("username").asString()
                val expiresAt = principal.expiresAt?.time?.minus(System.currentTimeMillis())
                call.respondText("Hello, $username! Token is expired at $expiresAt ms.")
            }
        }*/
        get("/session/increment") {
            val session = call.sessions.get<MySession>() ?: MySession("")
            call.sessions.set(session.copy(username = session.username + "a"))
            call.respondText("Counter is ${session.username}. Refresh to increment.")
        }
    }
}
