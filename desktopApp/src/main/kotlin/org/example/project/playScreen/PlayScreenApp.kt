package org.example.project.playScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.example.project.saves.SaveFormat
import org.example.project.saves.GameOutcome
import org.example.project.saves.SaveRepository
import org.example.project.runPlot.Portraits
import org.example.project.runPlot.RunPlotApp

class PlayScreenApp(val gameState: SaveFormat) : Screen {
	@Composable
	override fun Content() {
		val navigator = LocalNavigator.currentOrThrow
		var prologueShown by remember { mutableStateOf(false) }
		var majorSceneShown by remember { mutableStateOf<String?>(null) }
		var revision by remember { mutableStateOf(0) }
		// 成就解锁提示队列：位于 key(revision) 之外，跨面板重建保留，逐条弹窗祝贺。
		val achievementQueue = remember { mutableStateListOf<org.example.project.saves.Achievement>() }
		if (!gameState.finishedPrologue && !prologueShown) {
			prologueShown = true
			LaunchedEffect(Unit) {
				navigator.push(RunPlotApp(
					plot = if (gameState.newGamePlus)
						org.example.project.plots.NgPlusPrologue
					else
						org.example.project.plots.Prologue,
					title = if (gameState.newGamePlus)
						"序章 · 我知道该怎么回答"
					else
						"序章 · 合肥的第一堂课",
					gameState = gameState,
					onFinished = {
						gameState.finishedPrologue = true
						gameState.majorSceneNodeId = null
						gameState.checkAchievements().forEach { achievementQueue.add(it) }
						SaveRepository.save(gameState, gameState.slot)
						revision++
					}
				))
			}
		}
		if (gameState.pendingMajorScene != null && gameState.pendingMajorScene != majorSceneShown) {
			val sceneId = gameState.pendingMajorScene
			majorSceneShown = sceneId
			LaunchedEffect(sceneId) {
				sceneId?.let { scene ->
					navigator.push(RunPlotApp(
						plot = sceneFor(scene),
						title = majorSceneTitle(scene),
						gameState = gameState,
						onChoice = { choice ->
							gameState.applyStoryChoice(choice)
							SaveRepository.save(gameState, gameState.slot)
						}
					) {
						gameState.finishMajorScene()
						gameState.checkAchievements().forEach { achievementQueue.add(it) }
						SaveRepository.save(gameState, gameState.slot)
						revision++
					})
				}
			}
		}
		key(revision) {
			ManagementPanel(
				gameState = gameState,
				navigator = navigator,
				onChanged = {
					gameState.checkAchievements().forEach { achievementQueue.add(it) }
					SaveRepository.save(gameState, gameState.slot)
					revision++
				}
			)
		}
		achievementQueue.firstOrNull()?.let { achievement ->
			AlertDialog(
				onDismissRequest = { achievementQueue.removeAt(0) },
				title = { Text("🏅 成就解锁　${achievement.title}") },
				text = { Text(achievement.description) },
				confirmButton = {
					Button(onClick = { achievementQueue.removeAt(0) }) { Text("继续") }
				}
			)
		}
	}

	@Composable
	private fun ManagementPanel(gameState: SaveFormat, navigator: Navigator, onChanged: () -> Unit) {
		if (gameState.outcome != GameOutcome.PLAYING) {
			EndingPanel(gameState, navigator, onChanged)
			return
		}
		if (gameState.newGamePlus) {
			NgPlusManagementPanel(gameState, navigator, onChanged)
			return
		}
		var showMenuConfirm by remember { mutableStateOf(false) }
		var showLoadMenu by remember { mutableStateOf(false) }
		Column(
			Modifier.fillMaxSize().background(Color(0xFFF6F1E8)).padding(24.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
				Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
					Text("合肥 · 周日编程班", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF713821))
					Text("第${gameState.week}周  ·  ${gameState.date}  ·  存档位 ${gameState.slot + 1}", style = MaterialTheme.typography.bodyMedium)
				}
				Surface(color = Color(0xFFE9D5C5), shape = MaterialTheme.shapes.medium) {
					Text("行动点  ${gameState.actionPoints} / 3", modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), color = Color(0xFF713821))
				}
			}
			Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
				OutlinedButton(onClick = {
					SaveRepository.save(gameState, gameState.slot)
					gameState.lastActionText = "已保存到存档位 ${gameState.slot + 1}。"
					onChanged()
				}) { Text("手动保存") }
				OutlinedButton(onClick = { showLoadMenu = true }) { Text("读取存档") }
				OutlinedButton(onClick = { navigator.push(org.example.project.archive.ArchiveScreen(gameState)) }) { Text("训练档案") }
				OutlinedButton(onClick = { showMenuConfirm = true }) { Text("返回主菜单") }
			}
			Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
				Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
					Text("${gameState.currentMilestone} 1=", style = MaterialTheme.typography.titleMedium)
					Text("目标：四人 NOI Au", style = MaterialTheme.typography.labelLarge)
				}
				LinearProgressIndicator(
					progress = { milestoneProgress(gameState.currentMilestone) },
					modifier = Modifier.fillMaxWidth(),
					color = Color(0xFF9A4D32),
					trackColor = Color(0xFFE6D8C9)
				)
			}
			if (gameState.lastCompetition.isNotEmpty()) {
				Text("最近比赛：${gameState.lastCompetition.first().milestone}", style = MaterialTheme.typography.titleSmall)
				gameState.lastCompetition.forEach { result ->
					Text("${result.rank}. ${result.studentName}  ${result.score} 分　${result.performance}", style = MaterialTheme.typography.bodySmall)
				}
			}
			LazyColumn(
				modifier = Modifier.weight(1f),
				verticalArrangement = Arrangement.spacedBy(10.dp)
			) {
				items(gameState.students, key = { it.id }) { student ->
					Card(Modifier.fillMaxWidth()) {
						Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
							Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
								Portraits.characterForStudentId(student.id)?.let { character ->
									Portraits.SpeakerAvatar(speaker = character, size = 48.dp)
								}
								Text(student.name, style = MaterialTheme.typography.titleLarge)
							}
							Text(student.description, style = MaterialTheme.typography.bodyMedium)
							StatBar("水平", student.ability, Color(0xFF9A4D32))
							StatBar("科技", student.technology, Color(0xFF4F6D5A))
							StatBar("好感", student.favor, Color(0xFFB58A45), 12)
							StatBar("士气", student.morale, Color(0xFF6C7B9A), 10)
							Text("成长 ${student.growthRate}    稳定性 ${student.stability}", style = MaterialTheme.typography.labelMedium, color = Color(0xFF76685B))
							Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
								Button(enabled = gameState.actionPoints > 0, onClick = { if (gameState.spend("teach", student.id)) onChanged() }) { Text("讲授科技") }
								Button(enabled = gameState.actionPoints > 0, onClick = { if (gameState.spend("coach", student.id)) onChanged() }) { Text("私下辅导") }
								Button(
									enabled = gameState.actionPoints > 0 && !gameState.heartToHeartDoneThisWeek,
									onClick = { if (gameState.spend("talk", student.id)) onChanged() }
								) { Text("谈心") }
							}
						}
					}
				}
			}
			if (gameState.lastActionText.isNotEmpty()) {
				Text(
					gameState.lastActionText,
					style = MaterialTheme.typography.bodySmall,
					color = Color(0xFF75695F)
				)
			}
			Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
				Button(enabled = gameState.actionPoints > 0, onClick = { if (gameState.spend("prepare")) onChanged() }) { Text("备课（全员科技 +1）") }
				Button(enabled = gameState.actionPoints > 0, onClick = { if (gameState.spend("review")) onChanged() }) { Text("集体复盘（全员水平 +2）") }
				Button(
					enabled = gameState.actionPoints > 0 && !gameState.heartToHeartDoneThisWeek,
					onClick = { if (gameState.spend("walk")) onChanged() }
				) { Text(if (gameState.heartToHeartDoneThisWeek) "本周已放松" else "带她们散步") }
			}
			Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
				Button(
					enabled = gameState.actionPoints > 0 && !gameState.mockContestDoneThisWeek,
					onClick = { if (gameState.spend("mock")) onChanged() }
				) { Text(if (gameState.mockContestDoneThisWeek) "本周已模拟赛" else "组织模拟赛") }
				Button(
					onClick = { gameState.endWeek(); onChanged() }
				) { Text("结束本周") }
			}
			// 好感阈值达标通知：提示玩家有好感剧情可观看，点击后才进入。
			if (gameState.pendingFavorScene != null) {
				Surface(
					color = Color(0xFFE8D5B8),
					shape = MaterialTheme.shapes.medium,
					modifier = Modifier.fillMaxWidth()
				) {
					Row(
						Modifier.padding(16.dp),
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.spacedBy(12.dp)
					) {
						Text(
							"有人和你的关系更近了一步。",
							style = MaterialTheme.typography.bodyLarge,
							color = Color(0xFF713821),
							modifier = Modifier.weight(1f)
						)
						Button(onClick = {
							gameState.acceptFavorScene()
							onChanged()
						}) { Text("进入好感剧情") }
					}
				}
			}
		}
		if (gameState.eventId != null) {
			AlertDialog(
				onDismissRequest = {
					gameState.dismissEvent()
					onChanged()
				},
				title = { Text(gameState.eventSpeaker) },
				text = { Text(gameState.eventText) },
				confirmButton = {
					Button(onClick = {
						gameState.dismissEvent()
						onChanged()
					}) { Text("记住这一刻") }
				}
			)
		}
		if (showMenuConfirm) {
			AlertDialog(
				onDismissRequest = { showMenuConfirm = false },
				title = { Text("返回主菜单") },
				text = { Text("当前进度会先自动保存到存档位 ${gameState.slot + 1}，然后返回主菜单。") },
				confirmButton = {
					TextButton(onClick = {
						SaveRepository.save(gameState, gameState.slot)
						showMenuConfirm = false
						navigator.popUntilRoot()
					}) { Text("保存并返回") }
				},
				dismissButton = {
					TextButton(onClick = { showMenuConfirm = false }) { Text("取消") }
				}
			)
		}
		if (showLoadMenu) {
			LoadMenuDialog(
				currentSlot = gameState.slot,
				onDismiss = { showLoadMenu = false },
				onLoad = { slot ->
					val loaded = SaveRepository.load(slot) ?: return@LoadMenuDialog
					loaded.slot = slot
					showLoadMenu = false
					navigator.replaceAll(PlayScreenApp(loaded))
				}
			)
		}
	}

	/**
	 * 二周目专属·精简经营界面。玩家一周目已把经营玩腻，二周目要的是“精炼界面 + 稠密故事”：
	 * 去掉数值进度条、四人属性卡、备课/复盘/散步/模拟赛/单人辅导等日常按钮，
	 * 只保留“上课 → 结束本周”的核心节奏；四人以头像 + 名字的方式在场，但不再暴露数值。
	 * 学生成长改由 [SaveFormat.endWeek] 中的二周目每周自动补档保证，避免精简后卡关。
	 */
	@Composable
	private fun NgPlusManagementPanel(gameState: SaveFormat, navigator: Navigator, onChanged: () -> Unit) {
		var showMenuConfirm by remember { mutableStateOf(false) }
		var showLoadMenu by remember { mutableStateOf(false) }
		Column(
			Modifier.fillMaxSize().background(Color(0xFFEDE7DD)).padding(28.dp),
			verticalArrangement = Arrangement.spacedBy(20.dp)
		) {
			Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
				Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
					Text("合肥 · 又一个周日", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF5E4A3A))
					Text("第${gameState.week}周  ·  ${gameState.date}  ·  存档位 ${gameState.slot + 1}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF76685B))
				}
				Surface(color = Color(0xFFDDCEBE), shape = MaterialTheme.shapes.medium) {
					Text("行动点  ${gameState.actionPoints} / 3　·　${gameState.currentMilestone}", modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), color = Color(0xFF5E4A3A))
				}
			}
			Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
				OutlinedButton(onClick = {
					SaveRepository.save(gameState, gameState.slot)
					gameState.lastActionText = "已保存到存档位 ${gameState.slot + 1}。"
					onChanged()
				}) { Text("手动保存") }
				OutlinedButton(onClick = { showLoadMenu = true }) { Text("读取存档") }
				OutlinedButton(onClick = { navigator.push(org.example.project.archive.ArchiveScreen(gameState)) }) { Text("训练档案") }
				OutlinedButton(onClick = { showMenuConfirm = true }) { Text("返回主菜单") }
			}
			// 精简版：四人只以头像 + 名字在场，不再显示任何数值与操作按钮。
			Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
				Text("这一周，她们还在。", style = MaterialTheme.typography.titleMedium, color = Color(0xFF5E4A3A))
				Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
					gameState.students.forEach { student ->
						Column(
							horizontalAlignment = Alignment.CenterHorizontally,
							verticalArrangement = Arrangement.spacedBy(6.dp)
						) {
							Portraits.characterForStudentId(student.id)?.let { character ->
								Portraits.SpeakerAvatar(speaker = character, size = 64.dp)
							}
							Text(student.name, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF5E4A3A))
						}
					}
				}
				if (gameState.lastCompetition.isNotEmpty()) {
					Text("最近比赛：${gameState.lastCompetition.first().milestone}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF76685B))
				}
				if (gameState.lastActionText.isNotEmpty()) {
					Text(gameState.lastActionText, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6B5B4C))
				}
			}
			// 二周目经营只剩两种集体行动，加上结束本周。没有上课，也没有单人辅导。
			Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
				Button(
					enabled = gameState.actionPoints > 0,
					onClick = { if (gameState.spend("ngplus-ability")) onChanged() }
				) { Text("集体提升水平") }
				Button(
					enabled = gameState.actionPoints > 0,
					onClick = { if (gameState.spend("ngplus-tech")) onChanged() }
				) { Text("集体讲授科技") }
				Button(onClick = { gameState.endWeek(); onChanged() }) { Text("结束本周") }
			}
		}
		if (gameState.eventId != null) {
			AlertDialog(
				onDismissRequest = {
					gameState.dismissEvent()
					onChanged()
				},
				title = { Text(gameState.eventSpeaker) },
				text = { Text(gameState.eventText) },
				confirmButton = {
					Button(onClick = {
						gameState.dismissEvent()
						onChanged()
					}) { Text("记住这一刻") }
				}
			)
		}
		if (showMenuConfirm) {
			AlertDialog(
				onDismissRequest = { showMenuConfirm = false },
				title = { Text("返回主菜单") },
				text = { Text("当前进度会先自动保存到存档位 ${gameState.slot + 1}，然后返回主菜单。") },
				confirmButton = {
					TextButton(onClick = {
						SaveRepository.save(gameState, gameState.slot)
						showMenuConfirm = false
						navigator.popUntilRoot()
					}) { Text("保存并返回") }
				},
				dismissButton = {
					TextButton(onClick = { showMenuConfirm = false }) { Text("取消") }
				}
			)
		}
		if (showLoadMenu) {
			LoadMenuDialog(
				currentSlot = gameState.slot,
				onDismiss = { showLoadMenu = false },
				onLoad = { slot ->
					val loaded = SaveRepository.load(slot) ?: return@LoadMenuDialog
					loaded.slot = slot
					showLoadMenu = false
					navigator.replaceAll(PlayScreenApp(loaded))
				}
			)
		}
	}

	/** 游戏内读档：列出所有存档槽，选择后切换到该存档（不必回到主菜单）。 */
	@Composable
	private fun LoadMenuDialog(currentSlot: Int, onDismiss: () -> Unit, onLoad: (Int) -> Unit) {
		val slots = remember { SaveRepository.allSlots() }
		AlertDialog(
			onDismissRequest = onDismiss,
			title = { Text("读取存档") },
			confirmButton = {},
			dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
			text = {
				Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
					Text("切换存档前，请先手动保存当前进度以免丢失。", style = MaterialTheme.typography.labelMedium, color = Color(0xFF76685B))
					slots.forEachIndexed { index, info ->
						Row(
							Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.SpaceBetween,
							verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
						) {
							Column(Modifier.padding(end = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
								Text(
									"存档位 ${index + 1}${if (index == currentSlot) "（当前）" else ""}",
									style = MaterialTheme.typography.titleSmall,
									color = Color(0xFF713821)
								)
								if (info == null) {
									Text("空", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9A8C7E))
								} else {
									Text("第${info.week}周 · ${info.milestone}", style = MaterialTheme.typography.bodySmall)
								}
							}
							Button(
								enabled = info != null && index != currentSlot,
								onClick = { onLoad(index) }
							) { Text(if (index == currentSlot) "使用中" else "读取") }
						}
					}
				}
			}
		)
	}

	@Composable
	private fun StatBar(label: String, value: Int, color: Color, maximum: Int = 120) {
		Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
			Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
				Text(label, style = MaterialTheme.typography.labelMedium)
				Text("$value${if (label == "好感" || label == "士气") " / $maximum" else ""}", style = MaterialTheme.typography.labelMedium)
			}
			LinearProgressIndicator(
				progress = { (value.toFloat() / maximum).coerceIn(0f, 1f) },
				modifier = Modifier.fillMaxWidth(),
				color = color,
				trackColor = Color(0xFFE6D8C9)
			)
		}
	}

	private fun milestoneProgress(milestone: String): Float = when (milestone) {
		"CSP-S" -> 0.25f
		"NOIP" -> 0.5f
		"省队" -> 0.75f
		"NOI" -> 1f
		else -> 0f
	}

	private fun sceneFor(sceneId: String) = when (sceneId) {
		"初一结束" -> org.example.project.plots.GradeOneEndingScene
		"NOIP后" -> org.example.project.plots.AfterNoipScene
		"省队前夜" -> if (gameState.newGamePlus) org.example.project.plots.NgPlusBeforeTeamScene else org.example.project.plots.BeforeTeamScene
		"NOI前夜" -> if (gameState.newGamePlus) org.example.project.plots.NgPlusBeforeNoiScene else org.example.project.plots.BeforeNoiScene
		"关系-WYM-4" -> org.example.project.plots.WymBondScene
			"关系-LZN-4" -> org.example.project.plots.LznBondScene
			"关系-ZHC-4" -> org.example.project.plots.ZhcBondScene
			"关系-LJD-4" -> org.example.project.plots.LjdBondScene
			"关系-WYM-8" -> org.example.project.plots.WymFavor8Scene
			"关系-LZN-8" -> org.example.project.plots.LznFavor8Scene
			"关系-ZHC-8" -> org.example.project.plots.ZhcFavor8Scene
			"关系-LJD-8" -> org.example.project.plots.LjdFavor8Scene
			"关系-WYM-10" -> org.example.project.plots.WymMidBondScene
		"关系-LZN-10" -> org.example.project.plots.LznMidBondScene
		"关系-ZHC-10" -> org.example.project.plots.ZhcMidBondScene
		"关系-LJD-10" -> org.example.project.plots.LjdMidBondScene
		"关系-WYM-12" -> org.example.project.plots.WymRelationshipScene
		"关系-LZN-12" -> org.example.project.plots.LznRelationshipScene
		"关系-ZHC-12" -> org.example.project.plots.ZhcRelationshipScene
		"关系-LJD-12" -> org.example.project.plots.LjdRelationshipScene
		"失败-CSP-S" -> org.example.project.plots.FailureScene
		"失败-NOIP" -> org.example.project.plots.NoipFailureScene
		"失败-省队" -> org.example.project.plots.TeamFailureScene
		"失败-NOI" -> org.example.project.plots.NoiFailureScene
		"CSP-S" -> org.example.project.plots.CspScene
		// 二周目：NOIP 与 NOI 走“记忆错位 → 崩解”的专属变体，其余场景复用一周目。
		"NOIP" -> if (gameState.newGamePlus) org.example.project.plots.NgPlusNoipScene else org.example.project.plots.NoipScene
		"省队" -> org.example.project.plots.TeamScene
		"NOI" -> if (gameState.newGamePlus) org.example.project.plots.NgPlusNoiScene else org.example.project.plots.NoiScene
		else -> error("未知重大场景: $sceneId")
	}

	private fun majorSceneTitle(sceneId: String) = when (sceneId) {
		"初一结束" -> "间章 · 暑假前夜"
		"NOIP后" -> "间章 · 成绩公布以后"
		"省队前夜" -> if (gameState.newGamePlus) "第三章 · 标签都掉了" else "第三章前 · 名额与名字"
		"NOI前夜" -> if (gameState.newGamePlus) "终章前 · 没有回答" else "终章前 · 走到这里以后"
		"关系-WYM-4" -> "吴一鸣 · 夜色里的年糕"
		"关系-LZN-4" -> "刘子诺 · 排名表的折痕"
		"关系-ZHC-4" -> "朱皓辰 · 留到最后的人"
		"关系-LJD-4" -> "李佳迪 · 一份诚实的报告"
		"关系-WYM-8" -> "吴一鸣 · 醒着做完的两道题"
		"关系-LZN-8" -> "刘子诺 · 先找自己的名字"
		"关系-ZHC-8" -> "朱皓辰 · 给王老师的资料"
		"关系-LJD-8" -> "李佳迪 · 不怕也不捧"
		"关系-WYM-10" -> "吴一鸣 · 醒着的理由"
		"关系-LZN-10" -> "刘子诺 · 不只是赢给你看"
		"关系-ZHC-10" -> "朱皓辰 · 最后一页"
		"关系-LJD-10" -> "李佳迪 · 值得被靠近"
		"关系-WYM-12" -> "吴一鸣 · 被看见的速度"
		"关系-LZN-12" -> "刘子诺 · 不再和自己比赛"
		"关系-ZHC-12" -> "朱皓辰 · 第一次开口"
		"关系-LJD-12" -> "李佳迪 · 不必永远第一"
		"失败-CSP-S" -> "坏结局 · CSP-S 的名单之外"
		"失败-NOIP" -> "坏结局 · NOIP 的名单之外"
		"失败-省队" -> "坏结局 · 省队的名单之外"
		"失败-NOI" -> "坏结局 · 最后一步的名单之外"
		"CSP-S" -> "第一章 · 雨中的名单"
		"NOIP" -> if (gameState.newGamePlus) "第二章 · 对不上的记忆" else "第二章 · 等待成绩的下午"
		"省队" -> "第三章 · 名单之外"
		"NOI" -> if (gameState.newGamePlus) "终章 · 我记不清她们了" else "终章 · 走向更远的地方"
		else -> sceneId
	}

	@Composable
	private fun EndingPanel(gameState: SaveFormat, navigator: Navigator, onChanged: () -> Unit) {
		var chooseTarget by remember { mutableStateOf(gameState.outcome == GameOutcome.ROMANCE_PENDING) }
		// 三周目入口：二周目走到 NOI 崩塌后，点击“再试一次”先确认，再进入纯叙事的三周目剧情。
		var showThirdPlaythrough by remember { mutableStateOf(false) }
		var showFourthPlaythrough by remember { mutableStateOf(false) }
		// 停在“NOI（NOI Au）失利没拿金牌”的失败结局那一刻，永久解锁二周目，
		// 并把本局成就并入全局记忆。即使之后覆盖或删除这个存档，解锁也不会丢失。
		LaunchedEffect(gameState.outcome) {
			if (gameState.isTopStageFailure()) {
				SaveRepository.recordTopStageFailureCleared(gameState.unlockedAchievements)
			}
		}
		Column(
			Modifier
				.fillMaxSize()
				.background(Color(0xFFF4F0E8))
				.verticalScroll(rememberScrollState())
				.padding(40.dp),
			verticalArrangement = Arrangement.spacedBy(20.dp)
		) {
			Text(
				when (gameState.outcome) {
					GameOutcome.BAD_ENDING -> "训练班结业：遗憾"
					GameOutcome.NORMAL_ENDING -> "结局：NOI"
					GameOutcome.FAMILY_ENDING -> "结局：合家欢"
					else -> "结局：心意"
				},
				style = MaterialTheme.typography.headlineLarge
			)
			Text(gameState.endingText, style = MaterialTheme.typography.bodyLarge)
			SummaryCard(gameState)
			if (gameState.outcome != GameOutcome.BAD_ENDING) {
				Text("多年以后", style = MaterialTheme.typography.titleLarge, color = Color(0xFF713821))
				gameState.students.forEach { student ->
					Text(
						futureNote(student.id),
						style = MaterialTheme.typography.bodyMedium,
						color = Color(0xFF5E5148)
					)
				}
			}
			if (gameState.outcome == GameOutcome.ROMANCE_PENDING) {
				Text("有人的好感已经到达阈值。请选择想要共同走下去的人：")
				Button(onClick = { chooseTarget = true }) { Text("选择恋爱对象") }
			}
			Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
				Button(onClick = { navigator.popUntilRoot() }) { Text("返回主菜单") }
				OutlinedButton(onClick = {
					val fresh = SaveFormat().apply { slot = gameState.slot }
					SaveRepository.save(fresh, fresh.slot)
					navigator.replaceAll(PlayScreenApp(fresh))
				}) { Text("重新开始（覆盖本存档位）") }
			}
			// 只有停在 NOI（NOI Au）失利没拿金牌的失败结局、且当前还不是二周目时，
			// 才在角落露出这个隐蔽的入口。这是首次进入二周目的唯一方式；解锁后也可从主菜单进入。
			if (gameState.isTopStageFailure() && !gameState.newGamePlus) {
				Row(
					Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.End
				) {
					TextButton(onClick = {
						val ngPlus = SaveFormat.newGamePlus(gameState.slot, gameState.unlockedAchievements)
						SaveRepository.save(ngPlus, ngPlus.slot)
						navigator.replaceAll(PlayScreenApp(ngPlus))
					}) {
						Text(
							"……如果能带着记忆，重来一次？",
							style = MaterialTheme.typography.bodySmall,
							color = Color(0xFF9C8A7A)
						)
					}
				}
			}
			// 二周目走到 NOI（“我记不清她们了”那一幕，如今必然以失利收场）之后，
			// 角落露出“再试一次”——三周目入口。点击后先确认，再进入纯叙事的三周目剧情。
			if (gameState.newGamePlus && "NOI" in gameState.completedMilestones) {
				Row(
					Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.End
				) {
					TextButton(onClick = { showThirdPlaythrough = true }) {
						Text(
							"再试一次",
							style = MaterialTheme.typography.bodySmall,
							color = Color(0xFF9C8A7A)
						)
					}
				}
			}
		}
		if (showThirdPlaythrough) {
			AlertDialog(
				onDismissRequest = { showThirdPlaythrough = false },
				title = { Text("再试一次") },
				text = { Text("“你确定，还要再来一次吗？”\n\n（这一次，没有经营，只有一段必须走完的记忆。）") },
				confirmButton = {
					TextButton(onClick = {
						showThirdPlaythrough = false
						navigator.push(RunPlotApp(
							plot = org.example.project.plots.ThirdPlaythroughScene,
							title = "三周目 · 循环"
						) {
							gameState.thirdPlaythroughCompleted = true
							SaveRepository.save(gameState, gameState.slot)
							onChanged()
						})
					}) { Text("……再来一次") }
				},
				dismissButton = {
					TextButton(onClick = { showThirdPlaythrough = false }) { Text("……再说吧") }
				}
			)
		}
		// 四周目入口：三周目完成后，角落露出"我看见你了"。点击后进入纯叙事的四周目剧情。
		if (gameState.thirdPlaythroughCompleted) {
			Row(
				Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.End
			) {
				TextButton(onClick = { showFourthPlaythrough = true }) {
					Text(
						"我看见你了",
						style = MaterialTheme.typography.bodySmall,
						color = Color(0xFF9C8A7A)
					)
				}
			}
		}
		if (showFourthPlaythrough) {
			AlertDialog(
				onDismissRequest = { showFourthPlaythrough = false },
				title = { Text("四周目 · 我看见你了") },
				text = { Text("这一次，从吴一鸣的视角，重新走进那间教室。\n\n（她保留了三周目的情感碎片——不是清晰的记忆，而是直觉、身体记忆、对特定话语的过度敏感。）") },
				confirmButton = {
					TextButton(onClick = {
						showFourthPlaythrough = false
						navigator.push(RunPlotApp(
							plot = org.example.project.plots.FourthPlaythroughScene,
							title = "四周目 · 我看见你了"
						))
					}) { Text("走进那间教室") }
				},
				dismissButton = {
					TextButton(onClick = { showFourthPlaythrough = false }) { Text("……先不了") }
				}
			)
		}
		if (chooseTarget && gameState.outcome == GameOutcome.ROMANCE_PENDING) {
			AlertDialog(
				onDismissRequest = { chooseTarget = false },
				title = { Text("选择恋爱结局") },
				confirmButton = {},
				text = {
					Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
						gameState.students.filter { it.favor >= 12 }.forEach { student ->
							Button(
								onClick = {
									gameState.chooseRomance(student.id)
									chooseTarget = false
									onChanged()
								},
								modifier = Modifier.fillMaxWidth()
							) {
								Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
									Portraits.characterForStudentId(student.id)?.let { character ->
										Portraits.SpeakerAvatar(speaker = character, size = 36.dp, showBorder = false)
									}
									Text(student.name)
								}
							}
						}
					}
				}
			)
		}
	}

	/**
	 * 结营小结：无论结局好坏，都用一张卡片回顾这一届四个人最终的四维数据，
	 * 以及最后一次里程碑比赛的名次与临场表现，给整局游戏一个数据层面的收束。
	 */
	@Composable
	private fun SummaryCard(gameState: SaveFormat) {
		Card(
			Modifier.fillMaxWidth(),
			colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF7))
		) {
			Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
				Text("结营小结", style = MaterialTheme.typography.titleLarge, color = Color(0xFF713821))
				Text(
					"第 ${gameState.week} 周　最终里程碑：${gameState.currentMilestone}",
					style = MaterialTheme.typography.bodySmall,
					color = Color(0xFF76685B)
				)
				gameState.students.forEach { student ->
					Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
						Portraits.characterForStudentId(student.id)?.let { character ->
							Portraits.SpeakerAvatar(speaker = character, size = 44.dp)
						}
						Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
							Text(student.name, style = MaterialTheme.typography.titleSmall, color = Color(0xFF713821))
							Text(
								"水平 ${student.ability}　科技 ${student.technology}　好感 ${student.favor}　心态 ${student.morale}/10",
								style = MaterialTheme.typography.bodyMedium,
								color = Color(0xFF5E5148)
							)
						}
					}
				}
				val finalContest = gameState.lastCompetition
				if (finalContest.isNotEmpty()) {
					Text(
						"最后一次比赛（${finalContest.first().milestone}）",
						style = MaterialTheme.typography.titleSmall,
						color = Color(0xFF713821)
					)
					finalContest.forEach { result ->
						Text(
							"第 ${result.rank} 名　${result.studentName}　${result.score} 分 · ${result.performance}",
							style = MaterialTheme.typography.bodySmall,
							color = Color(0xFF5E5148)
						)
					}
				}
			}
		}
	}

	private fun futureNote(studentId: String): String = when (studentId) {
		"WYM" -> "吴一鸣后来仍然很容易困。她没有变成一个永远高效的人，而是学会在需要休息时承认疲惫，在醒来的时候认真做完眼前的事。她偶尔会发来年糕的新照片，也会在照片下面附上一道自己最近觉得有趣的题。"
		"LZN" -> "刘子诺后来成了一个很会带新人的人。她仍然会紧张，仍然会在比赛前说很多话，但她不再把失误叫作命运，也不再把别人的名字当成自己的影子。她最常对后辈说的一句话是：先别急，检查完再哭。"
		"ZHC" -> "朱皓辰后来走上了研究和教学的路。她依旧沉默，依旧会在别人说完后停很久再回答，却终于不再把沉默当成坚强的证明。她的笔记本里一直保留着那句旧话：错误不是失败的证据，是下一次训练的入口。"
		"LJD" -> "李佳迪后来不再执着于每一张榜单的第一名。她仍然会追逐难题，也仍然有锋利得让人惊讶的解法，只是当有人超过她时，她终于可以先说一句恭喜，再回到自己的问题里。"
		else -> "她们都带着那段周日的时光继续向前。"
	}
}
