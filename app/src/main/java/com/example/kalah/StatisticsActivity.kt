package com.example.kalah

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kalah.database.AppDatabase
import com.example.kalah.database.GameResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatisticsActivity : AppCompatActivity() {

    private lateinit var rvMatches: RecyclerView
    private lateinit var tvTotalGames: TextView
    private lateinit var tvVsAICount: TextView
    private lateinit var tvTwoPlayersCount: TextView
    private lateinit var btnBack: Button
    private lateinit var btnClear: Button

    private lateinit var adapter: MatchAdapter
    private val matchesList = mutableListOf<GameResult>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        rvMatches = findViewById(R.id.rvMatches)
        tvTotalGames = findViewById(R.id.tvTotalGames)
        tvVsAICount = findViewById(R.id.tvVsAICount)
        tvTwoPlayersCount = findViewById(R.id.tvTwoPlayersCount)
        btnBack = findViewById(R.id.btnBack)
        btnClear = findViewById(R.id.btnClear)

        adapter = MatchAdapter(matchesList)
        rvMatches.layoutManager = LinearLayoutManager(this)
        rvMatches.adapter = adapter

        loadStatistics()

        btnBack.setOnClickListener {
            finish()
        }

        btnClear.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Очистить статистику")
                .setMessage("Вы уверены, что хотите удалить всю историю игр?")
                .setPositiveButton("Да") { _, _ ->
                    clearStatistics()
                }
                .setNegativeButton("Нет", null)
                .show()
        }
    }

    private fun loadStatistics() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val allResults = AppDatabase.getInstance(this@StatisticsActivity)
                    .gameResultDao()
                    .getAllResults()

                val vsAICount = allResults.count { it.gameMode == "VS_AI" }
                val twoPlayersCount = allResults.count { it.gameMode == "TWO_PLAYERS" }

                withContext(Dispatchers.Main) {
                    matchesList.clear()
                    matchesList.addAll(allResults)
                    adapter.notifyDataSetChanged()

                    tvTotalGames.text = "📊 Всего: ${allResults.size}"
                    tvVsAICount.text = "🤖 AI: $vsAICount"
                    tvTwoPlayersCount.text = "👥 2P: $twoPlayersCount"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    tvTotalGames.text = "Ошибка загрузки"
                }
            }
        }
    }

    private fun clearStatistics() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppDatabase.getInstance(this@StatisticsActivity)
                    .gameResultDao()
                    .deleteAll()

                withContext(Dispatchers.Main) {
                    matchesList.clear()
                    adapter.notifyDataSetChanged()

                    tvTotalGames.text = "📊 Всего: 0"
                    tvVsAICount.text = "🤖 AI: 0"
                    tvTwoPlayersCount.text = "👥 2P: 0"

                    AlertDialog.Builder(this@StatisticsActivity)
                        .setTitle("Статистика очищена")
                        .setMessage("Вся история игр удалена.")
                        .setPositiveButton("OK", null)
                        .show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    inner class MatchAdapter(private val matches: List<GameResult>) :
        RecyclerView.Adapter<MatchAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_match, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val match = matches[position]
            val dateFormat = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())
            val date = dateFormat.format(Date(match.timestamp))

            holder.tvPlayer1Name.text = match.player1Name
            holder.tvPlayer1Score.text = match.player1Score.toString()
            holder.tvPlayer2Name.text = match.player2Name
            holder.tvPlayer2Score.text = match.player2Score.toString()
            holder.tvDate.text = date

            when {
                match.winnerName == match.player1Name -> {
                    holder.tvResult.text = "🏆 ПОБЕДА"
                    holder.tvResult.setBackgroundColor(resources.getColor(android.R.color.holo_green_dark))
                }
                match.winnerName == match.player2Name -> {
                    holder.tvResult.text = "ПОРАЖЕНИЕ"
                    holder.tvResult.setBackgroundColor(resources.getColor(android.R.color.holo_red_dark))
                }
                else -> {
                    holder.tvResult.text = "🤝 НИЧЬЯ"
                    holder.tvResult.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
                }
            }
            holder.tvResult.setTextColor(resources.getColor(android.R.color.white))
            holder.tvResult.setPadding(8, 4, 8, 4)
        }

        override fun getItemCount(): Int = matches.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvPlayer1Name: TextView = itemView.findViewById(R.id.tvPlayer1Name)
            val tvPlayer1Score: TextView = itemView.findViewById(R.id.tvPlayer1Score)
            val tvPlayer2Name: TextView = itemView.findViewById(R.id.tvPlayer2Name)
            val tvPlayer2Score: TextView = itemView.findViewById(R.id.tvPlayer2Score)
            val tvResult: TextView = itemView.findViewById(R.id.tvResult)
            val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        }
    }
}