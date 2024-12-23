package com.example

import android.os.Bundle
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.calorie_counter.R
import com.example.ui.theme.ChatGPTChatTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
    }
}
