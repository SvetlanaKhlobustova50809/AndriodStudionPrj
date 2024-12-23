package com.example

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import com.google.gson.Gson

class ChatViewModel : ViewModel() {
    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    fun sendMessage(userMessage: String) {
        _messages.value = _messages.value!! + ChatMessage(userMessage, true)

        viewModelScope.launch(Dispatchers.IO) {
            var extractedValue = "Model answer"
            // Раскоментить для ответа модели
//            val client = OkHttpClient()
//
//            val mediaType = "application/json".toMediaTypeOrNull()
//            val body = RequestBody.create(mediaType, "{\"query\":\"$userMessage\"}")
//            val request = Request.Builder()
//                .url("https://chatgpt-openai1.p.rapidapi.com/ask")
//                .post(body)
//                .addHeader("x-rapidapi-key", "7d1747c570msh75b79621466e46dp1087f9jsn50c200f1143f")
//                .addHeader("x-rapidapi-host", "chatgpt-openai1.p.rapidapi.com")
//                .addHeader("Content-Type", "application/json")
//                .build()
//
//            val response = client.newCall(request).execute()
//            val responseBody = response.body?.string()
//            var extractedValue = ""
//            if (responseBody != null) {
//                val gson = Gson()
//                val jsonObject = gson.fromJson(responseBody, JsonObject::class.java)
//
//                extractedValue = jsonObject.get("response")?.asString.toString()
//
//            } else {
//                extractedValue = "Response body is null"
//            }


            val botMessage = extractMessageFromResponse(extractedValue)
            _messages.postValue(_messages.value!! + ChatMessage(botMessage, false))
        }
    }

    private fun extractMessageFromResponse(responseBody: String?): String {
        // Parse JSON (implement parsing based on OpenAI API response structure)
        return responseBody ?: "Error retrieving response"
    }
}