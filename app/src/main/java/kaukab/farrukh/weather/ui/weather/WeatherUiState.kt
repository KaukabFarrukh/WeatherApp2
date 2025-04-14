package kaukab.farrukh.weather.ui.weather

import kaukab.farrukh.weather.model.Weather

data class WeatherUiState(
    val weather: Weather? = null,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
)
