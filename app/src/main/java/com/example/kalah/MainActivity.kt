package com.example.kalah

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private var isMusicOn = true
    private val PERMISSION_REQUEST_CODE = 100

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

        // Кнопка "Подробнее о системе"
        val downloadButton = dialog.findViewById<Button>(R.id.btn_download_manual)
        downloadButton.setOnClickListener {
            dialog.dismiss()
            checkPermissionAndOpenPdf()
        }

        dialog.show()
    }

    private fun checkPermissionAndOpenPdf() {
        // Для Android 13+ (API 33+) нужно другое разрешение
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ не требует разрешения для чтения файлов в кэше приложения
            openPdfFromRaw()
        }
        // Для Android 6-12
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                openPdfFromRaw()
            }
        } else {
            openPdfFromRaw()
        }
    }

    private fun openPdfFromRaw() {
        try {
            // Пытаемся открыть PDF из ресурсов
            val pdfResourceId = resources.getIdentifier("system_manual", "raw", packageName)

            if (pdfResourceId == 0) {
                Toast.makeText(this, "PDF файл не найден в ресурсах", Toast.LENGTH_LONG).show()
                return
            }

            // Копируем PDF из raw во временный файл
            val inputStream = resources.openRawResource(pdfResourceId)
            val tempFile = File(cacheDir, "system_manual.pdf")

            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()

            // Открываем PDF
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val uri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    tempFile
                )

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                }

                startActivity(intent)
            } else {
                val uri = Uri.fromFile(tempFile)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                }
                startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка при открытии PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openPdfFromRaw()
                } else {
                    Toast.makeText(this, "Нужно разрешение для открытия PDF", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}