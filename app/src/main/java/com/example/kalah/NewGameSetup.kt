package com.example.kalah

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class NewGameSetup : AppCompatActivity() {

    private var pitsCount = 6      // от 6 до 8
    private var stonesCount = 4    // от 4 до 6
    private var aiDifficulty = 1   // от 1 до 3
    private var isVsAI = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_game_setup)

        // Получаем данные от предыдущего экрана
        val player1Name = intent.getStringExtra("PLAYER1_NAME") ?: "Player 1"
        val player1Avatar = intent.getIntExtra("PLAYER1_AVATAR", R.drawable.avatar1)
        val player2Name = intent.getStringExtra("PLAYER2_NAME") ?: "Player 2"
        val player2Avatar = intent.getIntExtra("PLAYER2_AVATAR", R.drawable.avatar2)
        isVsAI = intent.getStringExtra("GAME_MODE") == "VS_AI"

        // Элементы управления
        val tvLunkiValue: EditText = findViewById(R.id.tvLunkiValue)
        val tvStoneValue: EditText = findViewById(R.id.tvStoneValue)
        val tvAIDifficulty: EditText = findViewById(R.id.ai_level)
        val btnStartGame: ImageButton = findViewById(R.id.btnStartGame)

        // Элементы сложности AI
        val textDifficulty: TextView = findViewById(R.id.text_difficulty)

        // Устанавливаем начальные значения
        tvLunkiValue.setText(pitsCount.toString())
        tvStoneValue.setText(stonesCount.toString())
        tvAIDifficulty.setText(aiDifficulty.toString())

        // Показываем блок сложности только для режима с AI
        if (isVsAI) {
            textDifficulty.visibility = View.VISIBLE
            tvAIDifficulty.visibility = View.VISIBLE
        }

        // Обработчик для лунок (диапазон от 6 до 8)
        tvLunkiValue.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val value = tvLunkiValue.text.toString().toIntOrNull()
                pitsCount = when {
                    value == null -> 6
                    value < 6 -> {
                        Toast.makeText(this, "Количество лунок должно быть от 6 до 8, установлено 6", Toast.LENGTH_SHORT).show()
                        6
                    }
                    value > 8 -> {
                        Toast.makeText(this, "Количество лунок должно быть от 6 до 8, установлено 8", Toast.LENGTH_SHORT).show()
                        8
                    }
                    else -> value
                }
                tvLunkiValue.setText(pitsCount.toString())
            }
        }

        // Обработчик для камней (диапазон от 4 до 6)
        tvStoneValue.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val value = tvStoneValue.text.toString().toIntOrNull()
                stonesCount = when {
                    value == null -> 4
                    value < 4 -> {
                        Toast.makeText(this, "Количество камней должно быть от 4 до 6, установлено 4", Toast.LENGTH_SHORT).show()
                        4
                    }
                    value > 6 -> {
                        Toast.makeText(this, "Количество камней должно быть от 4 до 6, установлено 6", Toast.LENGTH_SHORT).show()
                        6
                    }
                    else -> value
                }
                tvStoneValue.setText(stonesCount.toString())
            }
        }

        // Обработчик для сложности AI (диапазон от 1 до 3)
        tvAIDifficulty.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && isVsAI) {
                val value = tvAIDifficulty.text.toString().toIntOrNull()
                aiDifficulty = when {
                    value == null -> 1
                    value < 1 -> {
                        Toast.makeText(this, "Уровень сложности должен быть от 1 до 3, установлен 1", Toast.LENGTH_SHORT).show()
                        1
                    }
                    value > 3 -> {
                        Toast.makeText(this, "Уровень сложности должен быть от 1 до 3, установлен 3", Toast.LENGTH_SHORT).show()
                        3
                    }
                    else -> value
                }
                tvAIDifficulty.setText(aiDifficulty.toString())
            }
        }

        // Кнопка "Начать игру"
        btnStartGame.setOnClickListener {
            // Получаем значения из полей
            pitsCount = tvLunkiValue.text.toString().toIntOrNull() ?: 6
            stonesCount = tvStoneValue.text.toString().toIntOrNull() ?: 4
            aiDifficulty = tvAIDifficulty.text.toString().toIntOrNull() ?: 1

            // Проверка диапазонов
            var isValid = true

            if (pitsCount !in 6..8) {
                Toast.makeText(this, "Количество лунок должно быть от 6 до 8", Toast.LENGTH_SHORT).show()
                isValid = false
            }

            if (stonesCount !in 4..6) {
                Toast.makeText(this, "Количество камней должно быть от 4 до 6", Toast.LENGTH_SHORT).show()
                isValid = false
            }

            if (isVsAI && aiDifficulty !in 1..3) {
                Toast.makeText(this, "Уровень сложности должен быть от 1 до 3", Toast.LENGTH_SHORT).show()
                isValid = false
            }

            if (!isValid) {
                return@setOnClickListener
            }

            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("PLAYER1_NAME", player1Name)
            intent.putExtra("PLAYER1_AVATAR", player1Avatar)
            intent.putExtra("PLAYER2_NAME", player2Name)
            intent.putExtra("PLAYER2_AVATAR", player2Avatar)
            intent.putExtra("GAME_MODE", if (isVsAI) "VS_AI" else "TWO_PLAYERS")
            intent.putExtra("PITS_PER_PLAYER", pitsCount)
            intent.putExtra("STONES_PER_PIT", stonesCount)
            intent.putExtra("AI_DIFFICULTY", aiDifficulty)
            startActivity(intent)
            finish()
        }
    }
}