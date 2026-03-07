package com.example.kalah

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        //ВЕРХНЕЕ МЕНЮ
        val tm_set: ImageButton = findViewById(R.id.btn_tm_set)
        val tm_info: ImageButton = findViewById(R.id.btn_tm_info)
        val tm_sound: ImageButton = findViewById(R.id.btn_tm_sound)
        val tm_statistic: ImageButton = findViewById(R.id.btn_tm_statistic)

        //ОСНОВНЫЕ КНОПКИ
        val btn_play_tog: ImageButton = findViewById(R.id.im_btn_play_together)
        val btn_play_ai: ImageButton = findViewById(R.id.im_btn_play_ai)
        val btn_setting: ImageButton = findViewById(R.id.im_btn_settings)



        //КНОПКА ИНФОРМАЦИОННАЯ - ОБ АВТОРАХ
        tm_info.setOnClickListener {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.info_dialog)

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val okText = dialog.findViewById<TextView>(R.id.dialog_button)
            okText.setOnClickListener {
                dialog.dismiss()  // Только закрываем окно
            }
            dialog.show()  // Показываем диалог ВНЕ обработчика!
        }

    }
}