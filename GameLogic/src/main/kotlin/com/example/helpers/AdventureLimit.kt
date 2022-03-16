package com.example.helpers

data class AdventureLimit(
    val valid: Boolean = true,
    val limits: List<Int> = listOf(),
    val failsRequiredOnFourth:Int = 1,

)
