package org.example.project

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.navigator.Navigator
import org.example.project.homePage.HomePageApp
import org.example.project.saves.SettingsHolder

object AppConfig {
	const val APP_NAME = "WYJ GAL GAME"
	const val APP_VERSION = "1.0.0"

	val defaultWindowWidth = 1200.dp
	val defaultWindowHeight = 800.dp

	var windowWidth by mutableStateOf(defaultWindowWidth)
	var windowHeight by mutableStateOf(defaultWindowHeight)

	var smallFontSize by mutableStateOf(14.sp)
	var mediumFontSize by mutableStateOf(18.sp)
	var largeFontSize by mutableStateOf(28.sp)
}

fun main() = application {
	val windowState = rememberWindowState(
		width = AppConfig.defaultWindowWidth,
		height = AppConfig.defaultWindowHeight,
		placement = if (SettingsHolder.settings.fullscreen) WindowPlacement.Maximized else WindowPlacement.Floating
	)

	LaunchedEffect(windowState.size) {
		snapshotFlow { windowState.size }
			.collect { size ->
				AppConfig.windowWidth = size.width
				AppConfig.windowHeight = size.height
			}
	}

	Window(
		onCloseRequest = ::exitApplication,
		title = AppConfig.APP_NAME,
		state = windowState,
		resizable = true
	) {
		MaterialTheme(
			colorScheme = lightColorScheme(
				primary = Color(0xFF9A4D32),
				secondary = Color(0xFF4F6D5A),
				tertiary = Color(0xFFB58A45),
				background = Color(0xFFF6F1E8),
				surface = Color(0xFFFFFCF7),
				surfaceVariant = Color(0xFFEDE4D7)
			)
		) {
			Navigator(HomePageApp())
		}
	}
}