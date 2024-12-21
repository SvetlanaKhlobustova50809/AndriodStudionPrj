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
        val query = """
        SELECT 
            SUM(total_calories) AS total_calories,
            SUM(total_proteins) AS total_proteins,
            SUM(total_fats) AS total_fats,
            SUM(total_carbs) AS total_carbs
        FROM meal_logs
        WHERE user_id = ? AND DATE(meal_time) = ?
    """

        val cursor = db.rawQuery(query, arrayOf(userId.toString(), date))
        val result = mutableMapOf<String, Float>()

        if (cursor.moveToFirst()) {
            result["total_calories"] = cursor.getFloat(cursor.getColumnIndexOrThrow("total_calories"))
            result["total_proteins"] = cursor.getFloat(cursor.getColumnIndexOrThrow("total_proteins"))
            result["total_fats"] = cursor.getFloat(cursor.getColumnIndexOrThrow("total_fats"))
            result["total_carbs"] = cursor.getFloat(cursor.getColumnIndexOrThrow("total_carbs"))
        }

        cursor.close()
        return result
    }

    //Функция для добавления приёма пищи и воды (для тех самых кнопок на главной странице)
    fun addMealLog(userId: Int, mealId: Int, dietId: Int, mealTime: String?, totalCalories: Float?, totalProteins: Float?, totalFats: Float?, totalCarbs: Float?) {
        val db = this.writableDatabase

        // Создаем объект ContentValues, куда будем добавлять только ненулевые значения
        val values = android.content.ContentValues()

        // Обязательно добавляем все поля, которые передаются
        values.put("user_id", userId)
        values.put("meal_id", mealId)
        values.put("diet_id", dietId)

        // Если время приема пищи передано, добавляем его в базу
        mealTime?.let { values.put("meal_time", it) }

        // Если значение не null, добавляем его в базу
        totalCalories?.takeIf { it != 0f }?.let { values.put("total_calories", it) }
        totalProteins?.takeIf { it != 0f }?.let { values.put("total_proteins", it) }
        totalFats?.takeIf { it != 0f }?.let { values.put("total_fats", it) }
        totalCarbs?.takeIf { it != 0f }?.let { values.put("total_carbs", it) }

        // Выполняем вставку данных в таблицу meal_logs
        val result = db.insert("meal_logs", null, values)

        // Проверяем, был ли успешен результат (результат -1 означает ошибку)
        if (result == -1L) {
            Log.e("DatabaseHelper", "Failed to insert meal log")
        } else {
            Log.d("DatabaseHelper", "Meal log inserted successfully")
        }
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
    fun addMealLog(foodName: String, quantity: Int, userId: Int, mealTime: String, mealType: String): Boolean {
        val db = this.writableDatabase

        Log.d("Database", "Starting addMealLog with foodName: $foodName, quantity: $quantity, userId: $userId, mealTime: $mealTime, mealType: $mealType")

        // Проверяем, существует ли прием пищи с указанным mealType (завтрак, обед, ужин) и дата создания соответствует текущей
        var mealId: Int? = getMealIdByNameAndDate(mealType)

        if (mealId == null) {
            Log.d("Database", "Meal not found for mealType: $mealType, adding new meal")
            mealId = addMeal(mealType)
        }

        if (mealId == null) {
            Log.e("Database", "Failed to get or create meal for mealType: $mealType")
            db.close()
            return false
        }

        Log.d("Database", "MealId obtained: $mealId")

        // Получаем food_id для указанного foodName
        val cursor = try {
            Log.d("Database", "Executing query: SELECT food_id, calories, proteins, fats, carbs FROM foods WHERE food_name = $foodName")
            db.rawQuery("SELECT food_id, calories, proteins, fats, carbs FROM foods WHERE food_name = ?", arrayOf(foodName))
        } catch (e: Exception) {
            Log.e("Database", "Error executing rawQuery", e)
            null
        }

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                Log.d("Database", "Data found for food: $foodName")

                // Получаем индексы для всех столбцов
                val foodIdIndex = cursor.getColumnIndex("food_id")
                val caloriesIndex = cursor.getColumnIndex("calories")
                val proteinsIndex = cursor.getColumnIndex("proteins")
                val fatsIndex = cursor.getColumnIndex("fats")
                val carbsIndex = cursor.getColumnIndex("carbs")

                // Проверяем, что индексы валидны (>= 0)
                if (foodIdIndex >= 0 && caloriesIndex >= 0 && proteinsIndex >= 0 && fatsIndex >= 0 && carbsIndex >= 0) {
                    // Обрабатываем возможные NULL значения для каждого столбца
                    val foodId = if (!cursor.isNull(foodIdIndex)) cursor.getInt(foodIdIndex) else -1
                    val calories = if (!cursor.isNull(caloriesIndex)) cursor.getFloat(caloriesIndex) else 0f
                    val proteins = if (!cursor.isNull(proteinsIndex)) cursor.getFloat(proteinsIndex) else 0f
                    val fats = if (!cursor.isNull(fatsIndex)) cursor.getFloat(fatsIndex) else 0f
                    val carbs = if (!cursor.isNull(carbsIndex)) cursor.getFloat(carbsIndex) else 0f

                    // Логирование значений
                    Log.d("Database", "Food data found - foodId: $foodId, calories: $calories, proteins: $proteins, fats: $fats, carbs: $carbs")

                    // Вычисляем общие значения на основе количества порций
                    val totalCalories = calories * quantity
                    val totalProteins = proteins * quantity
                    val totalFats = fats * quantity
                    val totalCarbs = carbs * quantity

                    // Получаем diet_id для пользователя (если оно есть)
                    val dietId = getDietIdForUser(userId)

                    Log.d("Database", "DietId for user $userId: $dietId")

                    // Создаем объект для записи в таблицу meal_logs
                    val values = ContentValues().apply {
                        put("user_id", userId)
                        put("meal_id", mealId)
                        put("diet_id", dietId ?: -1) // Если диета не найдена, используем -1
                        put("meal_time", mealTime)
                        put("total_calories", totalCalories)
                        put("total_proteins", totalProteins)
                        put("total_fats", totalFats)
                        put("total_carbs", totalCarbs)
                        put("created_at", "CURRENT_TIMESTAMP") // Текущее время для записи
                    }

                    // Вставляем данные в таблицу meal_logs
                    val result = db.insert("meal_logs", null, values)

                    if (result != -1L) {
                        Log.d("Database", "Meal log inserted successfully")
                    } else {
                        Log.e("Database", "Failed to insert meal log")
                    }

                    cursor.close()
                    db.close()

                    return result != -1L
                } else {
                    Log.e("Database", "Invalid column index for food data")
                }
            } else {
                Log.e("Database", "No data found for foodName: $foodName")
            }
            cursor.close()
        } else {
            Log.e("Database", "Cursor is null after query execution.")
        }

        db.close()
        return false
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
        db.close()
        return null
    }

    fun addMeal(mealName: String): Int? {
        val db = this.writableDatabase
        Log.d("Database", "Adding new meal with name: $mealName")

        val values = ContentValues().apply {
            put("meal_name", mealName)
            put("created_at", "CURRENT_TIMESTAMP")  // Подставляем текущую дату и время
        }

        val mealId = db.insert("meals", null, values)

        if (mealId != -1L) {
            Log.d("Database", "Successfully added new meal with mealId: $mealId")
            db.close()
            return mealId.toInt()
        } else {
            Log.e("Database", "Failed to add new meal")
            db.close()
            return null
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
