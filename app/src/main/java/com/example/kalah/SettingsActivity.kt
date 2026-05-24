package com.example.kalah

import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var radioGroupBoardStyle: RadioGroup
    private lateinit var radioWood: RadioButton
    private lateinit var radioMetal: RadioButton
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        radioGroupBoardStyle = findViewById(R.id.radioGroupBoardStyle)
        radioWood = findViewById(R.id.radioWood)
        radioMetal = findViewById(R.id.radioMetal)
        btnSave = findViewById(R.id.btnSave)

        // Загружаем сохранённые настройки
        val currentStyle = SettingsManager.getBoardStyle(this)
        if (currentStyle == "wood") {
            radioWood.isChecked = true
        } else {
            radioMetal.isChecked = true
        }

        btnSave.setOnClickListener {
            val selectedStyle = when (radioGroupBoardStyle.checkedRadioButtonId) {
                R.id.radioWood -> "wood"
                R.id.radioMetal -> "metal"
                else -> "wood"
            }
            SettingsManager.saveBoardStyle(this, selectedStyle)
            finish()
        }
    }
}