package com.example.calorix
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.calorix.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var loginEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Инициализация элементов
        loginEditText = findViewById(R.id.loginEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)

        // Кнопка Войти
        loginButton.setOnClickListener {
            val login = loginEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (login.isNotEmpty() && password.isNotEmpty()) {
                checkLogin(login, password)
            } else {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }

        // Кнопка Регистрация
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkLogin(username: String, password: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Здесь вы должны проверить в базе данных SQLite, существует ли такой пользователь
            val user = DatabaseHelper(this@LoginActivity).getUserByUsernameAndPassword(username, password)

            withContext(Dispatchers.Main) {
                if (user != null) {
                    // Если пользователь найден, переход на главную страницу
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish() // Закрытие текущей активности
                } else {
                    // Если нет, выводим сообщение об ошибке
                    Toast.makeText(this@LoginActivity, "Неверный логин или пароль", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
