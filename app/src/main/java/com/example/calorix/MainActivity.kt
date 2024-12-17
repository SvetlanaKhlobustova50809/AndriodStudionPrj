package com.example.calorix

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var addDishButton: Button
    private lateinit var addMealButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация кнопок
        addDishButton = findViewById(R.id.addDishButton)
        addMealButton = findViewById(R.id.addMealButton)

        // Обработчик для кнопки "Добавить блюдо"
        addDishButton.setOnClickListener {
            // Переход на активность для добавления блюда
            val intent = Intent(this@MainActivity, AddDishActivity::class.java)
            startActivity(intent)
        }

        // Обработчик для кнопки "Добавить прием пищи"
        addMealButton.setOnClickListener {
            // Переход на активность для добавления приема пищи
            val intent = Intent(this@MainActivity, AddMealActivity::class.java)
            startActivity(intent)
        }
    }
}
