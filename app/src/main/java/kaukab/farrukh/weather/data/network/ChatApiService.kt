package kaukab.farrukh.weather.data.network

import kaukab.farrukh.weather.data.model.ChatRequest
import kaukab.farrukh.weather.data.model.ChatResponse

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ChatApiService {

    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun askQuestion(@Body body: ChatRequest): Response<ChatResponse>
}
