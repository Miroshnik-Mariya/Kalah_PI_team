package com.example.kalah.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface GameResultDao {
    @Insert
    suspend fun insert(result: GameResult)

    @Query("SELECT * FROM game_results ORDER BY timestamp DESC")
    suspend fun getAllResults(): List<GameResult>

    @Query("SELECT * FROM game_results WHERE gameMode = :mode ORDER BY timestamp DESC")
    suspend fun getResultsByMode(mode: String): List<GameResult>

    @Query("DELETE FROM game_results")
    suspend fun deleteAll()
}