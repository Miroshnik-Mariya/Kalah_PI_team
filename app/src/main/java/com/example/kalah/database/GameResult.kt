package com.example.kalah.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_results")
data class GameResult(
    val player1Name: String,
    val player2Name: String,
    val winnerName: String,
    val player1Score: Int,
    val player2Score: Int,
    val pitsPerPlayer: Int,
    val stonesPerPit: Int,
    val gameMode: String // "TWO_PLAYERS" или "VS_AI"
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    var timestamp: Long = System.currentTimeMillis()
}