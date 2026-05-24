package com.example.kalah

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class GameResultSimple(
    val player1Name: String,
    val player2Name: String,
    val winnerName: String,
    val player1Score: Int,
    val player2Score: Int,
    val pitsPerPlayer: Int,
    val stonesPerPit: Int,
    val gameMode: String,
    val timestamp: Long = System.currentTimeMillis()
)

object StatisticsManager {
    private const val PREFS_NAME = "game_statistics"
    private const val KEY_RESULTS = "results_list"

    fun saveResult(context: Context, result: GameResultSimple) {
        val results = getResults(context).toMutableList()
        results.add(0, result)

        if (results.size > 100) {
            results.removeAt(results.size - 1)
        }

        val jsonArray = JSONArray()
        for (r in results) {
            val jsonObject = JSONObject().apply {
                put("player1Name", r.player1Name)
                put("player2Name", r.player2Name)
                put("winnerName", r.winnerName)
                put("player1Score", r.player1Score)
                put("player2Score", r.player2Score)
                put("pitsPerPlayer", r.pitsPerPlayer)
                put("stonesPerPit", r.stonesPerPit)
                put("gameMode", r.gameMode)
                put("timestamp", r.timestamp)
            }
            jsonArray.put(jsonObject)
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_RESULTS, jsonArray.toString()).apply()
    }

    fun getResults(context: Context): List<GameResultSimple> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_RESULTS, null)

        if (json.isNullOrEmpty()) return emptyList()

        try {
            val results = mutableListOf<GameResultSimple>()
            val jsonArray = JSONArray(json)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val result = GameResultSimple(
                    player1Name = obj.getString("player1Name"),
                    player2Name = obj.getString("player2Name"),
                    winnerName = obj.getString("winnerName"),
                    player1Score = obj.getInt("player1Score"),
                    player2Score = obj.getInt("player2Score"),
                    pitsPerPlayer = obj.getInt("pitsPerPlayer"),
                    stonesPerPit = obj.getInt("stonesPerPit"),
                    gameMode = obj.getString("gameMode"),
                    timestamp = obj.getLong("timestamp")
                )
                results.add(result)
            }
            return results
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    fun clearResults(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_RESULTS).apply()
    }
}