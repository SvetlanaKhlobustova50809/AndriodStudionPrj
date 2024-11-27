package com.example.calorie_counter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.calorie_counter.R

class RecipeActivity : NavActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe)
    }
}