package kaukab.farrukh.weather.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kaukab.farrukh.weather.data.model.ChatRequest
import kaukab.farrukh.weather.data.model.Message
import kaukab.farrukh.weather.data.network.ChatApiService
import kaukab.farrukh.weather.data.repository.WeatherRepository
import kaukab.farrukh.weather.model.Weather
import kaukab.farrukh.weather.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatApiService: ChatApiService,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _response = MutableStateFlow("Say something to get started!")
    val response: StateFlow<String> = _response.asStateFlow()

    private val _detectedCity = MutableStateFlow("")
    val detectedCity: StateFlow<String> = _detectedCity.asStateFlow()

    fun setDetectedCity(city: String) {
        _detectedCity.value = city
    }

    fun askAI(userInput: String) {
        viewModelScope.launch {
            val cityFromInput = extractCityFromInput(userInput) ?: detectedCity.value.ifBlank { "Malmo" }

            val weatherSummary = getCurrentWeatherSummary(cityFromInput)

            val prompt = """
                The user asked: "$userInput"
                Please give a helpful answer using this real-time weather:
                $weatherSummary
            """.trimIndent()

            val request = ChatRequest(
                model = "gpt-3.5-turbo",
                messages = listOf(
                    Message(role = "system", content = "You are a helpful assistant that provides weather information."),
                    Message(role = "user", content = prompt)
                )
            )

            try {
                val result = chatApiService.askQuestion(request)
                if (result.isSuccessful) {
                    val answer = result.body()?.choices?.firstOrNull()?.message?.content
                    _response.value = answer ?: "No response from AI."
                } else {
                    _response.value = "Error ${result.code()}: ${result.message()}"
                }
            } catch (e: Exception) {
                _response.value = "Failed to get response: ${e.localizedMessage}"
            }
        }
    }

    private suspend fun getCurrentWeatherSummary(city: String): String {
        return try {
            weatherRepository.getWeatherForecast(city).firstOrNull()?.let { result ->
                when (result) {
                    is Result.Success -> {
                        val weather: Weather = result.data
                        "Temperature: ${weather.temperature}°C\n" +
                                "Condition: ${weather.condition.text}\n" +
                                "Humidity: ${weather.humidity}%\n" +
                                "Feels Like: ${weather.feelsLike}°C"
                    }
                    is Result.Error -> "Unable to fetch weather for $city."
                    is Result.Loading -> "Loading weather..."
                }
            } ?: "Weather data unavailable"
        } catch (e: Exception) {
            "Weather data unavailable"
        }
    }

    private fun extractCityFromInput(input: String): String? {
        val cityRegex = Regex("""in ([A-Za-z\s]+)""")
        return cityRegex.find(input)?.groupValues?.getOrNull(1)?.trim()
    }
}
