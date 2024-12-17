package com.example.calorix

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var genderEditText: EditText
    private lateinit var weightEditText: EditText
    private lateinit var heightEditText: EditText
    private lateinit var activityLevelEditText: EditText
    private lateinit var goalEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Инициализация элементов
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        emailEditText = findViewById(R.id.emailEditText)
        ageEditText = findViewById(R.id.ageEditText)
        genderEditText = findViewById(R.id.genderEditText)
        weightEditText = findViewById(R.id.weightEditText)
        heightEditText = findViewById(R.id.heightEditText)
        activityLevelEditText = findViewById(R.id.activityLevelEditText)
        goalEditText = findViewById(R.id.goalEditText)
        registerButton = findViewById(R.id.registerButton)
        backButton = findViewById(R.id.backButton)
        // Кнопка назад
        backButton.setOnClickListener {
            // Переходим на экран входа
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish() // Закрываем текущую активность
        }

        // Кнопка Регистрация
        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val email = emailEditText.text.toString()
            val age = ageEditText.text.toString()
            val gender = genderEditText.text.toString()
            val weight = weightEditText.text.toString()
            val height = heightEditText.text.toString()
            val activityLevel = activityLevelEditText.text.toString()
            val goal = goalEditText.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty() && email.isNotEmpty()) {
                registerUser(username, password, email, age, gender, weight, height, activityLevel, goal)
            } else {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(
        username: String, password: String, email: String,
        age: String, gender: String, weight: String, height: String,
        activityLevel: String, goal: String
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Добавление пользователя в базу данных
            val result = DatabaseHelper(this@RegisterActivity).addUser(
                username, password, email, age, gender, weight, height, activityLevel, goal
            )

            withContext(Dispatchers.Main) {
                if (result) {
                    // Переход на страницу входа
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@RegisterActivity, "Ошибка при регистрации", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
