package com.example.model.network

import kotlinx.serialization.Serializable

@Serializable
data class RoundVote(
    val king:String,
    val choosen: MutableList<String>,
    val results: MutableList <SingleVote>,
)
