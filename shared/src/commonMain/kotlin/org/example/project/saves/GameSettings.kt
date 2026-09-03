package org.example.project.saves

import kotlinx.serialization.Serializable

/**
 * 玩家可调整的全局设置，独立于任何存档，持久化到 ~/.wyjgalgame/settings.json。
 *
 * 数值均以"毫秒"为单位描述剧情播放节奏，UI 层直接读取，避免把 24ms / 1200ms 之类的
 * 参数写死在 Composable 里。
 */
@Serializable
data class GameSettings(
	/** 打字机逐字显示的间隔，越小越快。0 表示瞬间显示全部。 */
	var textSpeedMillis: Int = 24,
	/** 自动播放时，一句显示完毕后停顿多久再前进。 */
	var autoPlayDelayMillis: Int = 1200,
	/** 是否允许"跳过已读文本"（快进时略过读过的普通节点）。 */
	var skipReadOnly: Boolean = true,
	/** 是否以全屏（最大化）方式启动窗口。 */
	var fullscreen: Boolean = false
) {
	fun coerced(): GameSettings = copy(
		textSpeedMillis = textSpeedMillis.coerceIn(0, 200),
		autoPlayDelayMillis = autoPlayDelayMillis.coerceIn(200, 5000)
	)

	companion object {
		/** 文字速度预设：显示名 -> 每字符毫秒。 */
		val TEXT_SPEED_PRESETS: List<Pair<String, Int>> = listOf(
			"瞬间" to 0,
			"快" to 12,
			"标准" to 24,
			"慢" to 48
		)

		/** 自动播放速度预设：显示名 -> 停顿毫秒。 */
		val AUTO_SPEED_PRESETS: List<Pair<String, Int>> = listOf(
			"快" to 700,
			"标准" to 1200,
			"慢" to 2000
		)
	}
}
