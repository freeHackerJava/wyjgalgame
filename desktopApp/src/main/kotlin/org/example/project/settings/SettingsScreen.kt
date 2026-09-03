package org.example.project.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.example.project.saves.GameSettings
import org.example.project.saves.SettingsHolder

/**
 * 全局设置界面：调整文字速度、自动播放停顿、跳过已读文本与全屏启动。
 * 修改会即时写入 [SettingsHolder]（进而持久化到 settings.json），正在播放的剧情会立刻感知新速度。
 */
class SettingsScreen : Screen {
	@Composable
	override fun Content() {
		val navigator = LocalNavigator.currentOrThrow
		// 以当前设置为初值的本地草稿；每次改动即时提交给 SettingsHolder。
		var draft by remember { mutableStateOf(SettingsHolder.settings) }

		fun apply(newSettings: GameSettings) {
			draft = newSettings.coerced()
			SettingsHolder.update(draft)
		}

		Box(Modifier.fillMaxSize().background(Color(0xFFF6F1E8)), contentAlignment = Alignment.Center) {
			Column(
				Modifier.padding(48.dp).fillMaxWidth(0.72f),
				verticalArrangement = Arrangement.spacedBy(18.dp),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Text("设置", style = MaterialTheme.typography.displaySmall, color = Color(0xFF713821))
				Text("这些设置对所有存档生效，会立即保存。", style = MaterialTheme.typography.bodyMedium)

				Card(
					Modifier.fillMaxWidth(),
					colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF7))
				) {
					Column(Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
						PresetRow(
							title = "文字速度",
							hint = "剧情逐字显示的快慢。",
							presets = GameSettings.TEXT_SPEED_PRESETS,
							selected = draft.textSpeedMillis,
							onSelect = { apply(draft.copy(textSpeedMillis = it)) }
						)
						PresetRow(
							title = "自动播放停顿",
							hint = "开启自动播放后，一句显示完毕停顿多久再前进。",
							presets = GameSettings.AUTO_SPEED_PRESETS,
							selected = draft.autoPlayDelayMillis,
							onSelect = { apply(draft.copy(autoPlayDelayMillis = it)) }
						)
						ToggleRow(
							title = "跳过已读文本",
							hint = "快进时略过已经读过的普通对白，只在遇到新内容或选项时停下。",
							checked = draft.skipReadOnly,
							onCheckedChange = { apply(draft.copy(skipReadOnly = it)) }
						)
						ToggleRow(
							title = "全屏启动",
							hint = "下次启动游戏时以最大化窗口打开。",
							checked = draft.fullscreen,
							onCheckedChange = { apply(draft.copy(fullscreen = it)) }
						)
					}
				}

				OutlinedButton(onClick = { navigator.pop() }) { Text("返回") }
			}
		}
	}

	@Composable
	private fun PresetRow(
		title: String,
		hint: String,
		presets: List<Pair<String, Int>>,
		selected: Int,
		onSelect: (Int) -> Unit
	) {
		Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
			Text(title, style = MaterialTheme.typography.titleMedium, color = Color(0xFF713821))
			Text(hint, style = MaterialTheme.typography.labelMedium, color = Color(0xFF76685B))
			Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
				presets.forEach { (label, value) ->
					if (value == selected) {
						Button(onClick = { onSelect(value) }) { Text(label) }
					} else {
						OutlinedButton(onClick = { onSelect(value) }) { Text(label) }
					}
				}
			}
		}
	}

	@Composable
	private fun ToggleRow(
		title: String,
		hint: String,
		checked: Boolean,
		onCheckedChange: (Boolean) -> Unit
	) {
		Row(
			Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Column(Modifier.padding(end = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
				Text(title, style = MaterialTheme.typography.titleMedium, color = Color(0xFF713821))
				Text(hint, style = MaterialTheme.typography.labelMedium, color = Color(0xFF76685B))
			}
			Switch(checked = checked, onCheckedChange = onCheckedChange)
		}
	}
}
