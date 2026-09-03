package org.example.project.saves

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * 全局设置的运行时持有者。
 *
 * [GameSettings] 独立于任何存档，被主菜单的设置界面读写，并被剧情播放界面 [org.example.project.runPlot.RunPlotApp]
 * 直接读取（打字机速度、自动播放停顿、跳过已读）。由于 RunPlotApp 与存档系统解耦，用一个进程级单例把设置传播出去，
 * 避免层层透传构造参数。设置面板保存时调用 [update]，会同时写入 settings.json 并刷新此处的 Compose 状态，
 * 令正在播放的剧情立即感知新速度。
 */
object SettingsHolder {
	/** 当前生效的设置，初值来自磁盘。改变时会触发使用它的 Composable 重组。 */
	var settings: GameSettings by mutableStateOf(SaveRepository.loadSettings())
		private set

	/** 更新设置并持久化。 */
	fun update(newSettings: GameSettings) {
		val coerced = newSettings.coerced()
		settings = coerced
		SaveRepository.saveSettings(coerced)
	}
}
