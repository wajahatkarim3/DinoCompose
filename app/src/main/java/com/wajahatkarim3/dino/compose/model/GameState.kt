package com.wajahatkarim3.dino.compose.model

data class GameState(
    var currentScore: Int = 0,
    var highScore: Int = 0,
    var isGameOver: Boolean = false
) {
    fun increaseScore() {
        currentScore = currentScore.inc()
    }
}