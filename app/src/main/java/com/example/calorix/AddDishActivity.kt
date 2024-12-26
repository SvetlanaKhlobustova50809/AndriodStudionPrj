package com.example.calorix

import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class AddDishActivity : AppCompatActivity() {

    private lateinit var bottomNavHome: ImageButton
    private lateinit var bottomNavDishes: ImageButton
    private lateinit var bottomNavMeals: ImageButton
    private lateinit var bottomNavProfile: ImageButton
    private lateinit var updateButton: Button

    private val selectedColor = Color.parseColor("#6200EE")
    private val defaultColor = Color.parseColor("#9E9E9E")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_dish)

        //Получаем данные со входа
        val userId= intent.getIntExtra("USERID", -1)
        val user_info = DatabaseHelper(this@AddDishActivity).getUserById(userId)
        Log.d("Registration", "$user_info")
        val user_name = findViewById<TextView>(R.id.name_label)
        user_name.text = (user_info["user_name"] as? String).toString()
        val user_email = findViewById<TextView>(R.id.email_label)
        user_email.text = (user_info["email"] as? String).toString()

        val radioMale: RadioButton = findViewById(R.id.radio_male)
        val radioFemale: RadioButton = findViewById(R.id.radio_female)
        val userGender = (user_info["gender"] as? String).toString()
        if (userGender == "Мужской") {
            radioMale.isChecked = true
        }
        if (userGender == "Женский") {
            radioMale.isChecked = true
        }



        // Инициализация кнопок
        updateButton = findViewById(R.id.update_button)
        bottomNavHome = findViewById(R.id.nav_home)
        bottomNavDishes = findViewById(R.id.nav_dishes)
        bottomNavMeals = findViewById(R.id.nav_meals)
        bottomNavProfile = findViewById(R.id.nav_profile)

        setSelectedButton(bottomNavHome)

        // Обработчики для кнопок
        bottomNavDishes.setOnClickListener {
            val intent = Intent(this@AddDishActivity, MainActivity::class.java)
            intent.putExtra("USERID", userId)
            startActivity(intent)
        }
        bottomNavHome.setOnClickListener {
        }
        bottomNavMeals.setOnClickListener {
            val intent = Intent(this@AddDishActivity, AddMealActivity::class.java)
            intent.putExtra("USERID", userId)
            startActivity(intent)
        }
        bottomNavProfile.setOnClickListener {
            val intent = Intent(this@AddDishActivity, ChatActivity::class.java)
            intent.putExtra("USERID", userId)
            startActivity(intent)
        }

        // Обработчик нажатия на кнопку "Зарегестрироваться"
        updateButton.setOnClickListener {
            val name = user_name.text.toString()
            val email = user_email.text.toString()

            val gender = when {
                radioMale.isChecked -> "Мужской"
                radioFemale.isChecked -> "Женский"
                else -> ""
            }

            val age = findViewById<AutoCompleteTextView>(R.id.age_dropdown).text.toString()
            val height = findViewById<AutoCompleteTextView>(R.id.height_dropdown).text.toString()
            val weight = findViewById<AutoCompleteTextView>(R.id.weight_dropdown).text.toString()
            val password = findViewById<EditText>(R.id.password_label).text.toString()
            val repeatPassword = findViewById<EditText>(R.id.repeat_password_label).text.toString()

            // Проверка, чтобы все обязательные поля были заполнены
            if (name.isNotEmpty() && email.isNotEmpty() && gender.isNotEmpty()) {

                // Проверка на совпадение паролей
                if (password == repeatPassword) {
                    val isUpdated = DatabaseHelper(this).editUser(
                        userId, name, email, gender, age.takeIf { it.isNotEmpty() },
                        height.takeIf { it.isNotEmpty() }, weight.takeIf { it.isNotEmpty() },
                        password.takeIf { it.isNotEmpty() }
                    )

                    // Проверка, обновились ли данные
                    if (isUpdated) {
                        Toast.makeText(this, "Данные обновлены", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Ошибка при обновлении данных", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Показываем сообщение, если обязательные поля не заполнены
                Toast.makeText(this, "Пожалуйста, заполните все обязательные поля", Toast.LENGTH_SHORT).show()
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
}
