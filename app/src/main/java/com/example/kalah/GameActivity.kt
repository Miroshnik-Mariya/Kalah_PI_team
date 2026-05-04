package com.example.kalah

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(android.R.layout.simple_list_item_1)

        val player1Name = intent.getStringExtra("PLAYER1_NAME") ?: "Player 1"
        val player2Name = intent.getStringExtra("PLAYER2_NAME") ?: "Player 2"
        val pitsPerPlayer = intent.getIntExtra("PITS_PER_PLAYER", 6)
        val stonesPerPit = intent.getIntExtra("STONES_PER_PIT", 4)
        val aiDifficulty = intent.getIntExtra("AI_DIFFICULTY", 1)
        val isVsAI = intent.getStringExtra("GAME_MODE") == "VS_AI"

        Toast.makeText(
            this,
            "$player1Name vs $player2Name\nЛунок: $pitsPerPlayer\nКамней: $stonesPerPit\nСложность AI: $aiDifficulty\nРежим: ${if (isVsAI) "Против AI" else "Два игрока"}",
            Toast.LENGTH_LONG
        ).show()

        // Закрываем Activity через 3 секунды
        finish()
    }
}