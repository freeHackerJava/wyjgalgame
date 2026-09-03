package org.example.project.achievement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.example.project.saves.ACHIEVEMENTS
import org.example.project.saves.Achievement
import org.example.project.saves.SaveRepository

/**
 * 主菜单成就总览。
 *
 * 与训练档案里的“成就墙”不同：档案墙只反映当前存档的进度，而这里跨越全部存档槽汇总——
 * 只要任意一个存档解锁过某个成就，就在这里视为已解锁。方便玩家在开始新游戏之前，
 * 回看自己在所有周目里一共达成了哪些成就。纯读取存档，不修改任何状态。
 */
class AchievementOverviewScreen : Screen {
	@Composable
	override fun Content() {
		val navigator = LocalNavigator.currentOrThrow
		// 汇总所有存档槽已解锁的成就 id（取并集）。进入界面时读取一次即可。
		val unlocked = remember {
			(0 until SaveRepository.SLOT_COUNT)
				.mapNotNull { SaveRepository.load(it) }
				.flatMap { it.unlockedAchievements }
				.toSet()
		}
		Column(
			Modifier.fillMaxSize().background(Color(0xFFF6F1E8)).padding(24.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
				Column {
					Text("成就总览", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF713821))
					Text("汇总所有存档的成就进度", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF76685B))
				}
				OutlinedButton(onClick = { navigator.pop() }) { Text("返回主菜单") }
			}
			ProgressBanner(unlocked.size)
			LazyColumn(
				modifier = Modifier.fillMaxWidth(),
				verticalArrangement = Arrangement.spacedBy(12.dp)
			) {
				items(ACHIEVEMENTS, key = { it.id }) { achievement ->
					AchievementCard(achievement, achievement.id in unlocked)
				}
			}
		}
	}

	@Composable
	private fun ProgressBanner(unlockedCount: Int) {
		Surface(color = Color(0xFFFFFCF7), shape = MaterialTheme.shapes.medium) {
			Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
				Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
					Text("已解锁", style = MaterialTheme.typography.titleMedium, color = Color(0xFF713821))
					Text(
						"$unlockedCount / ${ACHIEVEMENTS.size}",
						style = MaterialTheme.typography.titleMedium,
						color = Color(0xFFB58A45)
					)
				}
				LinearProgressIndicator(
					progress = { if (ACHIEVEMENTS.isEmpty()) 0f else unlockedCount.toFloat() / ACHIEVEMENTS.size },
					modifier = Modifier.fillMaxWidth(),
					color = Color(0xFFB58A45),
					trackColor = Color(0xFFE6D8C9)
				)
			}
		}
	}

	@Composable
	private fun AchievementCard(achievement: Achievement, unlocked: Boolean) {
		Card(
			Modifier.fillMaxWidth(),
			colors = CardDefaults.cardColors(
				containerColor = if (unlocked) Color(0xFFFFFCF7) else Color(0xFFEDE7DE)
			)
		) {
			Row(
				Modifier.fillMaxWidth().padding(16.dp),
				horizontalArrangement = Arrangement.spacedBy(12.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					if (unlocked) "🏅" else "🔒",
					style = MaterialTheme.typography.titleLarge
				)
				Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
					Text(
						achievement.title,
						style = MaterialTheme.typography.titleSmall,
						color = if (unlocked) Color(0xFF713821) else Color(0xFF9A8C7E)
					)
					Text(
						if (unlocked) achievement.description else "尚未解锁",
						style = MaterialTheme.typography.bodySmall,
						color = if (unlocked) Color(0xFF5E5148) else Color(0xFFA99B8D)
					)
				}
			}
		}
	}
}
