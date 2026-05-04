package com.example.kalah

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class GameSetup : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(android.R.layout.simple_list_item_1) // Временный layout

        val player1Name = intent.getStringExtra("PLAYER1_NAME") ?: "Player 1"
        val player2Name = intent.getStringExtra("PLAYER2_NAME") ?: "Player 2"
        val gameMode = intent.getStringExtra("GAME_MODE") ?: "TWO_PLAYERS"

        Toast.makeText(
            this,
            "$player1Name vs $player2Name\nРежим: ${if (gameMode == "VS_AI") "Против AI" else "Два игрока"}",
            Toast.LENGTH_LONG
        ).show()
    }
}