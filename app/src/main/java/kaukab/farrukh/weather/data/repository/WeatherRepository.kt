package kaukab.farrukh.weather.data.repository

import kaukab.farrukh.weather.model.Weather
import kaukab.farrukh.weather.utils.Result
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    fun getWeatherForecast(city: String): Flow<Result<Weather>>
}