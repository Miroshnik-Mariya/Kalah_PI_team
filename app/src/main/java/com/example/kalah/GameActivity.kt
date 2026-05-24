package com.example.kalah

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.kalah.database.AppDatabase
import com.example.kalah.database.GameResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GameActivity : AppCompatActivity() {

    private lateinit var gameLogic: GameLogic
    private lateinit var topPitsContainer: LinearLayout
    private lateinit var bottomPitsContainer: LinearLayout
    private lateinit var tvPlayer1Info: TextView
    private lateinit var tvPlayer2Info: TextView
    private lateinit var tvTurn: TextView
    private lateinit var tvKalahLeft: TextView
    private lateinit var tvKalahRight: TextView
    private lateinit var tvKalahLeftPlayerName: TextView
    private lateinit var tvKalahRightPlayerName: TextView
    private lateinit var ivPlayer1Avatar: ImageView
    private lateinit var ivPlayer2Avatar: ImageView
    private lateinit var btnMenu: Button
    private lateinit var btnReset: Button

    private var player1Name = ""
    private var player2Name = ""
    private var player1Avatar = 0
    private var player2Avatar = 0
    private var pitsPerPlayer = 6
    private var stonesPerPit = 4
    private var isVsAI = false
    private var aiDifficulty = 1

    private val aiHandler = Handler(Looper.getMainLooper())
    private var isAIThinking = false
    private val pitViews = mutableMapOf<Int, View>()
    private val pitCounters = mutableMapOf<Int, TextView>()
    private var currentPitStyle = R.drawable.pit_wood_background

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Настройка обработки кнопки "Назад" через OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitDialog()
            }
        })

        try {
            // Получаем данные
            player1Name = intent.getStringExtra("PLAYER1_NAME") ?: "Игрок 1"
            player2Name = intent.getStringExtra("PLAYER2_NAME") ?: "Игрок 2"
            player1Avatar = intent.getIntExtra("PLAYER1_AVATAR", R.drawable.avatar1)
            player2Avatar = intent.getIntExtra("PLAYER2_AVATAR", R.drawable.avatar2)
            pitsPerPlayer = intent.getIntExtra("PITS_PER_PLAYER", 6)
            stonesPerPit = intent.getIntExtra("STONES_PER_PIT", 4)
            isVsAI = intent.getStringExtra("GAME_MODE") == "VS_AI"
            aiDifficulty = intent.getIntExtra("AI_DIFFICULTY", 1)

            // Инициализация UI
            topPitsContainer = findViewById(R.id.topPitsContainer)
            bottomPitsContainer = findViewById(R.id.bottomPitsContainer)
            tvPlayer1Info = findViewById(R.id.tvPlayer1Info)
            tvPlayer2Info = findViewById(R.id.tvPlayer2Info)
            tvTurn = findViewById(R.id.tvTurn)
            tvKalahLeft = findViewById(R.id.tvKalahLeft)
            tvKalahRight = findViewById(R.id.tvKalahRight)
            tvKalahLeftPlayerName = findViewById(R.id.tvKalahLeftPlayerName)
            tvKalahRightPlayerName = findViewById(R.id.tvKalahRightPlayerName)
            ivPlayer1Avatar = findViewById(R.id.ivPlayer1Avatar)
            ivPlayer2Avatar = findViewById(R.id.ivPlayer2Avatar)
            btnMenu = findViewById(R.id.btnMenu)
            btnReset = findViewById(R.id.btnReset)

            // Устанавливаем аватары
            if (player1Avatar != 0) ivPlayer1Avatar.setImageResource(player1Avatar)
            if (player2Avatar != 0) ivPlayer2Avatar.setImageResource(player2Avatar)

            // Устанавливаем имена
            tvPlayer1Info.text = "$player1Name\n0"
            tvPlayer2Info.text = "$player2Name\n0"

            // Устанавливаем имена на калахах
            tvKalahLeftPlayerName.text = player2Name  // Левый калах - игрок 2
            tvKalahRightPlayerName.text = player1Name // Правый калах - игрок 1

            // Создаём игровую логику
            gameLogic = GameLogic(pitsPerPlayer, stonesPerPit, isVsAI, aiDifficulty)

            // Создаём поле
            createBoard()

            // Обновляем UI
            updateUI()

            // Если первый ход у AI
            if (isVsAI && gameLogic.getCurrentPlayer() == 1) {
                makeAIMove()
            }

            // Кнопка меню
            btnMenu.setOnClickListener {
                showExitDialog()
            }

            // Кнопка сброса
            btnReset.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Сброс игры")
                    .setMessage("Вы уверены, что хотите начать игру заново?")
                    .setPositiveButton("Да") { _, _ ->
                        resetGame()
                    }
                    .setNegativeButton("Нет", null)
                    .show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setTitle("Выход из игры")
            .setMessage("Вы действительно хотите выйти в главное меню?")
            .setPositiveButton("Да") { _, _ ->
                finish()
            }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun createBoard() {
        topPitsContainer.removeAllViews()
        bottomPitsContainer.removeAllViews()
        pitViews.clear()
        pitCounters.clear()

        // Применяем стиль оформления
        applyBoardStyle()

        // ВЕРХНИЕ ЛУНКИ (игрок 2/AI)
        for (i in (pitsPerPlayer - 1) downTo 0) {
            val pitIndex = pitsPerPlayer + 1 + i
            val pitView = createPitView(pitIndex, gameLogic.getStonesInPit(pitIndex))
            pitView.setOnClickListener { onPitClick(pitIndex) }

            // Фиксированные размеры для лунки
            val params = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.pit_width),
                resources.getDimensionPixelSize(R.dimen.pit_height)
            )
            params.setMargins(6, 6, 6, 6)
            pitView.layoutParams = params

            topPitsContainer.addView(pitView)
            pitViews[pitIndex] = pitView
        }

        // НИЖНИЕ ЛУНКИ (игрок 1)
        for (i in 0 until pitsPerPlayer) {
            val pitIndex = i
            val pitView = createPitView(pitIndex, gameLogic.getStonesInPit(pitIndex))
            pitView.setOnClickListener { onPitClick(pitIndex) }

            // Фиксированные размеры для лунки
            val params = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.pit_width),
                resources.getDimensionPixelSize(R.dimen.pit_height)
            )
            params.setMargins(6, 6, 6, 6)
            pitView.layoutParams = params

            bottomPitsContainer.addView(pitView)
            pitViews[pitIndex] = pitView
        }
    }

    private fun createPitView(pitIndex: Int, stones: Int): View {
        val pitView = LayoutInflater.from(this).inflate(R.layout.item_pit, null)

        val stoneCount = pitView.findViewById<TextView>(R.id.stoneCount)
        val stonesContainer = pitView.findViewById<LinearLayout>(R.id.stonesContainer)

        stoneCount.text = stones.toString()
        pitCounters[pitIndex] = stoneCount

        // Визуализация камней
        stonesContainer?.removeAllViews()
        val maxVisible = minOf(stones, 4)
        for (j in 0 until maxVisible) {
            val stone = ImageView(this)
            stone.setImageResource(R.drawable.ic_stone)
            val stoneParams = LinearLayout.LayoutParams(25, 25)
            stoneParams.setMargins(2, 2, 2, 2)
            stone.layoutParams = stoneParams
            stonesContainer?.addView(stone)
        }

        return pitView
    }

    private fun onPitClick(pitIndex: Int) {
        if (isAIThinking) {
            Toast.makeText(this, "Подождите, AI думает...", Toast.LENGTH_SHORT).show()
            return
        }

        if (!gameLogic.canMove(pitIndex)) {
            Toast.makeText(this, "Нельзя сходить из этой лунки!", Toast.LENGTH_SHORT).show()
            return
        }

        makeMove(pitIndex)
    }

    private fun makeMove(pitIndex: Int) {
        val result = gameLogic.makeMove(pitIndex)

        when (result) {
            is MoveResult.Invalid -> {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
            }

            is MoveResult.Success -> {
                updateUI()
                if (result.captured > 0) {
                    Toast.makeText(this, "Захвачено ${result.captured} камней", Toast.LENGTH_SHORT).show()
                }

                gameLogic.switchPlayer()
                updateUITurn()

                if (gameLogic.isGameOver()) {
                    endGame()
                    return
                }

                if (isVsAI && gameLogic.getCurrentPlayer() == 1 && !gameLogic.isGameOver()) {
                    makeAIMove()
                }
            }

            is MoveResult.ExtraTurn -> {
                updateUI()
                Toast.makeText(this, "Дополнительный ход", Toast.LENGTH_LONG).show()
                updateUITurn()

                if (gameLogic.isGameOver()) {
                    endGame()
                    return
                }

                if (isVsAI && gameLogic.getCurrentPlayer() == 1 && !gameLogic.isGameOver()) {
                    makeAIMove()
                }
            }

            is MoveResult.GameOver -> {
                updateUI()
                endGame()
            }
        }
    }

    private fun makeAIMove() {
        if (!gameLogic.hasAvailableMoves()) {
            Toast.makeText(this, "У AI нет доступных ходов!", Toast.LENGTH_SHORT).show()
            gameLogic.switchPlayer()
            updateUITurn()
            return
        }

        isAIThinking = true
        tvTurn.text = "AI думает..."

        val delay = when (aiDifficulty) {
            1 -> 600L
            2 -> 1000L
            else -> 1400L
        }

        aiHandler.postDelayed({
            try {
                val aiMove = gameLogic.getAIMove()
                if (aiMove != -1) {
                    makeMove(aiMove)
                } else {
                    if (!gameLogic.isGameOver()) {
                        gameLogic.switchPlayer()
                        updateUITurn()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isAIThinking = false
            }
        }, delay)
    }

    private fun applyBoardStyle() {
        val boardStyle = SettingsManager.getBoardStyle(this)

        val boardContainer = findViewById<LinearLayout>(R.id.boardContainer)
        val kalahLeft = findViewById<LinearLayout>(R.id.kalahLeft)
        val kalahRight = findViewById<LinearLayout>(R.id.kalahRight)

        when (boardStyle) {
            "wood" -> {
                boardContainer?.setBackgroundResource(R.drawable.board_background_wood)
                kalahLeft?.setBackgroundResource(R.drawable.kalah_background_wood)
                kalahRight?.setBackgroundResource(R.drawable.kalah_background_wood)
                currentPitStyle = R.drawable.pit_wood_background
                applyPitStyle(currentPitStyle)
            }
            "metal" -> {
                boardContainer?.setBackgroundResource(R.drawable.board_background_metal)
                kalahLeft?.setBackgroundResource(R.drawable.kalah_background_metal)
                kalahRight?.setBackgroundResource(R.drawable.kalah_background_metal)
                currentPitStyle = R.drawable.pit_metal_background
                applyPitStyle(currentPitStyle)
            }
        }
    }

    private fun applyPitStyle(backgroundResId: Int) {
        pitViews.values.forEach { pitView ->
            pitView.setBackgroundResource(backgroundResId)
        }
    }

    private fun highlightAvailablePits() {
        val currentPlayer = gameLogic.getCurrentPlayer()
        val availableMoves = gameLogic.getAvailableMoves()

        // Сначала сбрасываем подсветку всех лунок
        pitViews.values.forEach { pitView ->
            pitView.alpha = 0.5f
            pitView.setBackgroundResource(currentPitStyle)
        }

        // Подсвечиваем только доступные лунки текущего игрока
        availableMoves.forEach { pitIndex ->
            val pitView = pitViews[pitIndex]
            pitView?.alpha = 1.0f
            pitView?.setBackgroundResource(R.drawable.pit_highlighted)
        }
    }

    private fun updateUI() {
        try {
            // Обновляем лунки
            pitCounters.forEach { (pitIndex, textView) ->
                textView.text = gameLogic.getStonesInPit(pitIndex).toString()

                val pitView = pitViews[pitIndex]
                val stonesContainer = pitView?.findViewById<LinearLayout>(R.id.stonesContainer)
                val stones = gameLogic.getStonesInPit(pitIndex)

                stonesContainer?.removeAllViews()
                val maxVisible = minOf(stones, 4)
                for (j in 0 until maxVisible) {
                    val stone = ImageView(this)
                    stone.setImageResource(R.drawable.ic_stone)
                    val stoneParams = LinearLayout.LayoutParams(25, 25)
                    stoneParams.setMargins(2, 2, 2, 2)
                    stone.layoutParams = stoneParams
                    stonesContainer?.addView(stone)
                }
            }

            // Отображение калахов: левый - игрок 2, правый - игрок 1
            tvKalahLeft.text = gameLogic.getPlayer2Score().toString()
            tvKalahRight.text = gameLogic.getPlayer1Score().toString()

            // Обновляем информацию об игроках
            tvPlayer1Info.text = "${player1Name}\n${gameLogic.getPlayer1Score()}"
            tvPlayer2Info.text = "${player2Name}\n${gameLogic.getPlayer2Score()}"

            updateUITurn()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateUITurn() {
        val currentPlayer = gameLogic.getCurrentPlayer()
        val turnText = if (currentPlayer == 0) player1Name else player2Name
        tvTurn.text = turnText

        tvPlayer1Info.alpha = if (currentPlayer == 0) 1.0f else 0.5f
        tvPlayer2Info.alpha = if (currentPlayer == 1) 1.0f else 0.5f

        // Подсвечиваем доступные лунки
        highlightAvailablePits()
    }

    private fun endGame() {
        try {
            val player1Score = gameLogic.getPlayer1Score()
            val player2Score = gameLogic.getPlayer2Score()
            val winnerName = when (gameLogic.getWinner()) {
                "PLAYER1" -> player1Name
                "PLAYER2" -> player2Name
                else -> "Ничья"
            }

            saveGameResult(winnerName, player1Score, player2Score)

            val message = when (gameLogic.getWinner()) {
                "PLAYER1" -> "$player1Name ПОБЕДИЛ!\n\nСчёт: $player1Score : $player2Score"
                "PLAYER2" -> "$player2Name ПОБЕДИЛ!\n\nСчёт: $player1Score : $player2Score"
                else -> "НИЧЬЯ! \n\nСчёт: $player1Score : $player2Score"
            }

            AlertDialog.Builder(this)
                .setTitle("🎲 ИГРА ОКОНЧЕНА 🎲")
                .setMessage(message)
                .setPositiveButton("Сыграть ещё") { _, _ ->
                    resetGame()
                }
                .setNegativeButton("Главное меню") { _, _ ->
                    finish()
                }
                .setCancelable(false)
                .show()
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }

    private fun saveGameResult(winnerName: String, player1Score: Int, player2Score: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = GameResult(
                    player1Name = player1Name,
                    player2Name = player2Name,
                    winnerName = winnerName,
                    player1Score = player1Score,
                    player2Score = player2Score,
                    pitsPerPlayer = pitsPerPlayer,
                    stonesPerPit = stonesPerPit,
                    gameMode = if (isVsAI) "VS_AI" else "TWO_PLAYERS"
                )
                AppDatabase.getInstance(this@GameActivity).gameResultDao().insert(result)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        aiHandler.removeCallbacksAndMessages(null)
    }

    private fun resetGame() {
        try {
            gameLogic.resetGame()

            topPitsContainer.removeAllViews()
            bottomPitsContainer.removeAllViews()
            pitViews.clear()
            pitCounters.clear()

            createBoard()
            updateUI()

            isAIThinking = false
            aiHandler.removeCallbacksAndMessages(null)

            if (isVsAI && gameLogic.getCurrentPlayer() == 1) {
                aiHandler.postDelayed({
                    makeAIMove()
                }, 500)
            }

            Toast.makeText(this, "Игра перезапущена!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка при перезапуске: ${e.message}", Toast.LENGTH_SHORT).show()
            recreate()
        }
    }

    private fun refreshBoard() {
        for (i in 0 until pitsPerPlayer) {
            val bottomPitIndex = i
            updatePitView(bottomPitIndex, gameLogic.getStonesInPit(bottomPitIndex))

            val topPitIndex = pitsPerPlayer + 1 + i
            updatePitView(topPitIndex, gameLogic.getStonesInPit(topPitIndex))
        }

        tvKalahLeft.text = gameLogic.getPlayer2Score().toString()
        tvKalahRight.text = gameLogic.getPlayer1Score().toString()
    }

    private fun updatePitView(pitIndex: Int, stones: Int) {
        val pitView = pitViews[pitIndex]
        if (pitView != null) {
            val stoneCount = pitView.findViewById<TextView>(R.id.stoneCount)
            val stonesContainer = pitView.findViewById<LinearLayout>(R.id.stonesContainer)

            stoneCount.text = stones.toString()

            stonesContainer?.removeAllViews()
            val maxVisible = minOf(stones, 4)
            for (j in 0 until maxVisible) {
                val stone = ImageView(this)
                stone.setImageResource(R.drawable.ic_stone)
                val stoneParams = LinearLayout.LayoutParams(25, 25)
                stoneParams.setMargins(2, 2, 2, 2)
                stone.layoutParams = stoneParams
                stonesContainer?.addView(stone)
            }
        }
    }
}