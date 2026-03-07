package com.example.kalah

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
    }

//    ВЕРХНЕЕ МЕНЮ
    val tm_set: ImageButton = findViewById(R.id.btn_tm_set)
    val tm_info: ImageButton = findViewById(R.id.btn_tm_info)
    val tm_sound: ImageButton = findViewById(R.id.btn_tm_sound)
    val tm_statistic: ImageButton = findViewById(R.id.btn_tm_statistic)
}