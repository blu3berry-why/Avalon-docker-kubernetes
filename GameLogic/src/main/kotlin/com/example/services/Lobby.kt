package com.example.services


import com.example.model.db.Lobby
import com.example.model.enums.ROLE
import com.example.model.enums.SCORE
import com.example.model.enums.WINNER
import com.example.model.network.Info
import com.example.model.network.RoundVote
import com.example.model.network.Settings
import com.example.model.network.SingleVote
import com.example.singleton.Singletons
import org.litote.kmongo.eq

fun createLobby(): Lobby {
    return Lobby(
        lobbyCode = sixCharStr(),
        info = Info(
            started = false,
            scores = mutableListOf(SCORE.UNDECIDED, SCORE.UNDECIDED, SCORE.UNDECIDED, SCORE.UNDECIDED, SCORE.UNDECIDED),
            currentRound = 0,
            isAdventure = false,
            currentAdventure = 0,
            king = null,
            selectedForAdventure = mutableListOf(),
            playersName = mutableListOf(),
        ),
        settings = Settings(
            assassin = false,
            mordred = false,
            morgana = false,
            oberon = false,
            percival = false,
            arnold = false,
        ),
        votes = mutableListOf(),
        userRoles = mutableListOf(),
        adventureVotes = mutableListOf(),
    )
}

suspend fun findLobby(lobbyCode: String): Lobby? {
    return Singletons.Lobbies.findOne(Lobby::lobbyCode eq lobbyCode)
}

suspend fun Lobby.start() {
    if (this.info.started)
        throw IllegalArgumentException("Lobby has already started")

    if (this.info.playersName.size < 5)
        throw IllegalArgumentException("Too few players")

    if (this.info.playersName.size > 10)
        throw IllegalArgumentException("Too many players")

    this.info.started = true
    // might should be randomised
    this.info.king = this.info.playersName[0]
    this.info.currentRound = 1
    // round 0 is just a placeholder
    this.votes.add(RoundVote("", mutableListOf(), mutableListOf()))
    this.votes.add(RoundVote(this.info.king!!, mutableListOf(), mutableListOf()))
    this.adventureVotes.add(RoundVote("", mutableListOf(), mutableListOf()))
    this.info.playerSelectNum = Singletons.const.adventureLimit[this.info.playersName.size].limits[info.currentAdventure+1]
    this.randomizeRoles()
}

suspend fun Lobby.select(chosen: List<String>) {
    if (chosen.size != Singletons.const.adventureLimit[this.info.playersName.size].limits[this.info.currentRound])
        throw IllegalArgumentException("This is not the required amount of people! Required: ${Singletons.const.adventureLimit[this.info.playersName.size].limits[this.info.currentRound]}, but found : ${chosen.size}!")

    if (this.info.selectedForAdventure.isNotEmpty())
        throw IllegalArgumentException("The king has already chosen")

    chosen.forEach {
        this.votes[this.info.currentRound].choosen.add(it)
    }

    this.info.selectedForAdventure = this.votes[this.info.currentRound].choosen

    Singletons.Lobbies.replaceOne(Lobby::lobbyCode eq this.lobbyCode,this)
}

suspend fun Lobby.vote(vote: SingleVote) {
    val results = this.votes[this.info.currentRound].results
    results.firstOrNull {
        it.username == vote.username
    } ?: results.add(vote)

    if (results.size == this.info.playersName.size) {
        if (results.filter { it.uservote }.size > (this.info.playersName.size / 2)) {
            this.startAdventure()
            this.info.failCounter = 0
        } else {
            this.info.failCounter++
            if (this.info.failCounter == 5) {
                this.info.winner = WINNER.EVIL
            }
            this.nextRound()
        }
    }
    Singletons.Lobbies.replaceOne(Lobby::lobbyCode eq this.lobbyCode,this)
}

suspend fun Lobby.nextRound() {
    this.info.currentRound++
    this.info.isAdventure = false
    info.selectedForAdventure.removeAll {true}
    this.info.playerSelectNum = Singletons.const.adventureLimit[this.info.playersName.size].limits[info.currentAdventure+1]
    this.votes.add(RoundVote(this.nextKing(), mutableListOf(), mutableListOf()))
    Singletons.Lobbies.replaceOne(Lobby::lobbyCode eq this.lobbyCode,this)
}

suspend fun Lobby.startAdventure() {
    this.info.isAdventure = true
    this.info.currentAdventure++
    this.info.selectedForAdventure = this.votes[this.info.currentRound].choosen
    this.adventureVotes.add(RoundVote(this.info.king!!, this.votes[this.info.currentRound].choosen, mutableListOf()))

    Singletons.Lobbies.replaceOne(Lobby::lobbyCode eq this.lobbyCode,this)
}

suspend fun Lobby.nextKing(): String {
    var idx = this.info.playersName.indexOf(this.info.playersName.first {
        this.info.king == it
    }) + 1

    if (idx == this.info.playersName.size)
        idx = 0

    this.info.king = this.info.playersName[idx]

    Singletons.Lobbies.replaceOne(Lobby::lobbyCode eq this.lobbyCode,this)
    return this.info.king!!
}

suspend fun Lobby.voteOnAdventure(vote: SingleVote) {
    if (!this.info.isAdventure)
        throw IllegalArgumentException("There is no adventure going on")

    val round = this.adventureVotes[this.info.currentAdventure]

    if (round.choosen.firstOrNull { vote.username == it } == null)
        throw IllegalArgumentException("You are not chosen")

    if (round.results.firstOrNull { vote.username == it.username } != null)
        throw IllegalArgumentException("You already voted")

    round.results.add(vote)

    var limit = 1

    if (this.info.currentAdventure == 4) {
        limit = Singletons.const.adventureLimit[this.info.playersName.size].failsRequiredOnFourth
    }

    if (round.results.size == round.choosen.size) {
        if (round.results.filter { !it.uservote }.size >= limit) {
            this.info.scores.add((this.info.currentAdventure - 1), SCORE.EVIL)
        } else {
            this.info.scores.add((this.info.currentAdventure - 1), SCORE.GOOD)
        }

        if (this.info.scores.filter { it == SCORE.EVIL }.size >= 3)
            this.info.winner = WINNER.EVIL

        if (this.info.scores.filter { it == SCORE.GOOD }.size >= 3)
            this.info.winner = WINNER.GOOD

        nextRound()
    }
    Singletons.Lobbies.replaceOne(Lobby::lobbyCode eq this.lobbyCode,this)
}

val Lobby.hasWinner: Boolean
    get() {
        if (this.info.winner != WINNER.NOT_DECIDED) {
            return true
        }
        return false
    }

fun Lobby.sees(role: ROLE, username: String): List<String> {
    if (role == ROLE.MERLIN) {
        return this.userRoles.filter {
            (it.role.isEvil() && it.role != ROLE.MORDRED)
        }.map { it.userName }.toList()
    }

    if (role == ROLE.OBERON)
        return this.userRoles.filter {
            (it.role.isEvil() && it.role != ROLE.OBERON && it.userName != username)
        }.map { it.userName }.toList()

    if (role.isEvil())
        return this.userRoles.filter {
            (it.role.isEvil() && it.role != ROLE.OBERON && it.userName != username)
        }.map { it.userName }.toList()

    if (role == ROLE.PERCIVAL)
        return this.userRoles.filter {
            (it.role == ROLE.MERLIN || it.role == ROLE.MORGANA)
        }.map { it.userName }.toList()


    return listOf()


}