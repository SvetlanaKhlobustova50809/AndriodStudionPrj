package com.example.calorix

import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import java.text.SimpleDateFormat
import java.util.Calendar
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
        val userName = intent.getStringExtra("USERNAME")

        // Устанавливаем приветствие в TextView
        val greetingText = findViewById<TextView>(R.id.greetingText)
        greetingText.text = "Hi, $userName"

        //Устанавливаем текущую дату
        val date = findViewById<TextView>(R.id.tomorrow_header)
        val currentDate = Calendar.getInstance().time
        var dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDate)
        date.text = formattedDate

        //Считаем данные за день
        val userId= intent.getIntExtra("USERID", -1)
        dateFormat = SimpleDateFormat("yyyy-mm-dd", Locale.getDefault())
        val date_for_sql = dateFormat.format(currentDate)
        val mealSummary = DatabaseHelper(this@MainActivity).getMealLogSummary(userId, date_for_sql)
        val calories = findViewById<TextView>(R.id.calories_val)
        calories.text = (mealSummary["total_calories"] as? Float ?: 0f).toString()
        val proteins = findViewById<TextView>(R.id.proteins_val)
        proteins.text = (mealSummary["total_proteins"] as? Float ?: 0f).toString()
        val fats = findViewById<TextView>(R.id.fats_val)
        fats.text = (mealSummary["total_fats"] as? Float ?: 0f).toString()
        val carbs = findViewById<TextView>(R.id.carbo_val)
        carbs.text = (mealSummary["total_carbs"] as? Float ?: 0f).toString()

        val addFoodBreakfastButton = findViewById<Button>(R.id.add_food_breakfast_button)
        val addFoodLunchButton = findViewById<Button>(R.id.add_food_lunch_button)
        val addFoodDinnerButton = findViewById<Button>(R.id.add_food_dinner_button)

        // Обработчики нажатий на кнопки
        addFoodBreakfastButton.setOnClickListener {
            // Показываем диалог для завтрака
            showAddFoodDialog("Breakfast")
        }

        addFoodLunchButton.setOnClickListener {
            // Показываем диалог для обеда
            showAddFoodDialog("Lunch")
        }

        addFoodDinnerButton.setOnClickListener {
            // Показываем диалог для ужина
            showAddFoodDialog("Dinner")
        }

        // Инициализация кнопок
        bottomNavHome = findViewById(R.id.nav_home)
        bottomNavDishes = findViewById(R.id.nav_dishes)
        bottomNavMeals = findViewById(R.id.nav_meals)
        bottomNavProfile = findViewById(R.id.nav_profile)

        setSelectedButton(bottomNavDishes)
        // Обработчики длыя кнопок
        bottomNavDishes.setOnClickListener {
            // Переход на главную активность
        }
        bottomNavHome.setOnClickListener {
            // Переход на активность добавления блюда
            val intent = Intent(this@MainActivity, AddDishActivity::class.java)
            intent.putExtra("USERID", userId)
            startActivity(intent)
        }
        bottomNavMeals.setOnClickListener {
            // Переход на активность добавления приема пищи
            val intent = Intent(this@MainActivity, AddMealActivity::class.java)
            startActivity(intent)
        }
        bottomNavProfile.setOnClickListener {
            // Переход на активность профиля
            val intent = Intent(this@MainActivity, ProgressActivity::class.java)
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

    private fun showAddFoodDialog(mealType: String) {

        // Получаем список всех блюд из базы данных
        val mealsList = DatabaseHelper(this@MainActivity).getAllFoodNames()

        // Инфлейтируем вид для диалога
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_food, null)

        // Находим элементы в диалоговом окне
        val mealNameEditText = dialogView.findViewById<AutoCompleteTextView>(R.id.meal_name_edit_text)
        val servingSizeEditText = dialogView.findViewById<EditText>(R.id.serving_size_edit_text)

        // Настроим адаптер для AutoCompleteTextView с данными из базы данных
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, mealsList)
        mealNameEditText.setAdapter(adapter)

        // Получаем userId из Intent
        val userId = intent.getIntExtra("USERID", -1)

        // Получаем текущую дату
        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDate)

        // Создаем AlertDialog
        val dialog = AlertDialog.Builder(this)
            .setTitle("Add $mealType")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->

                // Получаем введенное название блюда и количество порций
                val mealName = mealNameEditText.text.toString()
                val servingSize = servingSizeEditText.text.toString().toIntOrNull() ?: 1 // По умолчанию 1 порция, если не указано

                // Вызов функции для добавления в журнал приема пищи
                val success = DatabaseHelper(this@MainActivity).addMealLog(mealName, servingSize, userId, formattedDate, mealType)

                if (success) {
                    Log.d("AddFoodDialog", "Meal successfully added: $mealName, $mealType, Quantity: $servingSize")
                } else {
                    Log.d("AddFoodDialog", "Failed to add meal.")
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        // Показываем диалог
        dialog.show()
    }
}
