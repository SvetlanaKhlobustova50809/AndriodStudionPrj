package com.example.calorix

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ChatAdapter
import com.example.ChatViewModel
import com.example.calorix.R
import com.example.ui.theme.ChatGPTChatTheme
import java.util.Locale

class ChatActivity: ComponentActivity()  {
    private lateinit var bottomNavHome: ImageButton
    private lateinit var bottomNavDishes: ImageButton
    private lateinit var bottomNavMeals: ImageButton
    private lateinit var bottomNavProfile: ImageButton

    private val selectedColor = Color.parseColor("#6200EE")
    private val defaultColor = Color.parseColor("#9E9E9E")
    private val chatViewModel: ChatViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = ChatAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        // Observe chat messages
        chatViewModel.messages.observe(this) { messages ->
            adapter.submitList(messages)
            recyclerView.scrollToPosition(messages.size - 1)
        }
        val inputMessage = findViewById<EditText>(R.id.inputMessage)
        // Установка русской локали для клавиатуры
        val russianLocale = Locale("ru", "RU") // Использование конструктора Locale
        inputMessage.setImeHintLocales(android.os.LocaleList(russianLocale))
        inputMessage.setOnEditorActionListener { textView, _, _ ->
            val message = textView.text.toString()
            if (message.isNotBlank()) {
                chatViewModel.sendMessage(message)
                textView.text = null
            }
            true
        }

        val userId = intent.getIntExtra("USERID", -1)



        // Инициализация кнопок
        bottomNavHome = findViewById(R.id.nav_home)
        bottomNavDishes = findViewById(R.id.nav_dishes)
        bottomNavMeals = findViewById(R.id.nav_meals)
        bottomNavProfile = findViewById(R.id.nav_profile)

        setSelectedButton(bottomNavProfile)

        // Обработчики для кнопок
        bottomNavDishes.setOnClickListener {
            // Переход на главную активность
            val intent = Intent(this@ChatActivity, MainActivity::class.java)
            intent.putExtra("USERID", userId)
            startActivity(intent)
        }
        bottomNavHome.setOnClickListener {
            // Переход на активность добавления блюда
            val intent = Intent(this@ChatActivity, AddDishActivity::class.java)
            intent.putExtra("USERID", userId)
            startActivity(intent)
        }
        bottomNavMeals.setOnClickListener {
            val intent = Intent(this@ChatActivity, AddMealActivity::class.java)
            intent.putExtra("USERID", userId)
            startActivity(intent)
        }
        bottomNavProfile.setOnClickListener {
            // Переход на активность профиля
        }
    }

    private fun setSelectedButton(selectedButton: ImageButton) {
        // Сброс цвета у всех кнопок
        bottomNavHome.setColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
        bottomNavDishes.setColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
        bottomNavMeals.setColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
        bottomNavProfile.setColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)

        // Установка цвета для выбранной кнопки
        selectedButton.setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN)
    }
}