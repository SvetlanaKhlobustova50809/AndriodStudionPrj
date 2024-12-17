package com.example.calorix

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

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
            put("age", age)
            put("gender", gender)
            put("weight", weight)
            put("height", height)
            put("activity_level", activityLevel)
            put("goal", goal)
        }

        val result = db.insert("users", null, values)
        return result != -1L
    }

}

// Класс для представления пользователя
data class User(
    val userId: Int,
    val userName: String,
    val email: String
)
