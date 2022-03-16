package com.example.services

import com.example.model.db.Lobby
import com.example.model.db.UserRoleMap
import com.example.model.enums.ROLE
import com.example.singleton.Singletons
import org.litote.kmongo.eq
import kotlin.random.Random

fun sixCharStr(): String{
    val characters= "abcdefghijklmnopqrstuvwxyz0123456789"
    var result = ""
    for(i in 0 until 6){
        result += characters[Random.nextInt(0,36)]
    }
    return result
}

suspend fun Lobby.randomizeRoles(){
    val roles = mutableListOf<ROLE>()
    val numOfEvil = Singletons.const.playerBalance[this.info.playersName.size].evil

    val needMoreEvil = fun(): Boolean{
        return roles.size < numOfEvil
    }

    if(this.settings.assassin && needMoreEvil())
        roles.add(ROLE.ASSASSIN)

    if (this.settings.morgana && needMoreEvil())
        roles.add(ROLE.MORGANA)

    if (this.settings.mordred && needMoreEvil())
        roles.add(ROLE.MORDRED)

    if (this.settings.oberon && needMoreEvil())
        roles.add(ROLE.OBERON)

    while (needMoreEvil())
        roles.add(ROLE.MINION_OF_MORDRED)

    val needMoreGood= fun(): Boolean{
        return (roles.size - numOfEvil) < Singletons.const.playerBalance[this.info.playersName.size].good
    }

    roles.add(ROLE.MERLIN)

    if(this.settings.percival && needMoreGood())
        roles.add(ROLE.PERCIVAL)

    if (this.settings.arnold && needMoreGood())
        roles.add(ROLE.ARNOLD)

    while (needMoreGood())
        roles.add(ROLE.SERVANT_OF_ARTHUR)

    roles.shuffle()

    if (roles.size != this.info.playersName.size)
        throw IllegalArgumentException("Randomize.kt : 58, roles.size != players.size")

    for (i in 0 until roles.size){
        this.userRoles.add(UserRoleMap(this.info.playersName[i], roles[i]))
    }

    Singletons.Lobbies.replaceOne(Lobby::lobbyCode eq this.lobbyCode,this)
}