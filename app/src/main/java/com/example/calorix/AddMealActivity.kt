package com.example.calorix

import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment

class AddMealActivity : AppCompatActivity() {

    private lateinit var bottomNavHome: ImageButton
    private lateinit var bottomNavDishes: ImageButton
    private lateinit var bottomNavMeals: ImageButton
    private lateinit var bottomNavProfile: ImageButton

    private val selectedColor = Color.parseColor("#6200EE")
    private val defaultColor = Color.parseColor("#9E9E9E")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_meal)

        val searchRecipe = findViewById<AutoCompleteTextView>(R.id.search_recipe)
        val textInputArea = findViewById<EditText>(R.id.text_input_area)
        val addFoodButton = findViewById<Button>(R.id.add_food_button)

        val foodNames  = DatabaseHelper(this@AddMealActivity).getAllFoodNames()

        // Настройте адаптер для AutoCompleteTextView
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, foodNames)
        searchRecipe.setAdapter(adapter)

        // Настройка поведения при выборе элемента
        searchRecipe.setOnItemClickListener { _, _, position, _ ->
            val selectedFood = adapter.getItem(position)
            // Выполните действия при выборе
        }


        // Инициализация кнопок
        bottomNavHome = findViewById(R.id.nav_home)
        bottomNavDishes = findViewById(R.id.nav_dishes)
        bottomNavMeals = findViewById(R.id.nav_meals)
        bottomNavProfile = findViewById(R.id.nav_profile)

        setSelectedButton(bottomNavMeals)

        // Обработчики для кнопок
        bottomNavDishes.setOnClickListener {
            // Переход на главную активность
            val intent = Intent(this@AddMealActivity, MainActivity::class.java)
            startActivity(intent)
        }
        bottomNavHome.setOnClickListener {
            // Переход на активность добавления блюда
            val intent = Intent(this@AddMealActivity, AddDishActivity::class.java)
            startActivity(intent)
        }
        bottomNavMeals.setOnClickListener {
            // Переход на активность добавления приема пищи
            val intent = Intent(this@AddMealActivity, AddMealActivity::class.java)
            startActivity(intent)
        }
        bottomNavProfile.setOnClickListener {
            // Переход на активность профиля
            val intent = Intent(this@AddMealActivity, ProgressActivity::class.java)
            startActivity(intent)
        }

        // Установите обработчик клика для кнопки "Add"
        addFoodButton.setOnClickListener {
            // Извлечь название блюда из поля поиска
            val foodName = searchRecipe.text.toString().trim()

            // Проверяем, что название не пустое
            if (foodName.isEmpty()) {
                Toast.makeText(this, "Please enter a food name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (DatabaseHelper(this@AddMealActivity).isFoodExists(foodName)) {
                Toast.makeText(this, "Food with this name already exists", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Извлекаем текст из текстового поля
            val inputText = textInputArea.text.toString().trim()

            // Разбираем текст (пример: ключ: значение)
            val foodDetails = parseFoodDetails(inputText)

            // Добавляем данные в базу
            val isAdded = DatabaseHelper(this@AddMealActivity).addFood(
                foodName = foodName,
                servingSize = foodDetails["servingSize"]?.toFloatOrNull(),
                calories = foodDetails["calories"]?.toFloatOrNull(),
                proteins = foodDetails["proteins"]?.toFloatOrNull(),
                fats = foodDetails["fats"]?.toFloatOrNull(),
                carbs = foodDetails["carbs"]?.toFloatOrNull(),
                fiber = foodDetails["fiber"]?.toFloatOrNull(),
                sugar = foodDetails["sugar"]?.toFloatOrNull(),
                category = foodDetails["category"]
            )

            // Показываем результат
            if (isAdded) {
                Toast.makeText(this, "Food added successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to add food", Toast.LENGTH_SHORT).show()
            }
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

    private fun parseFoodDetails(input: String): Map<String, String> {
        val details = mutableMapOf<String, String>()

        // Разделяем текст на строки
        val lines = input.split("\n")
        for (line in lines) {
            val parts = line.split(":").map { it.trim() }
            if (parts.size == 2) {
                details[parts[0].lowercase()] = parts[1]
            }
        }

        return details
    }
}
