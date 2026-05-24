package com.example.kalah

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

object ToastManager {
    private var currentToast: Toast? = null
    private val hideHandler = Handler(Looper.getMainLooper())
    private var hideRunnable: Runnable? = null

    fun showShort(context: Context, message: String) {
        // Отменяем текущий Toast
        currentToast?.cancel()

        // Отменяем запланированное скрытие
        hideRunnable?.let { hideHandler.removeCallbacks(it) }

        // Создаём новый Toast
        currentToast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        currentToast?.show()

        // Автоматически скрыть через 1 секунду (даже если Toast сам не скрылся)
        hideRunnable = Runnable {
            currentToast?.cancel()
            currentToast = null
        }
        hideHandler.postDelayed(hideRunnable!!, 1000)
    }

    fun showLong(context: Context, message: String) {
        currentToast?.cancel()
        hideRunnable?.let { hideHandler.removeCallbacks(it) }

        currentToast = Toast.makeText(context, message, Toast.LENGTH_LONG)
        currentToast?.show()

        hideRunnable = Runnable {
            currentToast?.cancel()
            currentToast = null
        }
        hideHandler.postDelayed(hideRunnable!!, 2000)
    }

    fun cancel() {
        currentToast?.cancel()
        currentToast = null
        hideRunnable?.let { hideHandler.removeCallbacks(it) }
        hideRunnable = null
    }

    // Вызывать при каждом ходе игрока
    fun cancelForNextMove() {
        currentToast?.cancel()
        currentToast = null
        hideRunnable?.let { hideHandler.removeCallbacks(it) }
    }
}