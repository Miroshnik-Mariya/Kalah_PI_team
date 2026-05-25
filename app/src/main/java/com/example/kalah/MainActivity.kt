package com.example.kalah

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private var isMusicOn = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Запускаем фоновую музыку (только если она включена)
        isMusicOn = SettingsManager.isMusicOn(this)
        if (isMusicOn) {
            MusicManager.start(this, R.raw.background_music)
        }

        // ВЕРХНЕЕ МЕНЮ
        val tmSet: ImageButton = findViewById(R.id.btn_tm_set)
        val tmInfo: ImageButton = findViewById(R.id.btn_tm_info)
        val tmSound: ImageButton = findViewById(R.id.btn_tm_sound)
        val tmStatistic: ImageButton = findViewById(R.id.btn_tm_statistic)

        // ОСНОВНЫЕ КНОПКИ
        val btnPlayTogether: ImageButton = findViewById(R.id.im_btn_play_together)
        val btnPlayAi: ImageButton = findViewById(R.id.im_btn_play_ai)
        val btnSetting: ImageButton = findViewById(R.id.im_btn_settings)

        updateMusicIcon(tmSound)

        tmInfo.setOnClickListener {
            showInfoDialog()
        }

        btnPlayTogether.setOnClickListener {
            startRegistration(isVsAI = false)
        }

        btnPlayAi.setOnClickListener {
            startRegistration(isVsAI = true)
        }

        tmStatistic.setOnClickListener {
            val intent = Intent(this, StatisticsActivity::class.java)
            startActivity(intent)
        }

        btnSetting.setOnClickListener {
            try {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        tmSet.setOnClickListener {
            try {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        tmSound.setOnClickListener {
            if (isMusicOn) {
                MusicManager.pause()
                isMusicOn = false
                Toast.makeText(this, "Музыка выключена", Toast.LENGTH_SHORT).show()
            } else {
                MusicManager.resume()
                isMusicOn = true
                Toast.makeText(this, "Музыка включена", Toast.LENGTH_SHORT).show()
            }
            SettingsManager.saveMusicOn(this, isMusicOn)
            updateMusicIcon(tmSound)
        }
    }

    private fun updateMusicIcon(soundButton: ImageButton) {
        if (isMusicOn) {
            soundButton.setImageResource(R.drawable.topmenu_sound_on)
        } else {
            soundButton.setImageResource(R.drawable.topmenu_sound_off)
        }
    }

    private fun startRegistration(isVsAI: Boolean) {
        val intent = Intent(this, Registration::class.java)
        intent.putExtra("GAME_MODE", if (isVsAI) "VS_AI" else "TWO_PLAYERS")
        intent.putExtra("PLAYER_NUMBER", 1)
        startActivity(intent)
    }

    private fun showInfoDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.info_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        val okText = dialog.findViewById<ImageButton>(R.id.dialog_button)
        okText.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    // УБИРАЕМ onPause и onResume - музыка не останавливается
}