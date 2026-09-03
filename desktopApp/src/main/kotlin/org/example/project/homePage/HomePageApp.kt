package org.example.project.homePage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import org.example.project.achievement.AchievementOverviewScreen
import org.example.project.playScreen.PlayScreenApp
import org.example.project.saves.GameOutcome
import org.example.project.saves.SaveFormat
import org.example.project.saves.SaveRepository
import org.example.project.saves.SaveSlotInfo
import org.example.project.settings.SettingsScreen
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.system.exitProcess

class HomePageApp : Screen {
	@Composable
	override fun Content() {
		val navigator = LocalNavigator.currentOrThrow
		// revision 变化时重新读取存档槽信息（删档 / 新建后刷新）。
		var revision by remember { mutableStateOf(0) }
		val slots = remember(revision) { SaveRepository.allSlots() }
		// 二周目解锁状态与跨局继承的“记忆”（成就），随 revision 一起刷新。
		val progress = remember(revision) { SaveRepository.loadProgress() }
		var pendingDelete by remember { mutableStateOf<Int?>(null) }

		Box(Modifier.fillMaxSize().background(Color(0xFFF6F1E8)), contentAlignment = Alignment.Center) {
			Column(
				Modifier.padding(48.dp).fillMaxWidth(0.72f),
				verticalArrangement = Arrangement.spacedBy(18.dp),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Text("WYJ GAL GAME", style = MaterialTheme.typography.displaySmall, color = Color(0xFF713821))
				Text("合肥 · 周日编程班", style = MaterialTheme.typography.titleLarge, color = Color(0xFF4F6D5A))
				Text("从 CSP-S 1= 出发，陪她们走到 NOI Au。", style = MaterialTheme.typography.bodyLarge)

				Card(
					Modifier.fillMaxWidth(),
					colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF7))
				) {
					Column(Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
						Text("存档", style = MaterialTheme.typography.headlineSmall)
						Text("王怡钧为了挣生活费接下朋友介绍的编程班。四个初一女生，四条完全不同的成长路线。")
						slots.forEachIndexed { index, info ->
							SlotRow(
								slot = index,
								info = info,
								newGamePlusUnlocked = progress.newGamePlusUnlocked,
								onContinue = {
									val loaded = SaveRepository.load(index) ?: return@SlotRow
									loaded.slot = index
									navigator.push(PlayScreenApp(loaded))
								},
								onNewGame = {
									val fresh = SaveFormat().apply { slot = index }
									SaveRepository.save(fresh, index)
									navigator.push(PlayScreenApp(fresh))
								},
								onNewGamePlus = {
									// 二周目：带着上一世继承的成就（记忆）从头开局。
									val ngPlus = SaveFormat.newGamePlus(index, progress.carriedAchievements)
									SaveRepository.save(ngPlus, index)
									navigator.push(PlayScreenApp(ngPlus))
								},
								onDelete = { pendingDelete = index }
							)
						}
					}
				}
				Text("2024.09.01  ·  ICPC Final Au  ·  NOI Au", style = MaterialTheme.typography.labelMedium)
				Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
					OutlinedButton(onClick = { navigator.push(AchievementOverviewScreen()) }) { Text("成就") }
					OutlinedButton(onClick = { navigator.push(SettingsScreen()) }) { Text("设置") }
					OutlinedButton(onClick = { exitProcess(0) }) { Text("退出游戏") }
				}
			}
		}

		pendingDelete?.let { slot ->
			AlertDialog(
				onDismissRequest = { pendingDelete = null },
				title = { Text("删除存档") },
				text = { Text("确定要删除存档位 ${slot + 1} 吗？此操作无法撤销。") },
				confirmButton = {
					TextButton(onClick = {
						SaveRepository.delete(slot)
						pendingDelete = null
						revision++
					}) { Text("删除") }
				},
				dismissButton = {
					TextButton(onClick = { pendingDelete = null }) { Text("取消") }
				}
			)
		}
	}

	@Composable
	private fun SlotRow(
		slot: Int,
		info: SaveSlotInfo?,
		newGamePlusUnlocked: Boolean,
		onContinue: () -> Unit,
		onNewGame: () -> Unit,
		onNewGamePlus: () -> Unit,
		onDelete: () -> Unit
	) {
		Card(
			Modifier.fillMaxWidth(),
			colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F1E8))
		) {
			Row(
				Modifier.fillMaxWidth().padding(16.dp),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Column(Modifier.padding(end = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
					Text("存档位 ${slot + 1}", style = MaterialTheme.typography.titleMedium, color = Color(0xFF713821))
					if (info == null) {
						Text("空", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF9A8C7E))
					} else {
						Text("第${info.week}周 · ${info.milestone} · ${outcomeLabel(info.outcome)}", style = MaterialTheme.typography.bodyMedium)
						Text("上次保存：${formatTime(info.savedAtMillis)}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9A8C7E))
					}
				}
				Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
					if (info == null) {
						Button(onClick = onNewGame) { Text("新游戏") }
					} else {
						Button(onClick = onContinue) { Text("继续") }
						OutlinedButton(onClick = onNewGame) { Text("覆盖新开") }
						OutlinedButton(onClick = onDelete) { Text("删除") }
					}
					// 解锁二周目后，任意存档位都可以直接开一局“带着记忆重来”的新档。
					if (newGamePlusUnlocked) {
						OutlinedButton(onClick = onNewGamePlus) { Text("二周目") }
					}
				}
			}
		}
	}

	private fun outcomeLabel(outcome: GameOutcome): String = when (outcome) {
		GameOutcome.PLAYING -> "进行中"
		GameOutcome.BAD_ENDING -> "遗憾结局"
		GameOutcome.NORMAL_ENDING -> "NOI 结局"
		GameOutcome.ROMANCE_PENDING -> "待抉择"
		GameOutcome.ROMANCE_ENDING -> "心意结局"
		GameOutcome.FAMILY_ENDING -> "合家欢结局"
	}

	private fun formatTime(millis: Long): String {
		if (millis <= 0L) return "未知"
		return SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date(millis))
	}
}
