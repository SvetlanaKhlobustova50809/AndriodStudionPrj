package com.example.calorix

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import android.content.Intent
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var loginText: TextView
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var repeatPasswordEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var weightEditText: EditText
    private lateinit var heightEditText: EditText
    private lateinit var activityLevelSpinner: Spinner
    private lateinit var goalSpinner: Spinner
    private lateinit var createAccountButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Инициализация элементов
        loginText = findViewById(R.id.loginText)
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        repeatPasswordEditText = findViewById(R.id.repeatPasswordEditText)
        emailEditText = findViewById(R.id.emailEditText)
        ageEditText = findViewById(R.id.ageEditText)
        genderSpinner = findViewById(R.id.sexSpinner)
        weightEditText = findViewById(R.id.weightEditText)
        heightEditText = findViewById(R.id.heightEditText)
        activityLevelSpinner = findViewById(R.id.activityLevelSpinner)
        goalSpinner = findViewById(R.id.goalSpinner)
        createAccountButton = findViewById(R.id.createAccountButton)

        // Устанавливаем обработчик клика для TextView "Already have an account? Log In"
        loginText.setOnClickListener {
            // Переход на экран входа
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()  // Закрываем текущую активность
        }

        // Адаптер для спиннера уровня активности (от 0 до 10)
        val activityLevels = (0..10).map { it.toString() }
        val activityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, activityLevels)
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        activityLevelSpinner.adapter = activityAdapter

        // Адаптер для спиннера цели
        val goals = listOf("Оставаться в форме", "Похудеть", "Спорт")
        val goalAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, goals)
        goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        goalSpinner.adapter = goalAdapter

        val genders = listOf("Мужской", "Женский")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genders)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = genderAdapter

        // Кнопка для создания аккаунта
        createAccountButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val repeatPassword = repeatPasswordEditText.text.toString()
            val email = emailEditText.text.toString()
            val age = ageEditText.text.toString()
            val gender = genderSpinner.selectedItem.toString()
            val weight = weightEditText.text.toString()
            val height = heightEditText.text.toString()
            val activityLevel = activityLevelSpinner.selectedItem.toString() // Уровень активности
            val goal = goalSpinner.selectedItem.toString() // Цель

            // Проверка паролей и обязательных полей
            if (username.isNotEmpty() && password.isNotEmpty() && email.isNotEmpty()) {
                if (password == repeatPassword) {
                    // Регистрируем пользователя
                    registerUser(username, password, email, age, gender, weight, height, activityLevel, goal)
                } else {
                    Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Заполните имя пользователя, пароль и почту", Toast.LENGTH_SHORT).show()
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
