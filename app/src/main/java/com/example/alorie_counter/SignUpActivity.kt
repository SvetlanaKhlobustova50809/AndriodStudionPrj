package com.example.calorie_counter

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.calorie_counter.R

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val createAccountButton = findViewById<Button>(R.id.create_account_button)
        createAccountButton.setOnClickListener {
            val intent = Intent(this, DiaryActivity::class.java)
            startActivity(intent)
        }
    }
}