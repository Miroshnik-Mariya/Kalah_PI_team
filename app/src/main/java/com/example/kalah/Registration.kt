package com.example.kalah

//import android.R
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class Registration : AppCompatActivity() {

    // Список аватаров
    private val avatars = listOf(
        R.drawable.avatar1, R.drawable.avatar2, R.drawable.avatar3,
        R.drawable.avatar4, R.drawable.avatar5, R.drawable.avatar6,
        R.drawable.avatar7, R.drawable.avatar8, R.drawable.avatar9,
        R.drawable.avatar10
    )

    private var selectedAvatar = R.drawable.avatar1
    private var isVsAI = false
    private var playerNumber = 1
    private var player1Name = ""
    private var player1Avatar = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        // Получаем параметры из Intent
        isVsAI = intent.getStringExtra("GAME_MODE") == "VS_AI"
        playerNumber = intent.getIntExtra("PLAYER_NUMBER", 1)
        player1Name = intent.getStringExtra("PLAYER1_NAME") ?: ""
        player1Avatar = intent.getIntExtra("PLAYER1_AVATAR", R.drawable.avatar1)

        val ivAvatar: ImageView = findViewById(R.id.iv_avatar)
        val editEnterName: EditText = findViewById(R.id.editEnterName)
        val btnNext: ImageButton = findViewById(R.id.btn_next)
        val textViewTitle: TextView = findViewById(R.id.textView)

        // Устанавливаем заголовок
        if (isVsAI) {
            textViewTitle.text = "РЕГИСТРАЦИЯ"
        } else {
            textViewTitle.text = if (playerNumber == 1) "ИГРОК 1" else "ИГРОК 2"
        }

        // Устанавливаем начальный аватар
        try {
            ivAvatar.setImageResource(selectedAvatar)
        } catch (e: Exception) {
            ivAvatar.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // Выбор аватара
        ivAvatar.setOnClickListener {
            showAvatarPickerDialog()
        }


        // Кнопка Далее
        btnNext.setOnClickListener {
            val playerName = editEnterName.text.toString().trim()
            if (playerName.isNotEmpty()) {
                if (isVsAI) {
                    // Режим с AI - переходим в NewGameSetup
                    val intent = Intent(this, NewGameSetup::class.java).apply {
                        putExtra("PLAYER1_NAME", playerName)
                        putExtra("PLAYER1_AVATAR", selectedAvatar)
                        putExtra("GAME_MODE", "VS_AI")
                        putExtra("PLAYER2_NAME", "AI Bot")
                        putExtra("PLAYER2_AVATAR", R.drawable.robot_avatar)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    if (playerNumber == 1) {
                        // Сохраняем первого игрока и открываем регистрацию второго
                        val intent = Intent(this, Registration::class.java).apply {
                            putExtra("GAME_MODE", "TWO_PLAYERS")
                            putExtra("PLAYER_NUMBER", 2)
                            putExtra("PLAYER1_NAME", playerName)
                            putExtra("PLAYER1_AVATAR", selectedAvatar)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        // Второй игрок, переходим в NewGameSetup
                        val intent = Intent(this, NewGameSetup::class.java).apply {
                            putExtra("PLAYER1_NAME", player1Name)
                            putExtra("PLAYER1_AVATAR", player1Avatar)
                            putExtra("PLAYER2_NAME", playerName)
                            putExtra("PLAYER2_AVATAR", selectedAvatar)
                            putExtra("GAME_MODE", "TWO_PLAYERS")
                        }
                        startActivity(intent)
                        finish()
                    }
                }
            } else {
                editEnterName.error = "Введите имя"
            }
        }
    }

    private fun showAvatarPickerDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_avatar_picker)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Устанавливаем ширину диалога (80% от ширины экрана)
        val displayMetrics = resources.displayMetrics
        val width = (displayMetrics.widthPixels * 0.8).toInt()
        dialog.window?.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)

        val gridView = dialog.findViewById<GridView>(R.id.gridViewAvatars)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)

        val adapter = AvatarAdapter(this, avatars, selectedAvatar)
        gridView.adapter = adapter

        gridView.setOnItemClickListener { _, _, position, _ ->
            selectedAvatar = avatars[position]
            val ivAvatar: ImageView = findViewById(R.id.iv_avatar)
            ivAvatar.setImageResource(selectedAvatar)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onPause() {
        super.onPause()
        // Ничего не делаем с музыкой
    }
}