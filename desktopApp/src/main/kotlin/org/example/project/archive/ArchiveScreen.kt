package org.example.project.archive

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.example.project.saves.ACHIEVEMENTS
import org.example.project.saves.Achievement
import org.example.project.saves.CompetitionResult
import org.example.project.saves.SaveFormat
import org.example.project.saves.StudentState
import org.example.project.runPlot.Portraits

/**
 * 训练档案 / 回顾面板。
 *
 * 汇总一个存档里累积下来、平时散落在各处的信息，方便玩家回看整段历程：
 *  - 训练概览（周数、里程碑进度、已解锁的重大剧情数量）；
 *  - 四位学生的成长与好感阶段；
 *  - 历次里程碑比赛与最近一次模拟赛的成绩；
 *  - 玩家在剧情里做过的关键选择。
 * 纯读取 [SaveFormat]，不修改任何状态。
 */
class ArchiveScreen(val gameState: SaveFormat) : Screen {
	@Composable
	override fun Content() {
		val navigator = LocalNavigator.currentOrThrow
		Column(
			Modifier.fillMaxSize().background(Color(0xFFF6F1E8)).padding(24.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
				Column {
					Text("训练档案", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF713821))
					Text("第${gameState.week}周 · 存档位 ${gameState.slot + 1}", style = MaterialTheme.typography.bodyMedium)
				}
				OutlinedButton(onClick = { navigator.pop() }) { Text("返回经营") }
			}
			LazyColumn(
				modifier = Modifier.fillMaxWidth(),
				verticalArrangement = Arrangement.spacedBy(12.dp)
			) {
				item { OverviewCard(gameState) }
				item { Text("学生成长", style = MaterialTheme.typography.titleLarge, color = Color(0xFF713821)) }
				items(gameState.students, key = { it.id }) { student -> StudentCard(student) }
				item { Text("比赛记录", style = MaterialTheme.typography.titleLarge, color = Color(0xFF713821)) }
				item { CompetitionCard("最近里程碑比赛", gameState.lastCompetition) }
				item { CompetitionCard("最近模拟赛", gameState.lastMockContest) }
				item { Text("你的选择", style = MaterialTheme.typography.titleLarge, color = Color(0xFF713821)) }
				item { ChoicesCard(gameState.storyChoices) }
				item {
					Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
						Text("成就墙", style = MaterialTheme.typography.titleLarge, color = Color(0xFF713821))
						Text(
							"${gameState.unlockedAchievements.size} / ${ACHIEVEMENTS.size}",
							style = MaterialTheme.typography.titleMedium,
							color = Color(0xFFB58A45)
						)
					}
				}
				items(ACHIEVEMENTS, key = { it.id }) { achievement ->
					AchievementCard(achievement, achievement.id in gameState.unlockedAchievements)
				}
			}
		}
	}

	@Composable
	private fun OverviewCard(state: SaveFormat) {
		Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF7))) {
			Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
				Text("训练概览", style = MaterialTheme.typography.titleMedium, color = Color(0xFF713821))
				Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
					StatChip("当前阶段", state.currentMilestone)
					StatChip("已通过里程碑", "${state.completedMilestones.size} / 4")
					StatChip("已解锁剧情", "${state.seenMajorScenes.size} 段")
				}
				Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
					StatChip("经历事件", "${state.seenEvents.size + state.seenRandomEvents.size} 次")
					StatChip("累计选择", "${state.storyChoices.size} 次")
				}
			}
		}
	}

	@Composable
	private fun StatChip(label: String, value: String) {
		Surface(color = Color(0xFFEFE3D4), shape = MaterialTheme.shapes.medium) {
			Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
				Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF76685B))
				Text(value, style = MaterialTheme.typography.titleSmall, color = Color(0xFF713821))
			}
		}
	}

	@Composable
	private fun StudentCard(student: StudentState) {
		Card(Modifier.fillMaxWidth()) {
			Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
				Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
					Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
						Portraits.characterForStudentId(student.id)?.let { character ->
							Portraits.SpeakerAvatar(speaker = character, size = 44.dp)
						}
						Text(student.name, style = MaterialTheme.typography.titleLarge)
					}
					Text(favorStage(student.favor), style = MaterialTheme.typography.labelLarge, color = Color(0xFFB58A45))
				}
				StatBar("水平", student.ability, Color(0xFF9A4D32))
				StatBar("科技", student.technology, Color(0xFF4F6D5A))
				StatBar("好感", student.favor, Color(0xFFB58A45), 12)
				StatBar("士气", student.morale, Color(0xFF6C7B9A), 10)
				Text("成长 ${student.growthRate}    稳定性 ${student.stability}", style = MaterialTheme.typography.labelMedium, color = Color(0xFF76685B))
			}
		}
	}

	@Composable
	private fun CompetitionCard(title: String, results: List<CompetitionResult>) {
		Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF7))) {
			Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
				Text(title, style = MaterialTheme.typography.titleMedium, color = Color(0xFF713821))
				if (results.isEmpty()) {
					Text("暂无记录。", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9A8C7E))
				} else {
					Text(results.first().milestone, style = MaterialTheme.typography.labelLarge, color = Color(0xFF76685B))
					results.forEach { r ->
						Text("${r.rank}. ${r.studentName}　${r.score} 分　${r.performance}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5E5148))
					}
				}
			}
		}
	}

	@Composable
	private fun ChoicesCard(choices: List<String>) {
		Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF7))) {
			Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
				if (choices.isEmpty()) {
					Text("还没有做出任何关键选择。", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9A8C7E))
				} else {
					choices.forEachIndexed { index, choice ->
						Text("${index + 1}. 「$choice」", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5E5148))
					}
				}
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
				verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
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
						achievement.description,
						style = MaterialTheme.typography.bodySmall,
						color = if (unlocked) Color(0xFF5E5148) else Color(0xFFA99B8D)
					)
				}
			}
		}
	}

	@Composable
	private fun StatBar(label: String, value: Int, color: Color, maximum: Int = 120) {
		Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
			Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
				Text(label, style = MaterialTheme.typography.labelMedium)
				Text("$value / $maximum", style = MaterialTheme.typography.labelMedium)
			}
			LinearProgressIndicator(
				progress = { (value.toFloat() / maximum).coerceIn(0f, 1f) },
				modifier = Modifier.fillMaxWidth(),
				color = color,
				trackColor = Color(0xFFE6D8C9)
			)
		}
	}

	private fun favorStage(favor: Int): String = when {
		favor >= 12 -> "心意 · 满"
		favor >= 10 -> "交心"
		favor >= 8 -> "亲近"
		favor >= 4 -> "信任"
		favor >= 1 -> "熟识"
		else -> "陌生"
	}
}
