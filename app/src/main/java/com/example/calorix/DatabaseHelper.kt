package com.example.calorix

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "example.db"
        private const val DATABASE_VERSION = 1
        private const val TAG = "DatabaseHelper"
    }

    private val databasePath: String = context.getDatabasePath(DATABASE_NAME).absolutePath

    init {
        // Проверяем, существует ли база данных
        val dbFile = File(databasePath)
        if (!dbFile.exists()) {
            copyDatabaseFromRaw(context)
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Не нужно ничего создавать, так как база данных предустановлена
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Здесь можно обновлять структуру базы данных при необходимости
    }

    private fun copyDatabaseFromRaw(context: Context) {
        try {
            val inputStream: InputStream = context.resources.openRawResource(R.raw.example) // Замените "example" на имя вашего файла в raw
            val outputFile = File(databasePath)
            val outputStream = FileOutputStream(outputFile)

            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            Log.d(TAG, "Database copied successfully!")
        } catch (e: Exception) {
            Log.e(TAG, "Error copying database", e)
        }
    }

    fun getUserByUsernameAndPassword(username: String, password: String): User? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM users WHERE user_name = ? AND password_hash = ?",
            arrayOf(username, password)
        )
        return if (cursor.moveToFirst()) {
            val user = User(
                cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("user_name")),
                cursor.getString(cursor.getColumnIndexOrThrow("email"))
            )
            cursor.close()
            user
        } else {
            cursor.close()
            null
        }
    }

    fun addUser(
        username: String, password: String, email: String,
        age: String, gender: String, weight: String, height: String,
        activityLevel: String, goal: String
    ): Boolean {
        val db = this.writableDatabase
        val values = android.content.ContentValues().apply {
            put("user_name", username)
            put("password_hash", password) // Лучше использовать хэш пароля
            put("email", email)

            // Добавляем только те поля, которые не пустые
            if (age.isNotEmpty()) {
                put("age", age)
            }
            if (gender.isNotEmpty()) {
                put("gender", gender)
            }
            if (weight.isNotEmpty()) {
                put("weight", weight)
            }
            if (height.isNotEmpty()) {
                put("height", height)
            }
            if (activityLevel.isNotEmpty()) {
                put("activity_level", activityLevel)
            }
            if (goal.isNotEmpty()) {
                put("goal", goal)
            }
        }

        val result = db.insert("users", null, values)
        return result != -1L
    }

    fun getMealLogSummary(userId: Int, date: String): Map<String, Float> {
        val db = this.readableDatabase
        Log.d("Database", "Starting getMealLogSummary with userId: $userId, date: $date")

        // Логируем все записи из таблицы meals для проверки
        Log.d("Database", "Fetching all records from meals table for debugging")

        val queryAllMeals = "SELECT * FROM meals"
        val cursorAllMeals = db.rawQuery(queryAllMeals, null)

        if (cursorAllMeals.moveToFirst()) {
            do {
                val mealId = cursorAllMeals.getInt(cursorAllMeals.getColumnIndexOrThrow("meal_id"))
                val mealName = cursorAllMeals.getString(cursorAllMeals.getColumnIndexOrThrow("meal_name"))
                val createdAt = cursorAllMeals.getString(cursorAllMeals.getColumnIndexOrThrow("created_at"))
                val updatedAt = cursorAllMeals.getString(cursorAllMeals.getColumnIndexOrThrow("updated_at"))

                Log.d("Database", "Meal record - meal_id: $mealId, meal_name: $mealName, created_at: $createdAt, updated_at: $updatedAt")
            } while (cursorAllMeals.moveToNext())
        } else {
            Log.d("Database", "No records found in meals table.")
        }

        cursorAllMeals.close()

        // Теперь выполняем основной запрос для получения суммарных значений
        val query = """
        SELECT 
            SUM(ml.total_calories) AS total_calories,
            SUM(ml.total_proteins) AS total_proteins,
            SUM(ml.total_fats) AS total_fats,
            SUM(ml.total_carbs) AS total_carbs
        FROM meal_logs ml
        INNER JOIN meals m ON ml.meal_id = m.meal_id
        WHERE ml.user_id = ? AND DATE(datetime(m.created_at / 1000, 'unixepoch')) = ?   
    """

        Log.d("Database", "Executing query: $query")
        Log.d("Database", "Query parameters: user_id = $userId, date = $date")

        val cursor = db.rawQuery(query, arrayOf(userId.toString(), date))
        val result = mutableMapOf<String, Float>()

        if (cursor.moveToFirst()) {
            val totalCalories = cursor.getFloat(cursor.getColumnIndexOrThrow("total_calories"))
            val totalProteins = cursor.getFloat(cursor.getColumnIndexOrThrow("total_proteins"))
            val totalFats = cursor.getFloat(cursor.getColumnIndexOrThrow("total_fats"))
            val totalCarbs = cursor.getFloat(cursor.getColumnIndexOrThrow("total_carbs"))

            Log.d("Database", "Query result - total_calories: $totalCalories, total_proteins: $totalProteins, total_fats: $totalFats, total_carbs: $totalCarbs")

            result["total_calories"] = totalCalories
            result["total_proteins"] = totalProteins
            result["total_fats"] = totalFats
            result["total_carbs"] = totalCarbs
        } else {
            Log.d("Database", "No data found for the given userId and date.")
        }

        cursor.close()
        Log.d("Database", "getMealLogSummary completed with result: $result")
        return result
    }

    fun getMealsForDate(userId: Int, currentDate: String): Map<String, List<Map<String, Any>>> {
        val db = this.readableDatabase
        Log.d("Database", "Starting getMealsForDate with userId: $userId, currentDate: $currentDate")

        // SQL-запрос для извлечения данных о приёмах пищи
        val query = """
        SELECT 
            m.meal_name,
            mf.meal_food_id,
            f.food_name,
            mf.quantity,
            ml.total_calories,
            ml.total_proteins,
            ml.total_fats,
            ml.total_carbs,
            m.created_at
        FROM meal_logs ml
        INNER JOIN meals m ON ml.meal_id = m.meal_id
        INNER JOIN meal_foods mf ON m.meal_id = mf.meals_id
        INNER JOIN foods f ON mf.foods_id = f.food_id
        WHERE ml.user_id = ? 
        AND DATE(datetime(m.created_at / 1000, 'unixepoch')) = ?
    """

        Log.d("Database", "Executing query: $query")
        Log.d("Database", "Query parameters: user_id = $userId, currentDate = $currentDate")

        val cursor = db.rawQuery(query, arrayOf(userId.toString(), currentDate))
        val result = mutableMapOf<String, MutableList<Map<String, Any>>>()

        if (cursor.moveToFirst()) {
            do {
                val mealName = cursor.getString(cursor.getColumnIndexOrThrow("meal_name"))
                val totalCalories = cursor.getFloat(cursor.getColumnIndexOrThrow("total_calories"))
                val totalProteins = cursor.getFloat(cursor.getColumnIndexOrThrow("total_proteins"))
                val totalFats = cursor.getFloat(cursor.getColumnIndexOrThrow("total_fats"))
                val totalCarbs = cursor.getFloat(cursor.getColumnIndexOrThrow("total_carbs"))
                val loggedAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
                val foodName = cursor.getString(cursor.getColumnIndexOrThrow("food_name"))

                Log.d("Database", "Meal record - meal_name: $mealName, total_calories: $totalCalories, total_proteins: $totalProteins, total_fats: $totalFats, total_carbs: $totalCarbs, logged_at: $loggedAt")

                // Добавляем данные в результат, группируя по meal_name
                val mealData = mapOf(
                    "total_calories" to totalCalories,
                    "total_proteins" to totalProteins,
                    "total_fats" to totalFats,
                    "total_carbs" to totalCarbs,
                    "created_at" to loggedAt,
                    "food_name" to foodName
                )

                if (result.containsKey(mealName)) {
                    result[mealName]?.add(mealData)
                } else {
                    result[mealName] = mutableListOf(mealData)
                }
            } while (cursor.moveToNext())
        } else {
            Log.d("Database", "No meals found for the given userId and date.")
        }

        cursor.close()
        Log.d("Database", "getMealsForDate completed with result: $result")
        return result
    }

    fun debugMealFoods() {
        val db = this.readableDatabase

        val query = """
        SELECT 
            mf.meal_food_id,
            mf.meals_id,
            mf.foods_id,
            mf.quantity
        FROM meal_foods mf
    """

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val mealFoodId = cursor.getInt(cursor.getColumnIndexOrThrow("meal_food_id"))
                val mealId = cursor.getInt(cursor.getColumnIndexOrThrow("meals_id"))
                val foodId = cursor.getInt(cursor.getColumnIndexOrThrow("foods_id"))
                val quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))

                Log.d("DatabaseDebug", "Meal Food - Meal Food ID: $mealFoodId, Meal ID: $mealId, Food ID: $foodId, Quantity: $quantity")

            } while (cursor.moveToNext())
        } else {
            Log.d("DatabaseDebug", "No records found in meal_foods ")
        }

        cursor.close()
    }



    fun getUserById(userId: Int): Map<String, Any> {
        val db = this.readableDatabase
        val query = """
    SELECT 
        user_id, 
        user_name, 
        email,
        age, 
        gender, 
        weight, 
        height
    FROM users
    WHERE user_id = ?
    """

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))
        val result = mutableMapOf<String, Any>()

        if (cursor.moveToFirst()) {
            result["user_id"] = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"))

            // Используем проверку на null для строковых значений
            result["user_name"] = cursor.getString(cursor.getColumnIndexOrThrow("user_name")) ?: "Unknown"
            result["email"] = cursor.getString(cursor.getColumnIndexOrThrow("email")) ?: "Unknown"
            result["age"] = cursor.getString(cursor.getColumnIndexOrThrow("age")) ?: "Unknown"  // дата может быть в строковом формате
            result["gender"] = cursor.getString(cursor.getColumnIndexOrThrow("gender")) ?: "Unknown"

            // Для числовых значений (weight, height), если null - возвращаем дефолтные значения
            result["weight"] = cursor.getDouble(cursor.getColumnIndexOrThrow("weight")).takeIf { !it.isNaN() } ?: 0.0
            result["height"] = cursor.getDouble(cursor.getColumnIndexOrThrow("height")).takeIf { !it.isNaN() } ?: 0.0
        }

        cursor.close()
        return result
    }

    // Функция для обновления данных пользователя
    fun editUser(
        userId: Int,
        name: String,
        email: String,
        gender: String,
        age: String?,
        height: String?,
        weight: String?,
        password: String?
    ): Boolean {
        // Создаем объект SQLiteDatabase для записи
        val db = this.writableDatabase

        // Хэшируем пароль (можно использовать любую библиотеку для хэширования паролей, например, bcrypt)
        val passwordHash = password ?: "" // Если пароль не был передан, используем пустую строку

        // Строка для SQL-запроса
        val values = ContentValues().apply {
            put("user_name", name)
            put("email", email)
            put("password_hash", passwordHash)

            // Добавляем только те значения, которые не null
            age?.let { put("age", it) }
            gender.takeIf { it.isNotEmpty() }?.let { put("gender", it) }
            height?.let { put("height", it) }
            weight?.let { put("weight", it) }
            put("updated_at", getCurrentDate()) // Текущая дата
        }

        // Выполняем запрос для обновления
        val rowsAffected = db.update("users", values, "user_id = ?", arrayOf(userId.toString()))

        // Закрываем базу данных
        db.close()

        // Возвращаем true, если хотя бы одна строка была обновлена, иначе false
        return rowsAffected > 0
    }

    fun addFood(
        foodName: String,
        servingSize: Float? = null,  // Параметры могут быть null, если их нет
        calories: Float? = null,
        proteins: Float? = null,
        fats: Float? = null,
        carbs: Float? = null,
        fiber: Float? = null,
        sugar: Float? = null,
        category: String? = null
    ): Boolean {
        // Создаем объект SQLiteDatabase для записи
        val db = this.writableDatabase

        // Строка для SQL-запроса
        val values = ContentValues().apply {
            put("food_name", foodName)
            servingSize?.let { put("serving_size", it) }
            calories?.let { put("calories", it) }
            proteins?.let { put("proteins", it) }
            fats?.let { put("fats", it) }
            carbs?.let { put("carbs", it) }
            fiber?.let { put("fiber", it) }
            sugar?.let { put("sugar", it) }
            category?.let { put("category", it) }
        }


        // Выполняем запрос для вставки данных в таблицу
        val foodId = db.insert("foods", null, values)

        // Закрываем базу данных
        db.close()

        // Возвращаем true, если вставка прошла успешно, иначе false
        return foodId != -1L
    }

    fun getAllFoodNames(): List<String> {
        val foodNames = mutableListOf<String>()
        val db = readableDatabase
        val query = "SELECT food_name FROM foods"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val foodName = cursor.getString(cursor.getColumnIndexOrThrow("food_name"))
                foodNames.add(foodName)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return foodNames
    }

    fun isFoodExists(foodName: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            "foods",              // Имя таблицы
            arrayOf("food_name"), // Столбцы для выборки
            "food_name = ?",      // Условие
            arrayOf(foodName),    // Аргумент для условия
            null,                  // Группировка
            null,                  // Сортировка
            null                   // Порядок сортировки
        )

        // Если курсор возвращает хотя бы одну строку, значит такой продукт существует
        val exists = cursor.count > 0
        cursor.close()
        db.close()

        return exists
    }

    // Функция для получения diet_id для пользователя
    fun getDietIdForUser(userId: Int): Int? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT diet_id FROM users WHERE user_id = ?",
            arrayOf(userId.toString())
        )

        if (cursor.moveToFirst()) {
            // Get the column index for "diet_id"
            val dietIdIndex = cursor.getColumnIndex("diet_id")

            // Check if the column index is valid (>= 0)
            if (dietIdIndex >= 0) {
                val dietId = cursor.getInt(dietIdIndex)
                cursor.close()
                db.close()
                return dietId
            }
        }

        cursor.close()
        db.close()
        return null  // Return null if the column or value is not found
    }


    // Функция для добавления записи в meal_logs
    // Основная функция с использованием новой функции
    fun addMealLog(foodName: String, quantity: Int, userId: Int, mealTime: String, mealType: String): Boolean {
        val db = this.writableDatabase
        Log.d("Database", "Starting addMealLog with foodName: $foodName, quantity: $quantity, userId: $userId, mealTime: $mealTime, mealType: $mealType")

        var mealId: Int? = getMealIdByNameAndDate(mealType)
        if (mealId == null) {
            Log.d("Database", "Meal not found, creating new meal for mealType: $mealType")
            mealId = addMeal(mealType)  // Функция addMeal не должна закрывать базу данных
            if (mealId == null) {
                Log.e("Database", "Failed to get or create meal for mealType: $mealType")
                db.close() // Закрываем базу данных, так как дальнейшие операции невозможны
                return false
            }
        }
        Log.d("Database", "MealId obtained: $mealId")

        val (isFound, foodData) = getFoodData(foodName)  // Эта функция также не должна закрывать базу данных
        if (!isFound) {
            Log.e("Database", "Food data not found for foodName: $foodName")
            db.close() // Закрываем базу данных, так как дальнейшие операции невозможны
            return false
        }

        val foodId = foodData["food_id"] as? Int ?: -1
        val calories = foodData["calories"] as? Float ?: 0f
        val proteins = foodData["proteins"] as? Float ?: 0f
        val fats = foodData["fats"] as? Float ?: 0f
        val carbs = foodData["carbs"] as? Float ?: 0f

        Log.d("Database", "Food data - foodId: $foodId, calories: $calories, proteins: $proteins, fats: $fats, carbs: $carbs")

        val totalCalories = calories * quantity
        val totalProteins = proteins * quantity
        val totalFats = fats * quantity
        val totalCarbs = carbs * quantity

        Log.d("Database", """
        Calculated values:
        total_calories: $totalCalories
        total_proteins: $totalProteins
        total_fats: $totalFats
        total_carbs: $totalCarbs
    """.trimIndent())

        Log.d("Database", """
        Preparing to insert meal log:
        user_id: $userId
        meal_id: $mealId
        meal_time: $mealTime
        total_calories: $totalCalories
        total_proteins: $totalProteins
        total_fats: $totalFats
        total_carbs: $totalCarbs
    """.trimIndent())

        val values = ContentValues().apply {
            put("user_id", userId)
            put("meal_id", mealId)
            put("diet_id", 1)  // diet_id 1
            put("meal_time", mealTime)
            put("total_calories", totalCalories)
            put("total_proteins", totalProteins)
            put("total_fats", totalFats)
            put("total_carbs", totalCarbs)
        }

        val valuesMealFood = ContentValues().apply {
            put("meals_id", mealId)
            put("foods_id", foodId)
            put("quantity", quantity)
        }
        db.insert("meal_foods", null, valuesMealFood)

        Log.d("Database", "Attempting to insert meal log with values: $values")

        try {
            // Вставка в таблицу
            val result = db.insert("meal_logs", null, values)

            if (result != -1L) {
                Log.d("Database", "Meal log inserted successfully")
                db.close() // Закрываем базу данных только после успешной вставки
                return true
            } else {
                Log.e("Database", "Failed to insert meal log, result: $result")
                db.close() // Закрываем базу данных в случае ошибки вставки
                return false
            }
        } catch (e: Exception) {
            Log.e("Database", "Error during insert operation", e)
            db.close() // Закрываем базу данных в случае ошибки
            return false
        }
    }


    fun getMealIdByNameAndDate(mealName: String): Int? {
        val db = this.readableDatabase
        Log.d("Database", "Getting mealId for mealName: $mealName")

        val cursor = db.rawQuery(
            "SELECT meal_id FROM meals WHERE meal_name = ? AND DATE(created_at) = DATE('now')",
            arrayOf(mealName)
        )

        if (cursor.moveToFirst()) {
            val mealIdIndex = cursor.getColumnIndex("meal_id")
            if (mealIdIndex >= 0) {
                val mealId = cursor.getInt(mealIdIndex)
                cursor.close()
                db.close()
                Log.d("Database", "Found mealId: $mealId for mealName: $mealName")
                return mealId
            }
        }

        Log.e("Database", "Meal not found for mealName: $mealName")
        cursor.close()
        return null
    }

    fun addMeal(mealName: String): Int? {
        val db = this.writableDatabase
        Log.d("Database", "Adding new meal with name: $mealName")

        val values = ContentValues().apply {
            put("meal_name", mealName)
            put("created_at", System.currentTimeMillis())  // Время в миллисекундах
        }

        val mealId = db.insert("meals", null, values)

        if (mealId != -1L) {
            Log.d("Database", "Successfully added new meal with mealId: $mealId")
            return mealId.toInt()
        } else {
            Log.e("Database", "Failed to add new meal")
            return null
        }
    }

    // Функция для получения данных о пище
    fun getFoodData(foodName: String): Pair<Boolean, Map<String, Any?>> {
        val db = this.readableDatabase

        try {
            val cursor = db.rawQuery(
                "SELECT food_id, calories, proteins, fats, carbs FROM foods WHERE food_name = ?",
                arrayOf(foodName)
            )
            if (cursor != null && cursor.moveToFirst()) {
                // Проверяем наличие всех нужных столбцов
                val foodIdIndex = cursor.getColumnIndex("food_id")
                val caloriesIndex = cursor.getColumnIndex("calories")
                val proteinsIndex = cursor.getColumnIndex("proteins")
                val fatsIndex = cursor.getColumnIndex("fats")
                val carbsIndex = cursor.getColumnIndex("carbs")

                if (foodIdIndex == -1 || caloriesIndex == -1 || proteinsIndex == -1 || fatsIndex == -1 || carbsIndex == -1) {
                    Log.e("Database", "One or more required columns are missing in the result")
                    cursor.close()
                    db.close()
                    return Pair(false, mapOf())
                }

                // Извлекаем данные, проверяя на NULL
                val foodData = mapOf(
                    "food_id" to cursor.getInt(foodIdIndex),
                    "calories" to if (!cursor.isNull(caloriesIndex)) cursor.getFloat(caloriesIndex) else 0f,
                    "proteins" to if (!cursor.isNull(proteinsIndex)) cursor.getFloat(proteinsIndex) else 0f,
                    "fats" to if (!cursor.isNull(fatsIndex)) cursor.getFloat(fatsIndex) else 0f,
                    "carbs" to if (!cursor.isNull(carbsIndex)) cursor.getFloat(carbsIndex) else 0f
                )
                cursor.close()
                return Pair(true, foodData)
            } else {
                cursor?.close()
                return Pair(false, mapOf())
            }
        } catch (e: Exception) {
            Log.e("Database", "Error fetching food data", e)
            return Pair(false, mapOf())
        }
    }




    // Функция для хэширования пароля (вы можете заменить на вашу реализацию)
    /*private fun hashPassword(password: String): String {
        // Пример хэширования с использованием SHA-256 или другой алгоритм
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val hashBytes = messageDigest.digest(password.toByteArray())
        return Base64.encodeToString(hashBytes, Base64.DEFAULT)
    }*/

    // Функция для преобразования строки даты в формат SQL
    private fun parseDate(age: String): String {
        // Преобразуем возраст в формат даты, например, yyyy-MM-dd
        // В вашем случае это зависит от того, как вводится дата (можно использовать SimpleDateFormat)
        return age // Пример. Сделайте преобразование из строки в формат даты, если нужно
    }

    // Функция для получения текущей даты в формате yyyy-MM-dd
    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }




}

// Класс для представления пользователя
data class User(
    val userId: Int,
    val userName: String,
    val email: String
)
