package kaukab.farrukh.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kaukab.farrukh.weather.ui.chat.ChatScreen
import kaukab.farrukh.weather.ui.theme.WeatherTheme
import kaukab.farrukh.weather.ui.weather.WeatherScreen


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherTheme {
                val navController = rememberNavController()

                Surface {
                    NavHost(navController = navController, startDestination = "weather") {

                        composable("weather") {
                            WeatherScreen(navController = navController)
                        }
                        composable("chat") {
                            ChatScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}
