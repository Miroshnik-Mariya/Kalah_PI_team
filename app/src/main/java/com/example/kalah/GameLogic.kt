package com.example.kalah

import kotlin.random.Random

class GameLogic(
    private val pitsPerPlayer: Int,
    private val stonesPerPit: Int,
    private val isVsAI: Boolean = false,
    private val aiDifficulty: Int = 1 // 1, 2 или 3
) {
    private val totalPits = pitsPerPlayer * 2 + 2
    private val board = IntArray(totalPits)
    private var currentPlayer = 0 // 0 - игрок 1 (нижний ряд), 1 - игрок 2 (верхний ряд) или AI
    private var gameIsOver = false

    // Индексы калахов
    private val player1Kalah = pitsPerPlayer
    private val player2Kalah = totalPits - 1

    init {
        resetGame()
    }

    fun resetGame() {
        for (i in 0 until totalPits) {
            board[i] = when (i) {
                player1Kalah, player2Kalah -> 0
                else -> stonesPerPit
            }
        }
        currentPlayer = 0
        gameIsOver = false
    }

    // Получить количество камней в лунке
    fun getStonesInPit(pitIndex: Int): Int = board[pitIndex]

    // Получить количество камней в калахе игрока
    fun getKalahStones(player: Int): Int =
        if (player == 0) board[player1Kalah] else board[player2Kalah]

    // Проверка, может ли игрок сходить из этой лунки
    fun canMove(pitIndex: Int): Boolean {
        if (gameIsOver) return false

        return if (currentPlayer == 0) {
            // Игрок 1: лунки от 0 до pitsPerPlayer-1
            pitIndex in 0 until pitsPerPlayer && board[pitIndex] > 0
        } else {
            // Игрок 2: лунки от pitsPerPlayer+1 до totalPits-2
            pitIndex in (pitsPerPlayer + 1) until (totalPits - 1) && board[pitIndex] > 0
        }
    }

    // Выполнить ход
    fun makeMove(pitIndex: Int): MoveResult {
        if (!canMove(pitIndex)) {
            return MoveResult.Invalid("Нельзя сходить из этой лунки!")
        }

        var stones = board[pitIndex]
        board[pitIndex] = 0
        var currentIdx = pitIndex
        var lastPitWasKalah = false

        // Разбрасываем камни
        while (stones > 0) {
            currentIdx = (currentIdx + 1) % totalPits

            // Пропускаем калах противника
            when {
                currentPlayer == 0 && currentIdx == player2Kalah -> continue
                currentPlayer == 1 && currentIdx == player1Kalah -> continue
            }

            board[currentIdx]++
            stones--
        }

        // Проверка на попадание в свой калах (дополнительный ход)
        val extraTurn = when {
            currentPlayer == 0 && currentIdx == player1Kalah -> {
                lastPitWasKalah = true
                true
            }
            currentPlayer == 1 && currentIdx == player2Kalah -> {
                lastPitWasKalah = true
                true
            }
            else -> false
        }

        // Проверка на захват
        var captured = 0
        if (!extraTurn && board[currentIdx] == 1 && isOwnPit(currentIdx)) {
            val oppositeIdx = getOppositePit(currentIdx)
            val oppositeStones = board[oppositeIdx]

            if (oppositeStones > 0) {
                captured = oppositeStones + 1
                board[currentIdx] = 0
                board[oppositeIdx] = 0

                if (currentPlayer == 0) {
                    board[player1Kalah] += captured
                } else {
                    board[player2Kalah] += captured
                }
            }
        }

        // Проверка окончания игры
        checkGameOver()

        return if (gameIsOver) {
            MoveResult.GameOver(getWinner(), getPlayer1Score(), getPlayer2Score())
        } else if (extraTurn) {
            MoveResult.ExtraTurn(currentPlayer)
        } else {
            MoveResult.Success(currentPlayer, captured)
        }
    }

    // Переключить игрока
    fun switchPlayer() {
        currentPlayer = 1 - currentPlayer
    }

    // Проверка, принадлежит ли лунка текущему игроку
    private fun isOwnPit(pitIndex: Int): Boolean {
        return if (currentPlayer == 0) {
            pitIndex < pitsPerPlayer
        } else {
            pitIndex > pitsPerPlayer && pitIndex < totalPits - 1
        }
    }

    // Получить противоположную лунку
    private fun getOppositePit(pitIndex: Int): Int {
        if (pitIndex < pitsPerPlayer) {
            // Верхняя лунка -> нижняя напротив
            return (totalPits - 2) - pitIndex
        } else {
            // Нижняя лунка -> верхняя напротив
            return (totalPits - 2) - pitIndex
        }
    }

    // Проверка окончания игры
    private fun checkGameOver() {
        var player1Empty = true
        var player2Empty = true

        // Проверяем, пусты ли все лунки игрока 1
        for (i in 0 until pitsPerPlayer) {
            if (board[i] > 0) {
                player1Empty = false
                break
            }
        }

        // Проверяем, пусты ли все лунки игрока 2
        for (i in (pitsPerPlayer + 1) until (totalPits - 1)) {
            if (board[i] > 0) {
                player2Empty = false
                break
            }
        }

        if (player1Empty || player2Empty) {
            gameIsOver = true

            // Собираем оставшиеся камни в калахи
            if (player1Empty) {
                for (i in (pitsPerPlayer + 1) until (totalPits - 1)) {
                    board[player2Kalah] += board[i]
                    board[i] = 0
                }
            }
            if (player2Empty) {
                for (i in 0 until pitsPerPlayer) {
                    board[player1Kalah] += board[i]
                    board[i] = 0
                }
            }
        }
    }

    fun isGameOver(): Boolean = gameIsOver

    fun getWinner(): String {
        return when {
            board[player1Kalah] > board[player2Kalah] -> "PLAYER1"
            board[player2Kalah] > board[player1Kalah] -> "PLAYER2"
            else -> "TIE"
        }
    }

    fun getPlayer1Score(): Int = board[player1Kalah]
    fun getPlayer2Score(): Int = board[player2Kalah]

    fun getCurrentPlayer(): Int = currentPlayer

    // ========== AI ЛОГИКА ==========

    // Получить ход AI (возвращает индекс лунки)
    fun getAIMove(): Int {
        if (!isVsAI || currentPlayer != 1) return -1

        return when (aiDifficulty) {
            1 -> getRandomAIMove()
            2 -> getMediumAIMove()
            3 -> getHardAIMove()
            else -> getRandomAIMove()
        }
    }

    // Лёгкий уровень: случайный ход среди доступных лунок
    private fun getRandomAIMove(): Int {
        val availablePits = mutableListOf<Int>()
        for (i in (pitsPerPlayer + 1) until (totalPits - 1)) {
            if (board[i] > 0) {
                availablePits.add(i)
            }
        }
        return if (availablePits.isNotEmpty()) {
            availablePits[Random.nextInt(availablePits.size)]
        } else {
            -1
        }
    }

    // Средний уровень: предпочитает ходы, дающие дополнительный ход или захват
    private fun getMediumAIMove(): Int {
        var bestPit = -1
        var bestScore = -1

        for (pit in (pitsPerPlayer + 1) until (totalPits - 1)) {
            if (board[pit] > 0) {
                val score = evaluateMoveForAI(pit)
                if (score > bestScore) {
                    bestScore = score
                    bestPit = pit
                }
            }
        }

        return if (bestPit != -1) bestPit else getRandomAIMove()
    }

    // Сложный уровень: оценивает ходы с большей глубиной
    private fun getHardAIMove(): Int {
        var bestPit = -1
        var bestScore = -1000

        for (pit in (pitsPerPlayer + 1) until (totalPits - 1)) {
            if (board[pit] > 0) {
                // Симулируем ход
                val tempBoard = board.copyOf()
                val tempPlayer = currentPlayer

                val score = simulateMove(pit, tempBoard, tempPlayer)

                if (score > bestScore) {
                    bestScore = score
                    bestPit = pit
                }
            }
        }

        return if (bestPit != -1) bestPit else getRandomAIMove()
    }

    // Оценка хода для среднего уровня
    private fun evaluateMoveForAI(pit: Int): Int {
        var score = 0
        val stones = board[pit]
        val targetIdx = (pit + stones) % totalPits

        // Бонус за попадание в свой калах
        if (targetIdx == player2Kalah) {
            score += 10
        }

        // Бонус за возможный захват
        if (targetIdx != player2Kalah && targetIdx != player1Kalah) {
            val oppositeIdx = getOppositePit(targetIdx)
            if (board[targetIdx] == 0 && board[oppositeIdx] > 0) {
                score += 8
            }
        }

        // Бонус за ход из лунки с большим количеством камней
        score += stones / 2

        return score
    }

    // Симуляция хода для сложного уровня (упрощённая)
    private fun simulateMove(pit: Int, simBoard: IntArray, simPlayer: Int): Int {
        var stones = simBoard[pit]
        simBoard[pit] = 0
        var currentIdx = pit

        while (stones > 0) {
            currentIdx = (currentIdx + 1) % totalPits

            when {
                simPlayer == 0 && currentIdx == player2Kalah -> continue
                simPlayer == 1 && currentIdx == player1Kalah -> continue
            }

            simBoard[currentIdx]++
            stones--
        }

        var score = 0
        val extraTurn = when {
            simPlayer == 0 && currentIdx == player1Kalah -> true
            simPlayer == 1 && currentIdx == player2Kalah -> true
            else -> false
        }

        // Оцениваем результат
        if (extraTurn) {
            score += 15 // Большой бонус за дополнительный ход
        }

        // Оцениваем захват
        if (!extraTurn && currentIdx != player1Kalah && currentIdx != player2Kalah &&
            simBoard[currentIdx] == 1 && isOwnPitForSimulation(currentIdx, simPlayer)) {
            val oppositeIdx = getOppositePit(currentIdx)
            if (simBoard[oppositeIdx] > 0) {
                score += simBoard[oppositeIdx] + 5
            }
        }

        // Оцениваем, сколько камней попало в калах AI
        score += simBoard[player2Kalah] * 2

        return score
    }

    private fun isOwnPitForSimulation(pitIndex: Int, player: Int): Boolean {
        return if (player == 0) {
            pitIndex < pitsPerPlayer
        } else {
            pitIndex > pitsPerPlayer && pitIndex < totalPits - 1
        }
    }

    // Получить доступные ходы для текущего игрока
    fun getAvailableMoves(): List<Int> {
        val moves = mutableListOf<Int>()
        val start = if (currentPlayer == 0) 0 else pitsPerPlayer + 1
        val end = if (currentPlayer == 0) pitsPerPlayer else totalPits - 1

        for (i in start until end) {
            if (board[i] > 0) {
                moves.add(i)
            }
        }
        return moves
    }

    // Проверка, есть ли у игрока доступные ходы
    fun hasAvailableMoves(): Boolean {
        return getAvailableMoves().isNotEmpty()
    }
}

// Результат хода
sealed class MoveResult {
    data class Success(val player: Int, val captured: Int = 0) : MoveResult()
    data class ExtraTurn(val player: Int) : MoveResult()
    data class GameOver(val winner: String, val player1Score: Int, val player2Score: Int) : MoveResult()
    data class Invalid(val message: String) : MoveResult()
}