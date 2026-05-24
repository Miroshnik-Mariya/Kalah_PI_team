package com.example.kalah

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatisticsActivity : AppCompatActivity() {

    private lateinit var rvMatches: RecyclerView
    private lateinit var tvTotalGames: TextView
    private lateinit var tvVsAICount: TextView
    private lateinit var tvTwoPlayersCount: TextView
    private lateinit var btnClear: Button
    private lateinit var btnBack: Button

    private lateinit var adapter: MatchAdapter
    private val matchesList = mutableListOf<GameResultSimple>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        try {
            rvMatches = findViewById(R.id.rvMatches)
            tvTotalGames = findViewById(R.id.tvTotalGames)
            tvVsAICount = findViewById(R.id.tvVsAICount)
            tvTwoPlayersCount = findViewById(R.id.tvTwoPlayersCount)
            btnClear = findViewById(R.id.btnClear)
            btnBack = findViewById(R.id.btnBack)

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
        } catch (e: Exception) {
            e.printStackTrace()
            tvTotalGames.text = "Ошибка: ${e.message}"
        }
    }

    private fun loadStatistics() {
        try {
            val allResults = StatisticsManager.getResults(this)

            val vsAICount = allResults.count { it.gameMode == "VS_AI" }
            val twoPlayersCount = allResults.count { it.gameMode == "TWO_PLAYERS" }

            matchesList.clear()
            matchesList.addAll(allResults)
            adapter.notifyDataSetChanged()

            tvTotalGames.text = "📊 Всего: ${allResults.size}"
            tvVsAICount.text = "🤖 AI: $vsAICount"
            tvTwoPlayersCount.text = "👥 2P: $twoPlayersCount"

            if (allResults.isEmpty()) {
                tvTotalGames.text = "📊 Всего: 0\n\nНет сохранённых игр"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            tvTotalGames.text = "❌ Ошибка загрузки: ${e.message}"
            tvVsAICount.text = "🤖 AI: --"
            tvTwoPlayersCount.text = "👥 2P: --"
        }
    }

    private fun clearStatistics() {
        try {
            StatisticsManager.clearResults(this)

            matchesList.clear()
            adapter.notifyDataSetChanged()

            tvTotalGames.text = "📊 Всего: 0"
            tvVsAICount.text = "🤖 AI: 0"
            tvTwoPlayersCount.text = "👥 2P: 0"

            AlertDialog.Builder(this)
                .setTitle("Статистика очищена")
                .setMessage("Вся история игр удалена.")
                .setPositiveButton("OK", null)
                .show()
        } catch (e: Exception) {
            e.printStackTrace()
            AlertDialog.Builder(this)
                .setTitle("Ошибка")
                .setMessage("Не удалось очистить статистику: ${e.message}")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    inner class MatchAdapter(private val matches: List<GameResultSimple>) :
        RecyclerView.Adapter<MatchAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_match, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            try {
                val match = matches[position]
                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                val date = dateFormat.format(Date(match.timestamp))

                // Устанавливаем дату
                holder.tvDate.text = date

                // Устанавливаем режим игры
                val gameModeText = if (match.gameMode == "VS_AI") "🤖 Против AI" else "👥 Два игрока"
                val gameModeColor = if (match.gameMode == "VS_AI") "#2196F3" else "#4CAF50"
                holder.tvGameMode.text = gameModeText
                holder.tvGameMode.setBackgroundColor(Color.parseColor(gameModeColor))

                // Устанавливаем имена
                holder.tvPlayer1Name.text = match.player1Name
                holder.tvPlayer2Name.text = match.player2Name

                // Устанавливаем счета
                holder.tvPlayer1Score.text = match.player1Score.toString()
                holder.tvPlayer2Score.text = match.player2Score.toString()

                // Устанавливаем параметры игры
                holder.tvSettings.text = "${match.pitsPerPlayer} лунок | ${match.stonesPerPit} камня"

                // Устанавливаем аватары (используем стандартные)
                try {
                    val avatarRes = when (position % 10) {
                        0 -> R.drawable.avatar1
                        1 -> R.drawable.avatar2
                        2 -> R.drawable.avatar3
                        3 -> R.drawable.avatar4
                        4 -> R.drawable.avatar5
                        5 -> R.drawable.avatar6
                        6 -> R.drawable.avatar7
                        7 -> R.drawable.avatar8
                        8 -> R.drawable.avatar9
                        else -> R.drawable.avatar10
                    }
                    holder.ivPlayer1Avatar.setImageResource(avatarRes)

                    val avatarRes2 = when (position % 10) {
                        0 -> R.drawable.avatar2
                        1 -> R.drawable.avatar3
                        2 -> R.drawable.avatar4
                        3 -> R.drawable.avatar5
                        4 -> R.drawable.avatar6
                        5 -> R.drawable.avatar7
                        6 -> R.drawable.avatar8
                        7 -> R.drawable.avatar9
                        8 -> R.drawable.avatar10
                        else -> R.drawable.avatar1
                    }
                    holder.ivPlayer2Avatar.setImageResource(avatarRes2)
                } catch (e: Exception) {
                    // Если аватаров нет, оставляем как есть
                }

                // Настраиваем результат
                when {
                    match.winnerName == match.player1Name -> {
                        holder.tvResult.text = "🏆 ПОБЕДА"
                        holder.tvResult.setBackgroundColor(Color.parseColor("#4CAF50"))
                        holder.tvResult.setTextColor(Color.WHITE)
                    }
                    match.winnerName == match.player2Name -> {
                        holder.tvResult.text = "💔 ПОРАЖЕНИЕ"
                        holder.tvResult.setBackgroundColor(Color.parseColor("#F44336"))
                        holder.tvResult.setTextColor(Color.WHITE)
                    }
                    else -> {
                        holder.tvResult.text = "🤝 НИЧЬЯ"
                        holder.tvResult.setBackgroundColor(Color.parseColor("#9E9E9E"))
                        holder.tvResult.setTextColor(Color.WHITE)
                    }
                }

                holder.tvResult.setPadding(20, 6, 20, 6)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun getItemCount(): Int = matches.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvDate: TextView = itemView.findViewById(R.id.tvDate)
            val tvGameMode: TextView = itemView.findViewById(R.id.tvGameMode)
            val tvPlayer1Name: TextView = itemView.findViewById(R.id.tvPlayer1Name)
            val tvPlayer1Score: TextView = itemView.findViewById(R.id.tvPlayer1Score)
            val tvPlayer2Name: TextView = itemView.findViewById(R.id.tvPlayer2Name)
            val tvPlayer2Score: TextView = itemView.findViewById(R.id.tvPlayer2Score)
            val tvResult: TextView = itemView.findViewById(R.id.tvResult)
            val tvSettings: TextView = itemView.findViewById(R.id.tvSettings)
            val ivPlayer1Avatar: ImageView = itemView.findViewById(R.id.ivPlayer1Avatar)
            val ivPlayer2Avatar: ImageView = itemView.findViewById(R.id.ivPlayer2Avatar)
        }
    }
}