package com.example.model.db

import com.example.model.network.Info
import com.example.model.network.Settings
import com.example.model.network.RoundVote
import kotlinx.serialization.Serializable

@Serializable
data class Lobby(
    val lobbyCode: String,
    val info: Info,
    var settings: Settings,
    val votes: MutableList<RoundVote>,
    val adventureVotes: MutableList<RoundVote>,
    var userRoles: MutableList<UserRoleMap>
)
