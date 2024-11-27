package com.example.calorie_counter

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.calorie_counter.R
import com.google.android.material.bottomnavigation.BottomNavigationView


open class NavActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_diary -> {
                    val intent = Intent(this, DiaryActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_food -> {
                    val intent = Intent(this, FoodActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_stats -> {
                    val intent = Intent(this, ProgressActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}
