package com.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.model.db.Lobby
import com.example.model.db.User
import com.example.model.enums.ROLE
import com.example.model.enums.WINNER
import com.example.model.network.*
import com.example.services.*
import com.example.singleton.Singletons
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.response.*
import io.ktor.request.*
import org.litote.kmongo.eq
import org.mindrot.jbcrypt.BCrypt
import java.util.*


fun Application.configureRouting() {

    routing {

        get("/") {
            call.respondText("Im running! :)")
        }

        //------------------------------
        //   AUTHENTICATION
        //------------------------------

        post("/login") {
            val loginInfo = call.receive<LoginInfo>()

            val user = Singletons.Users.findOne(User::username eq loginInfo.username) ?: return@post call.apply {
                this.response.status(HttpStatusCode.NotFound)
            }.respond(hashMapOf("message" to "There is no user with this username"))



            if (user.password != BCrypt.hashpw(loginInfo.password, user.salt)) {
                return@post call.apply {
                    this.response.status(HttpStatusCode.Forbidden)
                }.respond(hashMapOf("message" to "Wrong password"))
            }

            val authTime = 24 * 60 * 60 * 1000
            // Check username and password
            val token = JWT.create()
                .withAudience(environment.config.property("jwt.audience").getString())
                .withIssuer(environment.config.property("jwt.domain").getString())
                .withClaim("username", user.username)
                .withExpiresAt(Date(System.currentTimeMillis() + authTime))
                .sign(Algorithm.HMAC256(environment.config.property("jwt.secret").getString()))
            call.respond(hashMapOf("token" to token, "username" to user.username))
        }

        /*  post("/login"){
              val loginInfo = call.receiveParameters()
              call.respond(hashMapOf("message" to loginInfo.toString()))
          }*/

        post("/register") {
            val loginInfo = call.receive<LoginInfo>()

            val user = Singletons.Users.findOne(User::username eq loginInfo.username)
            if (user != null) {
                call.response.status(HttpStatusCode.Conflict)
                return@post call.respond(hashMapOf("message" to "User with this username already exists."))
            }
            val salt = BCrypt.gensalt()
            val password = BCrypt.hashpw(loginInfo.password, salt)

            if (loginInfo.username == null) {
                call.response.status(HttpStatusCode.BadRequest)
                return@post call.respond(hashMapOf("message" to "New user has to have a username"))
            }

            if (loginInfo.password == null) {
                call.response.status(HttpStatusCode.BadRequest)
                return@post call.respond(hashMapOf("message" to "New user has to have a password"))
            }


            Singletons.Users.save(
                User(
                    _id = null,
                    username = loginInfo.username,
                    password = password,
                    email = loginInfo.email,
                    salt = salt,
                    friends = mutableListOf(),
                )
            )

            call.response.status(HttpStatusCode.OK)
        }

        get("/test") {
            call.respond(hashMapOf("message" to "test message"))
        }

        post("/test") {
            call.respond(hashMapOf("message" to "test message"))
        }

        authenticate("auth-jwt") {

            //------------------------------
            //   USER
            //------------------------------


            get("/user/{username}") {
                val user = Singletons.Users.findOne(User::username eq call.username)
                    ?: return@get call.response.status(HttpStatusCode.NotFound)
                call.respond(
                    LoginInfo(
                        username = user.username,
                        password = "",
                        email = user.email,
                        user.friends?.map { LoginInfo(username = it.username, null, null, null) }?.toMutableList()
                    )
                )
            }

            put("user") {
                val loginInfo = call.receive<LoginInfo>()
                val username = loginInfo.username

                val filter = User::username eq call.username

                if (username == call.parameters["username"]) {
                    Singletons.Users.findOne(filter)
                        ?: return@put call.response.status(HttpStatusCode.NotFound)

                    loginInfo.username?.let {
                        Singletons.Users.updateOne(filter, User::username eq loginInfo.username)
                    }

                    loginInfo.email?.let {
                        Singletons.Users.updateOne(filter, User::email eq loginInfo.email)
                    }

                    loginInfo.password?.let {
                        val salt = Singletons.Users.findOne(filter, User::username eq loginInfo.username)!!.salt
                        val password = BCrypt.hashpw(loginInfo.password, salt)
                        Singletons.Users.updateOne(filter, User::password eq password)
                    }
                }
                call.response.status(HttpStatusCode.OK)
            }

            delete("/user/{username}") {
                val username = getUsernameFromToken(call)
                if (username != call.username)
                    return@delete call.apply {
                        this.response.status(HttpStatusCode.PreconditionFailed)
                    }.respond(hashMapOf("message" to "Only the user can delete itself!"))


                Singletons.Users.findOne(User::username eq call.username)
                    ?: return@delete call.response.status(HttpStatusCode.NotFound)

                Singletons.Users.deleteOne(User::username eq call.username).wasAcknowledged()
                call.response.status(HttpStatusCode.NoContent)
            }


            //------------------------------
            //   LOBBY
            //------------------------------

            get("/settings/{lobbyid}") {
                val lobby = Singletons.Lobbies
                    .findOne(Lobby::lobbyCode eq call.code)
                    ?: return@get call.response.status(HttpStatusCode.NotFound)

                call.respond(lobby.settings)
            }

            put("/settings/{lobbyid}") {
                val settings = call.receive<Settings>()

                val lobby = Singletons.Lobbies
                    .findOne(Lobby::lobbyCode eq call.code)
                    ?: return@put call.response.status(HttpStatusCode.NotFound)

                lobby.settings = settings
                Singletons.Lobbies.replaceOne(Lobby::lobbyCode eq call.code, lobby)

                call.response.status(HttpStatusCode.OK)
            }

            get("getinfo/{lobbyid}") {
                val lobby = Singletons.Lobbies
                    .findOne(Lobby::lobbyCode eq call.code)
                    ?: return@get call.response.status(HttpStatusCode.NotFound)

                call.respond(lobby.info)
            }

            post("/createlobby") {
                var lobby = createLobby()

                while (Singletons.Lobbies.findOne(Lobby::lobbyCode eq lobby.lobbyCode) != null) {
                    lobby = createLobby()
                }
                Singletons.Lobbies.insertOne(lobby)
                call.respond(hashMapOf("lobbyCode" to lobby.lobbyCode))
            }

            post("/join/{lobbyid}") {
                val username = getUsernameFromToken(call)
                println(call.code)
                val lobby = findLobby(call.code) ?: return@post call.response.status(HttpStatusCode.NotFound)

                if (lobby.info.started)
                    return@post call.apply {
                        this.response.status(HttpStatusCode.Conflict)
                    }.respond(hashMapOf("message" to "The lobby that you tied to join has already started a game!"))



                if (!lobby.info.playersName.contains(username)) {
                    lobby.info.playersName.add(username)
                    Singletons.Lobbies.replaceOne(Lobby::lobbyCode eq lobby.lobbyCode, lobby)
                }
                call.response.status(HttpStatusCode.OK)
            }

            post("/leave/{lobbyid}") {
                val username = getUsernameFromToken(call)
                val lobby = findLobby(call.code) ?: return@post call.response.status(HttpStatusCode.NotFound)

                if (lobby.info.started) {
                    return@post call.apply {
                        this.response.status(HttpStatusCode.Conflict)
                    }.respond(hashMapOf("message" to "The lobby that you tied leave from has already started a game!"))
                }

                if (!lobby.info.playersName.contains(username)) {
                    lobby.info.playersName.remove(username)
                    Singletons.Lobbies.replaceOne(Lobby::lobbyCode eq lobby.lobbyCode, lobby)
                }


                call.response.status(HttpStatusCode.OK)
            }


            post("/start/{lobbyid}") {
                try {
                    findLobby(call.code)?.apply {
                        this.start()
                        Singletons.Lobbies.replaceOne(Lobby::lobbyCode eq this.lobbyCode, this)
                    } ?: return@post call.response.status(HttpStatusCode.NotFound)

                } catch (e: IllegalArgumentException) {
                    call.apply {
                        this.response.status(HttpStatusCode.Conflict)
                    }.respond(hashMapOf("message" to e.message))
                }

                call.response.status(HttpStatusCode.OK)
            }


            //------------------------------
            //   GAME
            //------------------------------

            get("/game/{lobbyid}") {
                val username = getUsernameFromToken(call)
                val lobby = findLobby(call.code) ?: return@get call.response.status(HttpStatusCode.NotFound)

                call.respond(lobby.info)
            }

            get("/game/{lobbyid}/character") {
                val username = getUsernameFromToken(call)
                val lobby = findLobby(call.code) ?: return@get call.response.status(HttpStatusCode.NotFound)
                if (lobby.hasWinner) {
                    call.response.status(HttpStatusCode.ExpectationFailed)
                    return@get call.respond(hashMapOf("message" to "This lobby has a winner"))
                }

                val role = lobby.userRoles.firstOrNull {
                    it.userName == username
                }?.role ?: return@get call.response.status(HttpStatusCode.NotFound)


                call.respond(CharacterInfo(role, lobby.sees(role, username)))
            }

            post("/game/{lobbyid}/select") {
                try {
                    findLobby(call.code)?.apply {
                        if (this.hasWinner) {
                            call.response.status(HttpStatusCode.ExpectationFailed)
                            return@post call.respond(hashMapOf("message" to "This lobby has a winner"))
                        }
                    }?.select(call.receive()) ?: return@post call.response.status(HttpStatusCode.NotFound)

                } catch (e: IllegalArgumentException) {
                    call.apply {
                        this.response.status(HttpStatusCode.Conflict)
                    }.respond(hashMapOf("message" to e.message))
                }

                call.response.status(HttpStatusCode.OK)
            }

            post("/game/{lobbyid}/vote") {
                try {
                    findLobby(call.code)?.apply {
                        if (this.hasWinner) {
                            call.response.status(HttpStatusCode.ExpectationFailed)
                            return@post call.respond(hashMapOf("message" to "This lobby has a winner"))
                        }

                    }?.vote(SingleVote(getUsernameFromToken(call), call.receive<SingleVote>().uservote))
                        ?: call.apply {
                            this.response.status(HttpStatusCode.NotFound)
                        }.respond(hashMapOf("message" to "Lobby not Found!"))
                } catch (e: IllegalArgumentException) {
                    call.apply {
                        this.response.status(HttpStatusCode.Conflict)
                    }.respond(hashMapOf("message" to e.message))
                }
                call.response.status(HttpStatusCode.OK)
            }

            post("/game/{lobbyid}/adventurevote") {
                try {
                    findLobby(call.code)?.apply {
                        if (this.hasWinner) {
                            call.response.status(HttpStatusCode.ExpectationFailed)
                            return@post call.respond(hashMapOf("message" to "This lobby has a winner"))
                        }

                    }?.voteOnAdventure(SingleVote(getUsernameFromToken(call), call.receive<SingleVote>().uservote))
                        ?: call.apply {
                            this.response.status(HttpStatusCode.NotFound)
                        }.respond(hashMapOf("message" to "Lobby not Found!"))


                } catch (e: IllegalArgumentException) {
                    call.apply {
                        this.response.status(HttpStatusCode.Conflict)
                    }.respond(hashMapOf("message" to e.message))
                }
                call.response.status(HttpStatusCode.OK)
            }

            post("/game/{lobbyid}/assassin") {
                val lobby = findLobby(call.code) ?: return@post call.apply {
                    this.response.status(HttpStatusCode.NotFound)
                }.respond(hashMapOf("message" to "Lobby not Found!"))

                if (lobby.info.winner == WINNER.EVIL)
                    return@post

                val guess = call.receive<AssassinGuess>()

                if (lobby.userRoles.firstOrNull { getUsernameFromToken(call) == it.userName }!!.role != ROLE.ASSASSIN)
                    return@post call.apply {
                        this.response.status(HttpStatusCode.Conflict)
                    }.respond(hashMapOf("message" to "You are not the assassin"))

                if (guess.guess == lobby.userRoles.firstOrNull { it.role == ROLE.MERLIN }!!.userName) {
                    lobby.info.winner = WINNER.EVIL
                } else {
                    lobby.info.winner = WINNER.GOOD
                }

                lobby.info.assassinHasGuessed = true

                Singletons.Lobbies.replaceOne(Lobby::lobbyCode eq lobby.lobbyCode,lobby)
                call.response.status(HttpStatusCode.OK)
            }
        }
    }
}
