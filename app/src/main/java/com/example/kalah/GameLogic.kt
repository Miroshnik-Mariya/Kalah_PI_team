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
    private var currentPlayer = 0 // 0 - игрок 1 (нижний ряд), 1 - игрок 2 (верхний ряд)
    private var gameIsOver = false

    // Индексы калахов
    val player1Kalah = pitsPerPlayer           // Калах игрока 1 (нижний ряд, справа)
    val player2Kalah = totalPits - 1           // Калах игрока 2 (верхний ряд, слева)

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
    fun getPlayer1Score(): Int = board[player1Kalah]
    fun getPlayer2Score(): Int = board[player2Kalah]

    // Проверка, может ли игрок сходить из этой лунки
    fun canMove(pitIndex: Int): Boolean {
        if (gameIsOver) return false
        if (board[pitIndex] == 0) return false

        return if (currentPlayer == 0) {
            // Игрок 1: лунки от 0 до pitsPerPlayer-1
            pitIndex in 0 until pitsPerPlayer
        } else {
            // Игрок 2: лунки от pitsPerPlayer+1 до totalPits-2
            pitIndex in (pitsPerPlayer + 1) until (totalPits - 1)
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
        var extraTurn = false

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
        if ((currentPlayer == 0 && currentIdx == player1Kalah) ||
            (currentPlayer == 1 && currentIdx == player2Kalah)) {
            extraTurn = true
        }

        // Проверка на захват (только если последняя лунка не калах и не пуста)
        var captured = 0
        if (!extraTurn && currentIdx != player1Kalah && currentIdx != player2Kalah) {
            if (isOwnPit(currentIdx) && board[currentIdx] == 1) {
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
        }

        // Проверка окончания игры
        checkGameOver()

        return when {
            gameIsOver -> MoveResult.GameOver(getWinner(), getPlayer1Score(), getPlayer2Score())
            extraTurn -> MoveResult.ExtraTurn(currentPlayer, captured)
            else -> MoveResult.Success(currentPlayer, captured)
        }
    }

    // Переключить игрока
    fun switchPlayer() {
        if (!gameIsOver) {
            currentPlayer = 1 - currentPlayer
        }
    }

    // Проверка, принадлежит ли лунка текущему игроку
    private fun isOwnPit(pitIndex: Int): Boolean {
        return if (currentPlayer == 0) {
            pitIndex in 0 until pitsPerPlayer
        } else {
            pitIndex in (pitsPerPlayer + 1) until (totalPits - 1)
        }
    }

    // Получить противоположную лунку
    private fun getOppositePit(pitIndex: Int): Int {
        // Формула для противоположной лунки
        // Для лунок игрока 1 (0..pitsPerPlayer-1) -> противоположная лунка игрока 2
        // Для лунок игрока 2 -> противоположная лунка игрока 1
        return (totalPits - 2) - pitIndex
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

    fun getCurrentPlayer(): Int = currentPlayer

    // Получить доступные ходы для текущего игрока
    fun getAvailableMoves(): List<Int> {
        val moves = mutableListOf<Int>()
        if (gameIsOver) return moves

        if (currentPlayer == 0) {
            for (i in 0 until pitsPerPlayer) {
                if (board[i] > 0) moves.add(i)
            }
        } else {
            for (i in (pitsPerPlayer + 1) until (totalPits - 1)) {
                if (board[i] > 0) moves.add(i)
            }
        }
        return moves
    }

    // Проверка, есть ли у игрока доступные ходы
    fun hasAvailableMoves(): Boolean = getAvailableMoves().isNotEmpty()

    // ========== AI ЛОГИКА ==========

    fun getAIMove(): Int {
        if (!isVsAI || currentPlayer != 1 || gameIsOver) return -1
        if (!hasAvailableMoves()) return -1

        return when (aiDifficulty) {
            1 -> getRandomAIMove()
            2 -> getMediumAIMove()
            3 -> getHardAIMove()
            else -> getRandomAIMove()
        }
    }

    private fun getRandomAIMove(): Int {
        val availablePits = getAvailableMoves()
        return if (availablePits.isNotEmpty()) {
            availablePits[Random.nextInt(availablePits.size)]
        } else -1
    }

    private fun getMediumAIMove(): Int {
        var bestPit = -1
        var bestScore = -1

        for (pit in getAvailableMoves()) {
            val score = evaluateMoveForAI(pit)
            if (score > bestScore) {
                bestScore = score
                bestPit = pit
            }
        }
        return if (bestPit != -1) bestPit else getRandomAIMove()
    }

    private fun getHardAIMove(): Int {
        var bestPit = -1
        var bestScore = -1000

        for (pit in getAvailableMoves()) {
            val tempBoard = board.copyOf()
            val tempPlayer = currentPlayer
            val score = simulateMove(pit, tempBoard, tempPlayer)
            if (score > bestScore) {
                bestScore = score
                bestPit = pit
            }
        }
        return if (bestPit != -1) bestPit else getRandomAIMove()
    }

    private fun evaluateMoveForAI(pit: Int): Int {
        var score = 0
        val stones = board[pit]
        val targetIdx = (pit + stones) % totalPits

        // Бонус за попадание в свой калах
        if (targetIdx == player2Kalah) {
            score += 10
        }

        // Бонус за возможный захват
        if (targetIdx != player2Kalah && targetIdx != player1Kalah && targetIdx in (pitsPerPlayer + 1) until (totalPits - 1)) {
            val oppositeIdx = getOppositePit(targetIdx)
            if (board[targetIdx] == 0 && board[oppositeIdx] > 0) {
                score += 8
            }
        }

        // Бонус за ход из лунки с большим количеством камней
        score += stones / 2

        return score
    }

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
        val extraTurn = (simPlayer == 1 && currentIdx == player2Kalah)

        if (extraTurn) {
            score += 15
        }

        if (!extraTurn && currentIdx != player1Kalah && currentIdx != player2Kalah) {
            val isOwn = if (simPlayer == 1) {
                currentIdx in (pitsPerPlayer + 1) until (totalPits - 1)
            } else {
                currentIdx < pitsPerPlayer
            }

            if (isOwn && simBoard[currentIdx] == 1) {
                val oppositeIdx = getOppositePit(currentIdx)
                if (simBoard[oppositeIdx] > 0) {
                    score += simBoard[oppositeIdx] + 5
                }
            }
        }

        score += simBoard[player2Kalah] * 2
        return score
    }
}

// Результат хода
sealed class MoveResult {
    data class Success(val player: Int, val captured: Int = 0) : MoveResult()
    data class ExtraTurn(val player: Int, val captured: Int = 0) : MoveResult()
    data class GameOver(val winner: String, val player1Score: Int, val player2Score: Int) : MoveResult()
    data class Invalid(val message: String) : MoveResult()
}