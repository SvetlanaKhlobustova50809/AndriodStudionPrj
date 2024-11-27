package com.example.calorie_counter

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.calorie_counter.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class DiaryActivity : NavActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary)

    }
}