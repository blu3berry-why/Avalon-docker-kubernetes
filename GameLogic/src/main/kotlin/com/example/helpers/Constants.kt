package com.example.helpers

class Constants {
    val playerBalance = listOf<Balance>(
        // # 0 players
        Balance(valid = false),
        // # 1 players
        Balance(valid = false),
        // # 2 players
        Balance(valid = false),
        // # 3 players
        Balance(valid = false),
        // # 4 players
        Balance(valid = false),
        // # 5 players
        Balance(evil = 2, good = 3),
        // # 6 players
        Balance(evil = 2, good = 4),
        // # 7 players
        Balance(evil = 3, good = 4),
        // # 8 players
        Balance(evil = 3, good = 5),
        // # 9 players
        Balance(evil = 3, good = 6),
        // # 10 players
        Balance(evil = 4, good = 6),)

    val adventureLimit = listOf<AdventureLimit>(
        // # 0 players
        AdventureLimit(valid = false),
        // # 1 players
        AdventureLimit(valid = false),
        // # 2 players
        AdventureLimit(valid = false),
        // # 3 players
        AdventureLimit(valid = false),
        // # 4 players
        AdventureLimit(valid = false),
        // # 5 players
        AdventureLimit(limits = listOf(
            -1, 2, 3, 2, 3, 3
        )),
        // # 6 players
        AdventureLimit(limits = listOf(
            -1, 2, 3, 4, 3, 4
        )),
        // # 7 players
        AdventureLimit(limits = listOf(
            -1, 2, 3, 3, 4, 4
        ),
            failsRequiredOnFourth = 2
        ),
        // # 8 players
        AdventureLimit(limits = listOf(
            -1, 3, 4, 4, 5, 5
        ),
            failsRequiredOnFourth = 2),
        // # 9 players
        AdventureLimit(limits = listOf(
            -1, 3, 4, 4, 5, 5
        ),
            failsRequiredOnFourth = 2),
        // # 10 players
        AdventureLimit(limits = listOf(
            -1, 3, 4, 4, 5, 5
        ),
            failsRequiredOnFourth = 2)
    )

}