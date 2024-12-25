package com.example.calorix

import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavHome: ImageButton
    private lateinit var bottomNavDishes: ImageButton
    private lateinit var bottomNavMeals: ImageButton
    private lateinit var bottomNavProfile: ImageButton

    private val selectedColor = Color.parseColor("#6200EE")
    private val defaultColor = Color.parseColor("#9E9E9E")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Получаем имя пользователя из Intent
        val userId = intent.getIntExtra("USERID", -1)
        val user_info = DatabaseHelper(this@MainActivity).getUserById(userId)
        val userName = (user_info["user_name"] as? String).toString()

        // Устанавливаем приветствие в TextView
        val greetingText = findViewById<TextView>(R.id.greetingText)
        greetingText.text = "Hi, $userName"

        // Устанавливаем текущую дату
        val date = findViewById<TextView>(R.id.tomorrow_header)
        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDate)
        date.text = formattedDate

        // Считаем данные за день
        val dateForSql = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentDate)
        val mealSummary = DatabaseHelper(this@MainActivity).getMealLogSummary(userId, dateForSql)

        val calories = findViewById<TextView>(R.id.calories_val)
        calories.text = (mealSummary["total_calories"] as? Float ?: 0f).toString()
        val proteins = findViewById<TextView>(R.id.proteins_val)
        proteins.text = (mealSummary["total_proteins"] as? Float ?: 0f).toString()
        val fats = findViewById<TextView>(R.id.fats_val)
        fats.text = (mealSummary["total_fats"] as? Float ?: 0f).toString()
        val carbs = findViewById<TextView>(R.id.carbo_val)
        carbs.text = (mealSummary["total_carbs"] as? Float ?: 0f).toString()

        // Кнопки для добавления пищи
        val addFoodBreakfastButton = findViewById<Button>(R.id.add_food_breakfast_button)
        val addFoodLunchButton = findViewById<Button>(R.id.add_food_lunch_button)
        val addFoodDinnerButton = findViewById<Button>(R.id.add_food_dinner_button)

        addFoodBreakfastButton.setOnClickListener {
            showAddFoodDialog("Breakfast")
        }

        addFoodLunchButton.setOnClickListener {
            showAddFoodDialog("Lunch")
        }

        addFoodDinnerButton.setOnClickListener {
            showAddFoodDialog("Dinner")
        }

        // Инициализация кнопок для нижней навигации
        bottomNavHome = findViewById(R.id.nav_home)
        bottomNavDishes = findViewById(R.id.nav_dishes)
        bottomNavMeals = findViewById(R.id.nav_meals)
        bottomNavProfile = findViewById(R.id.nav_profile)

        setSelectedButton(bottomNavDishes)

        bottomNavDishes.setOnClickListener {
            // Переход на экран с блюдами
        }

        bottomNavHome.setOnClickListener {
            // Переход на экран добавления блюда
            val intent = Intent(this@MainActivity, AddDishActivity::class.java)
            intent.putExtra("USERID", userId)
            startActivity(intent)
        }

        bottomNavMeals.setOnClickListener {
            // Переход на экран добавления приема пищи
            val intent = Intent(this@MainActivity, AddMealActivity::class.java)
            intent.putExtra("USERID", userId)
            startActivity(intent)
        }

        bottomNavProfile.setOnClickListener {
            // Переход на экран профиля
            val intent = Intent(this@MainActivity, ChatActivity::class.java)
            intent.putExtra("USERID", userId)
            startActivity(intent)
        }

        // Загружаем приемы пищи для текущего дня
        loadMealsForToday()
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

    private fun showAddFoodDialog(mealType: String) {
        val mealsList = DatabaseHelper(this@MainActivity).getAllFoodNames()

        // Инфлейтируем вид для диалога
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_food, null)

        // Находим элементы в диалоге
        val mealNameEditText = dialogView.findViewById<AutoCompleteTextView>(R.id.meal_name_edit_text)
        val servingSizeEditText = dialogView.findViewById<EditText>(R.id.serving_size_edit_text)

        // Настроим адаптер для AutoCompleteTextView с данными из базы данных
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, mealsList)
        mealNameEditText.setAdapter(adapter)

        // Получаем userId из Intent
        val userId = intent.getIntExtra("USERID", -1)

        // Получаем текущее время
        val currentTime = Calendar.getInstance().time
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault()) // Для формата TIME
        val formattedTime = timeFormat.format(currentTime)

        // Создаем AlertDialog
        val dialog = AlertDialog.Builder(this)
            .setTitle("Add $mealType")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                // Получаем введенное название блюда и количество порций
                val mealName = mealNameEditText.text.toString()
                val servingSize = servingSizeEditText.text.toString().toIntOrNull() ?: 1 // По умолчанию 1 порция, если не указано

                // Вызов функции для добавления в журнал приема пищи
                val success = DatabaseHelper(this@MainActivity).addMealLog(mealName, servingSize, userId, formattedTime, mealType)

                if (success) {
                    Log.d("AddFoodDialog", "Meal successfully added: $mealName, $mealType, Quantity: $servingSize")
                    loadMealsForToday() // Перезагружаем данные после добавления
                } else {
                    Log.d("AddFoodDialog", "Failed to add meal.")
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun loadMealsForToday() {
        val userId = intent.getIntExtra("USERID", -1)
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)

        val mealLogs = DatabaseHelper(this@MainActivity).getMealsForDate(userId, currentDate)

        try {
            findViewById<LinearLayout>(R.id.breakfast_list).removeAllViews()
            findViewById<LinearLayout>(R.id.lunch_list).removeAllViews()
            findViewById<LinearLayout>(R.id.dinner_list).removeAllViews()
        } catch (e: Exception) {
            return
        }

        for ((mealType, meals) in mealLogs) {
            for (mealData in meals) {
                try {
                    val mealView = createMealView(mealData)

                    when (mealType.lowercase(Locale.getDefault())) {
                        "breakfast" -> findViewById<LinearLayout>(R.id.breakfast_list).addView(mealView)
                        "lunch" -> findViewById<LinearLayout>(R.id.lunch_list).addView(mealView)
                        "dinner" -> findViewById<LinearLayout>(R.id.dinner_list).addView(mealView)
                    }
                } catch (e: Exception) {
                    Log.e("LoadMeals", "Error processing mealData: $mealData", e)
                }
            }
        }
    }

    private fun createMealView(mealData: Map<String, Any>): View {
        // Инфлейтим разметку элемента
        val mealView = layoutInflater.inflate(R.layout.meal_item, null)
        Log.d("DEBUG", "mealData: $mealData")
        // Извлекаем данные из Map с использованием безопасных типов
        val totalCalories = (mealData["total_calories"] as? Float ?: 0f).toString()
        val totalProteins = (mealData["total_proteins"] as? Float ?: 0f).toString()
        val totalFats = (mealData["total_fats"] as? Float ?: 0f).toString()
        val totalCarbs = (mealData["total_carbs"] as? Float ?: 0f).toString()
        val createdAtTimestamp = mealData["created_at"] as? Long ?: System.currentTimeMillis()
        val foodName = mealData["food_name"] as? String ?: "No Name"
        val quantity = (mealData["quantity"]).toString()


        // Форматируем дату
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val createdAt = dateFormat.format(Date(createdAtTimestamp))

        // Заполняем данные в разметке
        mealView.findViewById<TextView>(R.id.meal_name).text = foodName
        mealView.findViewById<TextView>(R.id.meal_weight).text = "Quantity:$quantity" // Можно передать реальный вес, если он есть в данных
аф
        // Отображаем питательные вещества
        mealView.findViewById<TextView>(R.id.proteins).text = "Proteins\n$totalProteins"
        mealView.findViewById<TextView>(R.id.fats).text = "Fats\n$totalFats"
        mealView.findViewById<TextView>(R.id.carbohydrates).text = "Carbo\n$totalCarbs"
        mealView.findViewById<TextView>(R.id.calories).text = "Calories\n$totalCalories"


        return mealView
    }

}
