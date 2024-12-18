package com.example.calorix

import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment

class ProgressActivity : AppCompatActivity() {

    private lateinit var bottomNavHome: ImageButton
    private lateinit var bottomNavDishes: ImageButton
    private lateinit var bottomNavMeals: ImageButton
    private lateinit var bottomNavProfile: ImageButton

    private val selectedColor = Color.parseColor("#6200EE")
    private val defaultColor = Color.parseColor("#9E9E9E")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)

        // Инициализация кнопок
        bottomNavHome = findViewById(R.id.nav_home)
        bottomNavDishes = findViewById(R.id.nav_dishes)
        bottomNavMeals = findViewById(R.id.nav_meals)
        bottomNavProfile = findViewById(R.id.nav_profile)

        setSelectedButton(bottomNavProfile)

        // Обработчики для кнопок
        bottomNavDishes.setOnClickListener {
            // Переход на главную активность
            val intent = Intent(this@ProgressActivity, MainActivity::class.java)
            startActivity(intent)
        }
        bottomNavHome.setOnClickListener {
            // Переход на активность добавления блюда
            val intent = Intent(this@ProgressActivity, AddDishActivity::class.java)
            startActivity(intent)
        }
        bottomNavMeals.setOnClickListener {
            // Переход на активность добавления приема пищи
            val intent = Intent(this@ProgressActivity, AddMealActivity::class.java)
            startActivity(intent)
        }
        bottomNavProfile.setOnClickListener {
            // Переход на активность профиля
            val intent = Intent(this@ProgressActivity, ProgressActivity::class.java)
            startActivity(intent)
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
