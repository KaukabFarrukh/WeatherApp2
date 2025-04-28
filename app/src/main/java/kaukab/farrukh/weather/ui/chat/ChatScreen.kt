package kaukab.farrukh.weather.ui.chat

import android.Manifest
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kaukab.farrukh.weather.utils.LocationUtil
import java.util.*

@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val aiResponse by viewModel.response.collectAsState()
    val detectedCity by viewModel.detectedCity.collectAsState()
    var userInput by remember { mutableStateOf("") }
    var suggestedCity by remember { mutableStateOf<String?>(null) }

    // 🔊 Text-to-Speech
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) {
            if (it == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
            }
        }

        // 🔍 Detect city from GPS
        val city = LocationUtil.getCityFromLocation(context)
        if (city != null) suggestedCity = city
    }

    DisposableEffect(Unit) {
        onDispose { tts?.shutdown() }
    }

    LaunchedEffect(aiResponse) {
        if (aiResponse.isNotBlank() && aiResponse != "Say something to get started!") {
            tts?.speak(aiResponse, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spokenText = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
        spokenText?.let {
            userInput = it
            viewModel.askAI(it)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            }
            voiceLauncher.launch(intent)
        } else {
            Toast.makeText(context, "Mic permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("Ask AI", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                label = { Text("Enter your question") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }) {
                Icon(Icons.Default.Mic, contentDescription = "Speak")
            }
        }

        // 📍 Optional GPS Suggestion Button
        suggestedCity?.let { city ->
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "📍 Detected from location: $city",
                style = MaterialTheme.typography.bodySmall
            )

            Button(
                onClick = {
                    userInput = "What's the weather in $city?"
                    viewModel.askAI(userInput)
                },
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text("Use current location: $city")
            }
        }
        // Detected City from prompt analysis
        if (detectedCity.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Detected city: $detectedCity", style = MaterialTheme.typography.labelMedium)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (userInput.isNotBlank()) {
                    viewModel.askAI(userInput)
                    userInput = ""
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Send")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("AI: $aiResponse", style = MaterialTheme.typography.bodyLarge)
    }
}
