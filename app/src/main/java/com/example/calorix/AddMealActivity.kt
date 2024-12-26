package com.example.calorix

import android.provider.MediaStore
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import kotlinx.coroutines.*
import androidx.lifecycle.lifecycleScope
import okhttp3.FormBody


class AddMealActivity : AppCompatActivity() {

    private lateinit var bottomNavHome: ImageButton
    private lateinit var bottomNavDishes: ImageButton
    private lateinit var bottomNavMeals: ImageButton
    private lateinit var bottomNavProfile: ImageButton

    private val selectedColor = Color.parseColor("#6200EE")
    private val defaultColor = Color.parseColor("#9E9E9E")

    companion object {
        const val PICK_IMAGE_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_meal)

        val userId = intent.getIntExtra("USERID", -1)

        val searchRecipe = findViewById<AutoCompleteTextView>(R.id.search_recipe)
        val textInputArea = findViewById<EditText>(R.id.text_input_area)
        val addFoodButton = findViewById<Button>(R.id.add_food_button)
        val addFoodByPhotoButton = findViewById<Button>(R.id.add_by_photo_button)

        val foodNames  = DatabaseHelper(this@AddMealActivity).getAllFoodNames()

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, foodNames)
        searchRecipe.setAdapter(adapter)
        searchRecipe.setOnItemClickListener { _, _, position, _ ->
            val selectedFood = adapter.getItem(position)
        }


        // Инициализация кнопок
        bottomNavHome = findViewById(R.id.nav_home)
        bottomNavDishes = findViewById(R.id.nav_dishes)
        bottomNavMeals = findViewById(R.id.nav_meals)
        bottomNavProfile = findViewById(R.id.nav_profile)

        setSelectedButton(bottomNavMeals)

        // Обработчики для кнопок
        bottomNavDishes.setOnClickListener {
            val intent = Intent(this@AddMealActivity, MainActivity::class.java)
            intent.putExtra("USERID", userId)
            startActivity(intent)
        }
        bottomNavHome.setOnClickListener {
            val intent = Intent(this@AddMealActivity, AddDishActivity::class.java)
            intent.putExtra("USERID", userId)
            startActivity(intent)
        }
        bottomNavMeals.setOnClickListener {
        }
        bottomNavProfile.setOnClickListener {
            val intent = Intent(this@AddMealActivity, ChatActivity::class.java)
            intent.putExtra("USERID", userId)
            startActivity(intent)
        }

        // Установите обработчик клика для кнопки "Add"
        addFoodButton.setOnClickListener {
            val foodName = searchRecipe.text.toString().trim()

            // Проверяем, что название не пустое
            if (foodName.isEmpty()) {
                Toast.makeText(this, "Please enter a food name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (DatabaseHelper(this@AddMealActivity).isFoodExists(foodName)) {
                Toast.makeText(this, "Food with this name already exists", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val inputText = textInputArea.text.toString().trim()
            val foodDetails = parseFoodDetails(inputText)

            val isAdded = DatabaseHelper(this@AddMealActivity).addFood(
                foodName = foodName,
                servingSize = foodDetails["servingSize"]?.toFloatOrNull(),
                calories = foodDetails["calories"]?.toFloatOrNull(),
                proteins = foodDetails["proteins"]?.toFloatOrNull(),
                fats = foodDetails["fats"]?.toFloatOrNull(),
                carbs = foodDetails["carbs"]?.toFloatOrNull(),
                fiber = foodDetails["fiber"]?.toFloatOrNull(),
                sugar = foodDetails["sugar"]?.toFloatOrNull(),
                category = foodDetails["category"]
            )

            if (isAdded) {
                Toast.makeText(this, "Food added successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to add food", Toast.LENGTH_SHORT).show()
            }
        }

        addFoodByPhotoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            if (selectedImageUri != null) {
                val picturePath = getPathFromUri(selectedImageUri)
                if (picturePath != null) {
                    lifecycleScope.launch {
                        // Launching a coroutine to handle the network request
                        val imgurLink =
                            uploadImageToImgurAsync(this@AddMealActivity, selectedImageUri, "ec4e929843e23b0")
                        if (imgurLink != null) {
//                            Toast.makeText(
//                                this@AddMealActivity,
//                                "Image uploaded",
//                                Toast.LENGTH_LONG
//                            ).show()
                            println(imgurLink)
                            fetchResponseAsync(imgurLink)
                        } else {
                            // Use a default image URL if upload fails
                            Toast.makeText(
                                this@AddMealActivity,
                                "Failed to upload image to Imgur",
                                Toast.LENGTH_LONG
                            ).show()
                            fetchResponseAsync("https://i.imgur.com/AprMtUi.jpeg")
                        }
                    }
                } else {
                    Toast.makeText(this, "Failed to get file path from URI", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun fetchResponseAsync(imgurLink: String) {
        lifecycleScope.launch {
            val response = callFlaskApiAsync(imgurLink)
            response?.let {
                if (DatabaseHelper(this@AddMealActivity).isFoodExists(response)) {
                    Toast.makeText(this@AddMealActivity, "Food with this name already exists", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Разбираем текст (пример: ключ: значение)
                val foodDetails = parseFoodDetails(response)

                val isAdded = DatabaseHelper(this@AddMealActivity).addFood(
                    foodName = response,
                    servingSize = foodDetails["servingSize"]?.toFloatOrNull(),
                    calories = foodDetails["calories"]?.toFloatOrNull(),
                    proteins = foodDetails["proteins"]?.toFloatOrNull(),
                    fats = foodDetails["fats"]?.toFloatOrNull(),
                    carbs = foodDetails["carbs"]?.toFloatOrNull(),
                    fiber = foodDetails["fiber"]?.toFloatOrNull(),
                    sugar = foodDetails["sugar"]?.toFloatOrNull(),
                    category = foodDetails["category"]
                )

                if (isAdded) {
                    Toast.makeText(this@AddMealActivity, "Response received: $it", Toast.LENGTH_LONG).show()
                    Toast.makeText(this@AddMealActivity, "Food added successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@AddMealActivity, "Failed to add food", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(this@AddMealActivity, "Failed to get response from API", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getPathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(projection[0])
                return cursor.getString(columnIndex)
            }
        }
        return null
    }

    suspend fun callFlaskApiAsync(imageUrl: String): String? {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val mediaType = "application/json".toMediaTypeOrNull()
            val requestBody = """{"image_url": "$imageUrl"}""".toRequestBody(mediaType)

            val request = Request.Builder()
                .url("http://10.0.2.2:8080/predict")
                .post(requestBody)
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    val responseData = response.body?.string()
                    if (response.isSuccessful && responseData != null) {
                        val json = JSONObject(responseData)
                        return@withContext json.getString("predicted_concepts")
                    } else {
                        println("Failed to connect: ${response.code}")
                        return@withContext null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Exception: ${e.message}")
                return@withContext null
            }
        }
    }


    suspend fun uploadImageToImgurAsync(context: Context, imageUri: Uri, clientId: String): String? {
        return withContext(Dispatchers.IO) {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(imageUri)
            val imageBytes = inputStream?.readBytes()
            inputStream?.close()

            if (imageBytes == null) return@withContext null

            val imageBase64 = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)

            val client = OkHttpClient.Builder().build()

            val requestBody = FormBody.Builder()
                .add("image", imageBase64)
                .add("type", "base64")
                .build()

            val request = Request.Builder()
                .url("https://api.imgur.com/3/image")
                .addHeader("Authorization", "Client-ID $clientId")
                .post(requestBody)
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        println("Failed response code: ${response.code}")
                        return@withContext null
                    }

                    val responseString = response.body?.string() ?: return@withContext null
                    val jsonObject = JSONObject(responseString)
                    val dataObject = jsonObject.optJSONObject("data")
                    return@withContext dataObject?.optString("link")
                }
            } catch (e: IOException) {
                e.printStackTrace()
                println("IOException occurred: ${e.message}")
                return@withContext null
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

    private fun parseFoodDetails(input: String): Map<String, String> {
        val details = mutableMapOf<String, String>()

        // Разделяем текст на строки
        val lines = input.split("\n")
        for (line in lines) {
            val parts = line.split(":").map { it.trim() }
            if (parts.size == 2) {
                details[parts[0].lowercase()] = parts[1]
            }
        }

        return details
    }
}