package org.example.project.runPlot

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.delay
import org.example.project.AppConfig
import org.example.project.plotsFormat.Characters
import org.example.project.plotsFormat.PlotTree
import org.example.project.plotsFormat.PlotTreePointer
import org.example.project.saves.SaveFormat
import org.example.project.saves.SaveRepository
import org.example.project.saves.SettingsHolder

/**
 * 剧情播放界面。
 *
 * 当传入 [gameState] 时，本界面会与存档系统联动：
 *  - 进入时若存档记录了 [SaveFormat.majorSceneNodeId]，从该节点续播而非从头重播；
 *  - 每推进一个普通节点，会记录已读（[SaveFormat.seenPlotNodes]）并更新当前节点，便于中断后续播；
 *  - 工具栏提供“保存进度”按钮；
 *  - “跳过已读”会依据设置只略过读过的普通节点，遇到新内容或选项停下。
 * 不传 [gameState] 时（如序章之外的独立预览）则退化为纯播放。
 */
class RunPlotApp(
	val plot: PlotTree,
	val title: String = "剧情",
	val gameState: SaveFormat? = null,
	val onChoice: (String) -> Unit = {},
	val onFinished: () -> Unit = {}
) : Screen {
	/** 已经播放过的对白记录，供历史回看。 */
	private data class HistoryLine(val speaker: Characters, val content: String)

	@Composable
	override fun Content() {
		val navigator = LocalNavigator.currentOrThrow
		val plotPointer = remember {
			PlotTreePointer(plot).also { pointer ->
				// 从存档记录的节点续播（若该节点仍存在于本剧情树中）。
				gameState?.majorSceneNodeId?.let { pointer.jumpToNode(it) }
			}
		}

		// 逐字动画：当前节点已经显示到第几个字符。
		var revealed by remember { mutableStateOf(0) }
		// 自动播放开关。
		var autoPlay by remember { mutableStateOf(false) }
		// 历史面板显隐。
		var showHistory by remember { mutableStateOf(false) }
		val history = remember { mutableStateListOf<HistoryLine>() }

		Box(
			modifier = Modifier.fillMaxSize().background(Color(0xFF2E2927)),
			contentAlignment = Alignment.BottomCenter
		) {
			// 立绘层：在对话框后面、靠底部显示当前说话人的立绘。缺图时自动隐藏。
			(plotPointer.getCurrentNode() as? PlotTree.SingleNode.Normal)?.let { node ->
				Portraits.SpeakerPortrait(
					speaker = node.speaker,
					modifier = Modifier
						.align(Alignment.BottomCenter)
						.fillMaxHeight(0.85f)
						.padding(bottom = AppConfig.windowHeight / 4)
				)
			}
			Text(
				text = title,
				color = Color(0xFFFFD9C8),
				fontSize = AppConfig.smallFontSize,
				modifier = Modifier.align(Alignment.TopStart).padding(24.dp)
			)
			DialogContent(
				navigator = navigator,
				plotPointer = plotPointer,
				revealed = revealed,
				revealedProvider = { revealed },
				onReveal = { revealed = it },
				history = history,
				autoPlay = autoPlay
			)
			Toolbar(
				navigator = navigator,
				plotPointer = plotPointer,
				revealed = revealed,
				onReveal = { revealed = it },
				autoPlay = autoPlay,
				onToggleAuto = { autoPlay = it },
				onShowHistory = { showHistory = true }
			)
		}

		if (showHistory) {
			HistoryDialog(history) { showHistory = false }
		}
	}

	@Composable
	private fun DialogContent(
		navigator: Navigator,
		plotPointer: PlotTreePointer,
		revealed: Int,
		revealedProvider: () -> Int,
		onReveal: (Int) -> Unit,
		history: MutableList<HistoryLine>,
		autoPlay: Boolean
	) {
		when (val currentNode = plotPointer.getCurrentNode()) {
			is PlotTree.SingleNode.Normal -> {
				NormalNode(currentNode, plotPointer, navigator, revealed, revealedProvider, onReveal, history, autoPlay)
			}
			is PlotTree.SingleNode.Option -> {
				OptionNode(currentNode, plotPointer, navigator)
			}
			is PlotTree.SingleNode.Custom -> {
				currentNode.block()
			}
		}
	}

	/** 记录当前节点到存档（续播用），并标记该节点已读（跳过已读用）。 */
	private fun trackNode(node: PlotTree.SingleNode) {
		val state = gameState ?: return
		val nodeId = when (node) {
			is PlotTree.SingleNode.Normal -> node.id
			is PlotTree.SingleNode.Option -> node.id
			is PlotTree.SingleNode.Custom -> node.id
		}
		state.rememberMajorSceneNode(nodeId)
		if (node is PlotTree.SingleNode.Normal) {
			state.markPlotNodeSeen(title, node.id)
		}
	}

	private fun advance(
		plotPointer: PlotTreePointer,
		navigator: Navigator
	) {
		if (!plotPointer.nextNode()) {
			onFinished()
			navigator.pop()
		}
	}

	@Composable
	private fun NormalNode(
		currentNode: PlotTree.SingleNode.Normal,
		plotPointer: PlotTreePointer,
		navigator: Navigator,
		revealed: Int,
		revealedProvider: () -> Int,
		onReveal: (Int) -> Unit,
		history: MutableList<HistoryLine>,
		autoPlay: Boolean
	) {
		val fullText = currentNode.content
		val isComplete = revealed >= fullText.length
		val textSpeed = SettingsHolder.settings.textSpeedMillis
		val autoDelay = SettingsHolder.settings.autoPlayDelayMillis

		// 每进入一个新节点，记录续播/已读信息、重置逐字进度并记录到历史。
		androidx.compose.runtime.LaunchedEffect(currentNode.id) {
			trackNode(currentNode)
			history.add(HistoryLine(currentNode.speaker, fullText))
			if (textSpeed <= 0) {
				// 瞬间显示：直接拉满。
				onReveal(fullText.length)
				return@LaunchedEffect
			}
			onReveal(0)
			var shown = 0
			while (shown < fullText.length) {
				delay(textSpeed.toLong())
				// 若玩家点击立即显示全文，revealed 会被外部提前拉满，此时结束动画。
				if (revealedProvider() >= fullText.length) break
				shown++
				onReveal(shown)
			}
		}

		// 自动播放：文字显示完毕后停顿再前进。
		androidx.compose.runtime.LaunchedEffect(currentNode.id, isComplete, autoPlay) {
			if (autoPlay && isComplete) {
				delay(autoDelay.toLong())
				advance(plotPointer, navigator)
			}
		}

		Column(
			modifier = Modifier
				.padding(bottom = AppConfig.windowHeight / 16)
				.heightIn(min = AppConfig.windowHeight / 3)
				.background(Color(0xFFFFFCF7), shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
				.fillMaxWidth()
				.clickable(
					interactionSource = remember { MutableInteractionSource() },
					indication = null
				) {
					// 未显示完时先补全，显示完后再前进。
					if (!isComplete) {
						onReveal(fullText.length)
					} else {
						advance(plotPointer, navigator)
					}
				}
		) {
			if (currentNode.speaker.displayName.isNotEmpty()) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier.padding(AppConfig.windowWidth / 96)
				) {
					// 有头像就显示圆形头像，缺图时自动跳过、仅显示名字。
					val hasAvatar = Portraits.SpeakerAvatar(
						speaker = currentNode.speaker,
						size = AppConfig.mediumFontSize.value.dp * 1.8f
					)
					if (hasAvatar) {
						Spacer(modifier = Modifier.width(10.dp))
					}
					Text(
						text = "【${currentNode.speaker.displayName}】",
						fontSize = AppConfig.mediumFontSize
					)
				}

				HorizontalDivider(
					thickness = 1.dp,
					color = Color.Gray,
					modifier = Modifier.fillMaxWidth()
				)
			}

			Text(
				text = fullText.take(revealed.coerceIn(0, fullText.length)),
				fontSize = AppConfig.mediumFontSize,
				modifier = Modifier.padding(AppConfig.windowWidth / 32)
			)
			Text(
				text = if (isComplete) "点击任意位置继续" else "点击显示全部",
				fontSize = AppConfig.smallFontSize,
				color = Color(0xFF9A4D32),
				modifier = Modifier.align(Alignment.End).padding(horizontal = 24.dp, vertical = 12.dp)
			)
		}
	}

	@Composable
	fun OptionNode(
		currentNode: PlotTree.SingleNode.Option,
		plotPointer: PlotTreePointer,
		navigator: Navigator
	) {
		androidx.compose.runtime.LaunchedEffect(currentNode.id) { trackNode(currentNode) }
		Column(
			modifier = Modifier
				.padding(bottom = AppConfig.windowHeight / 16)
				.heightIn(min = AppConfig.windowHeight / 3)
				.background(Color(0xFFFFF8DC))
		) {
			Text(
				text = "【${currentNode.question}】",
				fontSize = AppConfig.mediumFontSize,
				modifier = Modifier.padding(AppConfig.windowWidth / 96)
			)

			HorizontalDivider(
				thickness = 1.dp,
				color = Color.Gray,
				modifier = Modifier.fillMaxWidth()
			)

			Column(
				modifier = Modifier.padding(AppConfig.windowWidth / 32),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				currentNode.options.forEach { option ->
					Text(
						text = option.content,
						fontSize = AppConfig.mediumFontSize,
						modifier = Modifier
							.fillMaxWidth()
							.clickable {
								onChoice(option.content)
								if (!plotPointer.nextNode(option)) {
									onFinished()
									navigator.pop()
								}
							}
							.background(
								Color(0xFFF5F5DC),
								shape = RoundedCornerShape(4.dp)
							)
							.padding(12.dp)
					)
				}
			}
		}
	}

	@Composable
	fun Toolbar(
		navigator: Navigator,
		plotPointer: PlotTreePointer,
		revealed: Int,
		onReveal: (Int) -> Unit,
		autoPlay: Boolean,
		onToggleAuto: (Boolean) -> Unit,
		onShowHistory: () -> Unit
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.height(AppConfig.windowHeight / 16)
				.background(Color(0xFFFFE4E1)),
			horizontalArrangement = Arrangement.Center
		) {
			NextIcon(navigator, plotPointer, revealed, onReveal)
			SkipReadIcon(navigator, plotPointer)
			SkipNextIcon(navigator, plotPointer)
			AutoIcon(autoPlay, onToggleAuto)
			HistoryIcon(onShowHistory)
			if (gameState != null) SaveIcon()
			HomeIcon(navigator)
		}
	}

	@OptIn(ExperimentalMaterial3Api::class)
	@Composable
	private fun ToolbarIcon(
		tooltip: String,
		filled: androidx.compose.ui.graphics.vector.ImageVector,
		outlined: androidx.compose.ui.graphics.vector.ImageVector,
		tint: Color = Color.Unspecified,
		onClick: (() -> Unit)?
	) {
		val interactionSource = remember { MutableInteractionSource() }
		val isHovered by interactionSource.collectIsHoveredAsState()
		val tooltipState = rememberTooltipState(isPersistent = false)

		TooltipBox(
			positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
			state = tooltipState,
			tooltip = {
				Box(
					modifier = Modifier
						.background(Color(0xFF333333), shape = RoundedCornerShape(4.dp))
						.padding(horizontal = 8.dp, vertical = 4.dp)
				) {
					Text(text = tooltip, color = Color.White, fontSize = AppConfig.smallFontSize)
				}
			}
		) {
			val base = Modifier.fillMaxHeight().padding(horizontal = 8.dp).hoverable(interactionSource)
			Icon(
				imageVector = if (isHovered) filled else outlined,
				tint = tint,
				contentDescription = tooltip,
				modifier = if (onClick != null) base.clickable { onClick() } else base
			)
		}
	}

	@Composable
	fun NextIcon(
		navigator: Navigator,
		plotPointer: PlotTreePointer,
		revealed: Int,
		onReveal: (Int) -> Unit
	) {
		val isOption = plotPointer.getCurrentNode() is PlotTree.SingleNode.Option
		ToolbarIcon(
			tooltip = "下一句",
			filled = Icons.AutoMirrored.Filled.ArrowForwardIos,
			outlined = Icons.AutoMirrored.Outlined.ArrowForwardIos,
			tint = if (isOption) Color.Gray else Color.Unspecified,
			onClick = if (isOption) null else {
				{
					val node = plotPointer.getCurrentNode()
					if (node is PlotTree.SingleNode.Normal && revealed < node.content.length) {
						onReveal(node.content.length)
					} else {
						advance(plotPointer, navigator)
					}
				}
			}
		)
	}

	/**
	 * 跳过已读：连续前进，只要下一节点仍是“已读的普通节点”就继续跳，遇到未读内容或选项停下。
	 * 需开启设置中的“跳过已读文本”，且当前处于与存档联动的场景（否则没有已读记录）。
	 */
	@Composable
	fun SkipReadIcon(
		navigator: Navigator,
		plotPointer: PlotTreePointer
	) {
		val enabled = gameState != null && SettingsHolder.settings.skipReadOnly
		ToolbarIcon(
			tooltip = if (enabled) "跳过已读" else "跳过已读（未开启或不可用）",
			filled = Icons.Filled.FastForward,
			outlined = Icons.Outlined.FastForward,
			tint = if (enabled) Color.Unspecified else Color.Gray,
			onClick = if (!enabled) null else {
				{
					// 至少前进一步，随后持续跳过已读的普通节点。
					// enabled 已保证 gameState 非空，编译器据此对其智能转换。
					var moved = false
					while (true) {
						if (!plotPointer.nextNode()) {
							onFinished()
							navigator.pop()
							break
						}
						moved = true
						val node = plotPointer.getCurrentNode()
						val skippable = node is PlotTree.SingleNode.Normal &&
							gameState.isPlotNodeSeen(title, node.id)
						if (!skippable) break
					}
					if (moved) trackNode(plotPointer.getCurrentNode())
				}
			}
		)
	}

	@OptIn(ExperimentalMaterial3Api::class)
	@Composable
	fun SkipNextIcon(
		navigator: Navigator,
		plotPointer: PlotTreePointer
	) {
		var showDialog by remember { mutableStateOf(false) }
		ToolbarIcon(
			tooltip = "快进到下一个选项",
			filled = Icons.Filled.SkipNext,
			outlined = Icons.Outlined.SkipNext,
			onClick = { showDialog = true }
		)

		if (showDialog) {
			AlertDialog(
				onDismissRequest = { showDialog = false },
				title = { Text("确认快进") },
				text = { Text("确定要快进到下一个选项吗？") },
				confirmButton = {
					TextButton(
						onClick = {
							showDialog = false
							while (plotPointer.getCurrentNode() !is PlotTree.SingleNode.Option) {
								if (!plotPointer.nextNode()) {
									onFinished()
									navigator.pop()
									break
								}
							}
						}
					) { Text("确定") }
				},
				dismissButton = {
					TextButton(onClick = { showDialog = false }) { Text("取消") }
				}
			)
		}
	}

	@Composable
	fun AutoIcon(autoPlay: Boolean, onToggleAuto: (Boolean) -> Unit) {
		ToolbarIcon(
			tooltip = if (autoPlay) "停止自动播放" else "自动播放",
			filled = if (autoPlay) Icons.Filled.Stop else Icons.Filled.PlayArrow,
			outlined = if (autoPlay) Icons.Filled.Stop else Icons.Outlined.PlayArrow,
			tint = if (autoPlay) Color(0xFF9A4D32) else Color.Unspecified,
			onClick = { onToggleAuto(!autoPlay) }
		)
	}

	@Composable
	fun HistoryIcon(onShowHistory: () -> Unit) {
		ToolbarIcon(
			tooltip = "历史回看",
			filled = Icons.Filled.History,
			outlined = Icons.Outlined.History,
			onClick = onShowHistory
		)
	}

	/** 保存进度：把当前节点写入存档并落盘，中断后可从此处续播。 */
	@Composable
	fun SaveIcon() {
		var showToast by remember { mutableStateOf(false) }
		ToolbarIcon(
			tooltip = "保存进度",
			filled = Icons.Filled.Save,
			outlined = Icons.Outlined.Save,
			onClick = {
				gameState?.let { state ->
					SaveRepository.save(state, state.slot)
					showToast = true
				}
			}
		)
		if (showToast) {
			AlertDialog(
				onDismissRequest = { showToast = false },
				title = { Text("已保存") },
				text = { Text("当前剧情进度已保存到存档位 ${(gameState?.slot ?: 0) + 1}，下次会从这一句继续。") },
				confirmButton = { TextButton(onClick = { showToast = false }) { Text("好") } }
			)
		}
	}

	@Composable
	fun HomeIcon(navigator: Navigator) {
		var showDialog by remember { mutableStateOf(false) }
		ToolbarIcon(
			tooltip = "返回主菜单",
			filled = Icons.Filled.Home,
			outlined = Icons.Outlined.Home,
			onClick = { showDialog = true }
		)

		if (showDialog) {
			AlertDialog(
				onDismissRequest = { showDialog = false },
				title = { Text("返回主菜单") },
				text = {
					Text(
						if (gameState != null) "当前剧情进度会先保存，下次可从这一句继续。确定返回主菜单吗？"
						else "确定要中断当前剧情返回主菜单吗？未完成的剧情下次会重新播放。"
					)
				},
				confirmButton = {
					TextButton(onClick = {
						showDialog = false
						gameState?.let { SaveRepository.save(it, it.slot) }
						navigator.popUntilRoot()
					}) { Text("确定") }
				},
				dismissButton = {
					TextButton(onClick = { showDialog = false }) { Text("取消") }
				}
			)
		}
	}

	@Composable
	private fun HistoryDialog(history: List<HistoryLine>, onDismiss: () -> Unit) {
		AlertDialog(
			onDismissRequest = onDismiss,
			title = { Text("历史回看") },
			confirmButton = {
				TextButton(onClick = onDismiss) { Text("关闭") }
			},
			text = {
				if (history.isEmpty()) {
					Text("还没有可回看的对白。")
				} else {
					LazyColumn(
						modifier = Modifier.fillMaxWidth().heightIn(max = AppConfig.windowHeight / 2),
						verticalArrangement = Arrangement.spacedBy(10.dp)
					) {
						items(history) { line ->
							Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
								// 每句台词旁显示说话人头像；缺图（含旁白/独白）自动不占位。
								Portraits.SpeakerAvatar(speaker = line.speaker, size = 32.dp)
								Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
									if (line.speaker.displayName.isNotEmpty()) {
										Text("【${line.speaker.displayName}】", fontSize = AppConfig.smallFontSize, color = Color(0xFF9A4D32))
									}
									Text(line.content, fontSize = AppConfig.smallFontSize)
								}
							}
						}
					}
				}
			}
		)
	}
}
