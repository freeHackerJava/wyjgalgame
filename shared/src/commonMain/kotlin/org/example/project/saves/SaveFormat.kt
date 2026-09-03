package org.example.project.saves

import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class StudentState(
	val id: String,
	val name: String,
	val description: String,
	var ability: Int = 42,
	var technology: Int = 38,
	var favor: Int = 0,
	val growthRate: Int = 1,
	var stability: Int = 3,
	/** 士气/心态：影响比赛临场发挥与是否容易在压力下崩盘。0-10，默认 5。 */
	var morale: Int = 5
)

@Serializable
enum class GameOutcome {
	PLAYING,
	BAD_ENDING,
	NORMAL_ENDING,
	ROMANCE_PENDING,
	ROMANCE_ENDING,
	FAMILY_ENDING
}

@Serializable
data class CompetitionResult(
	val milestone: String,
	val studentId: String,
	val studentName: String,
	val score: Int,
	val rank: Int,
	val performance: String
)

@Serializable
class SaveFormat {
	/** 当前存档所在的槽位，仅运行时使用，不写入存档文件。 */
	@Transient
	var slot: Int = 0
	var date: LocalDate = LocalDate(2024, 9, 1)
	var week: Int = 1
	var actionPoints: Int = 3
	var currentMilestone: String = "CSP-S"
	var students: MutableList<StudentState> = mutableListOf(
		StudentState("WYM", "吴一鸣", "白色长发，红色瞳孔。慵懒嗜睡，家里养着年糕。", 43, 39, 0, 1, 2, morale = 5),
		StudentState("LZN", "刘子诺", "粉色短发，黄色瞳孔。活泼健谈，发挥不太稳定。", 42, 37, 0, 2, 1, morale = 4),
		StudentState("ZHC", "朱皓辰", "黑色长发，黑色瞳孔。沉默稳重，常常发呆。", 41, 38, 0, 1, 5, morale = 6),
		StudentState("LJD", "李佳迪", "银色长发，蓝色瞳孔。高冷而聪慧，是四人中最强的。", 49, 45, 0, 2, 4, morale = 5)
	)
	var finishedPrologue: Boolean = false
	var outcome: GameOutcome = GameOutcome.PLAYING
	var romanceTargetId: String? = null
	var endingText: String = ""
	var eventId: String? = null
	var eventSpeaker: String = ""
	var eventText: String = ""
	var seenEvents: MutableSet<String> = mutableSetOf()
	var lastActionText: String = ""
	var pendingMajorScene: String? = null
	var deferredMajorScene: String? = null
	/** 好感阈值达标后、玩家尚未选择"进入好感剧情"时，暂存待播放的好感场景 id。 */
	var pendingFavorScene: String? = null
	/** 当前正在播放的重大场景已经推进到的节点 id，用于中断后从原处续播。 */
	var majorSceneNodeId: String? = null
	var seenMajorScenes: MutableSet<String> = mutableSetOf()
	/** 已经读过的剧情节点（跨场景累计），供"跳过已读文本"使用。key 形如 "剧情标题#节点id"。 */
	var seenPlotNodes: MutableSet<String> = mutableSetOf()
	var completedMilestones: MutableSet<String> = mutableSetOf()
	var storyChoices: MutableList<String> = mutableListOf()
	var lastCompetition: MutableList<CompetitionResult> = mutableListOf()
	/** 已经触发过的随机突发事件 id，保证同一事件不会重复出现。 */
	var seenRandomEvents: MutableSet<String> = mutableSetOf()
	/** 已解锁的成就 id。 */
	var unlockedAchievements: MutableSet<String> = mutableSetOf()
	/** 已选中但因弹窗被脚本周常事件占用而排队等待展示的随机事件 id。 */
	var pendingRandomEventId: String? = null
	/** 本周是否已经进行过“谈心”或“模拟赛”这类每周一次的特殊行动。 */
	var heartToHeartDoneThisWeek: Boolean = false
	var mockContestDoneThisWeek: Boolean = false
	/** 最近一次模拟赛的结果，用于训练档案回顾。 */
	var lastMockContest: MutableList<CompetitionResult> = mutableListOf()
	/** 累计带学生散步的次数，用于成就判定。 */
	var walkCount: Int = 0
	/** 累计组织集体复盘的次数，用于成就判定。 */
	var reviewCount: Int = 0
	/**
	 * 二周目（New Game+）标记。为 true 时，王怡钧带着“上一世的记忆”重来：
	 * 关键场景（序章、里程碑、结局）会走二周目变体，营造“以为能拯救一切、
	 * 最终发现无力改变”的叙事；部分关键选项的收益也会被削弱。
	 * 只有在 NOI（NOI Au）阶段失利、没能拿到金牌的失败结局里，才能进入二周目。
	 */
	var newGamePlus: Boolean = false
	/** 三周目是否已完成。完成后可在结局面板解锁四周目入口。 */
	var thirdPlaythroughCompleted: Boolean = false

	/**
	 * 是否停在了“NOI（NOI Au）阶段失利、没拿到金牌”的失败结局上——
	 * 即在最高一级的比赛里没能让所有人达到金牌线。这是解锁二周目的唯一入口：
	 * 王怡钧正因为在这一步输了，才会想“带着记忆重来一次”。
	 */
	fun isTopStageFailure(): Boolean =
		outcome == GameOutcome.BAD_ENDING && lastCompetition.firstOrNull()?.milestone == "NOI"

	fun spend(action: String, studentId: String? = null): Boolean {
		if (actionPoints <= 0) return false
		val student = studentId?.let { id -> students.firstOrNull { it.id == id } }
		when (action) {
			"teach" -> student?.let {
				it.technology += 4
				it.ability += it.growthRate
				lastActionText = "你为${it.name}讲解了一部分新科技。她的知识储备增加了。"
			} ?: return false
			"coach" -> student?.let {
				if (pendingFavorScene != null) return false
				it.ability += 4
				it.favor += 1
				triggerFavorEvent(it)
				if (eventId == null) lastActionText = "你和${it.name}单独复盘了错题。她更信任你了。"
			} ?: return false
			"talk" -> student?.let {
				if (heartToHeartDoneThisWeek) return false
				if (pendingFavorScene != null) return false
				it.morale = (it.morale + 3).coerceAtMost(10)
				it.stability += 1
				it.favor += 1
				heartToHeartDoneThisWeek = true
				triggerFavorEvent(it)
				if (eventId == null) lastActionText = "你陪${it.name}聊了聊比赛之外的事。她放松了不少，心态更稳了。"
			} ?: return false
			"mock" -> {
				if (mockContestDoneThisWeek) return false
				lastMockContest = runCompetition("模拟赛")
				students.forEach {
					it.ability += 1
					// 模拟赛让强者建立信心、弱者积累压力，士气随名次浮动。
					val rank = lastMockContest.first { r -> r.studentId == it.id }.rank
					it.morale = (it.morale + if (rank <= 2) 1 else -1).coerceIn(0, 10)
				}
				mockContestDoneThisWeek = true
				val top = lastMockContest.first()
				lastActionText = "你组织了一场四小时模拟赛。${top.studentName}拿到了本场最高分，四个人都在复盘各自的失误。"
			}
			"prepare" -> {
				students.forEach { it.technology += 1 }
				lastActionText = "你整理了下周的讲义，四个人都提前接触到了新内容。"
			}
			"walk" -> {
				if (heartToHeartDoneThisWeek) return false
				if (pendingFavorScene != null) return false
				// 散步是集体放松：全员士气与好感小幅提升，但和“谈心”共享每周一次的额度。
				students.forEach {
					it.morale = (it.morale + 1).coerceAtMost(10)
					it.favor += 1
				}
				heartToHeartDoneThisWeek = true
				walkCount++
				lastActionText = "你带四个人沿着合肥的街边散了会儿步。没有讲题，只是聊天，回来的路上大家的脚步都轻快了不少。"
			}
			"review" -> {
				// 集体复盘：把上周的错题摆到一起讲，稳步提升水平与稳定性。
				students.forEach {
					it.ability += 2
					it.stability += 1
				}
				reviewCount++
				lastActionText = "你把四个人上周的错题放在一起集体复盘。相似的坑，一次讲清，谁也没再重复踩第二遍。"
			}
			// —— 二周目专属的两种集体行动：精简界面里只剩它们和“结束本周”。——
			// 收益刻意压得很低：即便一周把三点行动全砸在同一件事上，也追不回每周的衰减。
			// 玩家能做的只是让下滑慢一点点——“记忆崩坏”里，努力有意义，但改变不了结局。
			"ngplus-ability" -> {
				// 集体提升水平：凭记忆替四个人挑好该练的题，但记忆已经开始对不上了。
				students.forEach { it.ability += 1 }
				lastActionText = "你按记忆里的题单带四个人练了一轮。可有几道题，你怎么也想不起当初是怎么讲通的了。"
			}
			"ngplus-tech" -> {
				// 集体讲授科技：想把记得的知识点讲透，却发现记忆正在一处处塌陷。
				students.forEach { it.technology += 1 }
				lastActionText = "你讲这一阶段该讲的科技。讲到一半，你忽然记不清下一个知识点——四个人抬头看你，你笑了笑，翻回讲义。"
			}
			else -> return false
		}
		actionPoints--
		return true
	}

	fun endWeek() {
		if (outcome != GameOutcome.PLAYING) return
		date = date.plus(7, kotlinx.datetime.DateTimeUnit.DAY)
		week++
		actionPoints = 3
		heartToHeartDoneThisWeek = false
		mockContestDoneThisWeek = false
		students.forEach { student ->
			student.ability = (student.ability - 1 + weeklyPerformance(student)).coerceAtLeast(0)
			// 长期高压会缓慢消磨士气，需要靠谈心/模拟赛表现来维持。
			student.morale = (student.morale - moraleDrift(student)).coerceIn(0, 10)
		}
		// 二周目：记忆正在崩坏，路怎么铺都铺不平。每周额外重挫全员水平与科技——
		// 这份衰减刻意大于两种集体行动的收益之和，让玩家“怎么补都补不上”：数值一路下滑，
		// 越接近终点越无力。这既是“过程也真的很难”的难度体现，也是“无力改变”叙事的落点。
		// 前面的里程碑仍会叙事放行（见 checkMilestone），保证故事一定能走到 NOI 崩塌那一刻。
		if (newGamePlus) {
			students.forEach { student ->
				student.ability = (student.ability - 5).coerceAtLeast(0)
				student.technology = (student.technology - 5).coerceAtLeast(0)
			}
		}

		lastActionText = "本周课程结束。你检查了四个人的作业，准备迎接下一次周日。"
		checkMilestone()
		if (outcome == GameOutcome.PLAYING) {
			triggerWeeklyEvent()
			// 无论脚本周常事件是否触发，都尝试推进随机事件队列：
			// 到达节奏时先入队，弹窗空闲时再展示，避免被脚本事件长期挤占。
			triggerRandomEvent()
		}
		if (outcome == GameOutcome.PLAYING && !newGamePlus && week == 40 && "初一结束" !in seenMajorScenes) {
			pendingMajorScene = "初一结束"
		}
		if (outcome == GameOutcome.PLAYING && !newGamePlus && week == 130 && "NOIP后" !in seenMajorScenes) {
			pendingMajorScene = "NOIP后"
		}
		if (outcome == GameOutcome.PLAYING && !newGamePlus && week == 150 && "省队前夜" !in seenMajorScenes) {
			pendingMajorScene = "省队前夜"
		}
		if (outcome == GameOutcome.PLAYING && !newGamePlus && week == 185 && "NOI前夜" !in seenMajorScenes) {
			pendingMajorScene = "NOI前夜"
		}
		// 二周目压缩后的插叙触发点：省队与 NOI 前夜提前到位，走“记忆崩坏”的专属变体。
		if (outcome == GameOutcome.PLAYING && newGamePlus && week == 40 && "省队前夜" !in seenMajorScenes) {
			pendingMajorScene = "省队前夜"
		}
		if (outcome == GameOutcome.PLAYING && newGamePlus && week == 55 && "NOI前夜" !in seenMajorScenes) {
			pendingMajorScene = "NOI前夜"
		}
	}

	fun dismissEvent() {
		eventId?.let { seenEvents.add(it) }
		eventId = null
		eventSpeaker = ""
		eventText = ""
		if (pendingMajorScene == null) {
			pendingMajorScene = deferredMajorScene
			deferredMajorScene = null
		}
	}

	fun finishMajorScene() {
		pendingMajorScene?.let { seenMajorScenes.add(it) }
		pendingMajorScene = deferredMajorScene
		deferredMajorScene = null
		majorSceneNodeId = null
	}

	/** 记录当前重大场景已推进到的节点，供中断后续播。 */
	fun rememberMajorSceneNode(nodeId: String) {
		majorSceneNodeId = nodeId
	}

	/** 标记某条剧情节点为已读。key 形如 "标题#节点id"。 */
	fun markPlotNodeSeen(plotTitle: String, nodeId: String) {
		seenPlotNodes.add("$plotTitle#$nodeId")
	}

	/** 判断某条剧情节点是否已读。 */
	fun isPlotNodeSeen(plotTitle: String, nodeId: String): Boolean =
		"$plotTitle#$nodeId" in seenPlotNodes

	fun chooseRomance(studentId: String) {
		if (outcome != GameOutcome.ROMANCE_PENDING) return
		if (students.any { it.id == studentId && it.favor >= 12 }) {
			romanceTargetId = studentId
			outcome = GameOutcome.ROMANCE_ENDING
			endingText = "多年后，你和已经成年的${students.first { it.id == studentId }.name}在合肥重逢。那些年周日的陪伴没有被时间带走，你们终于以平等的身份确认了彼此的心意。"
		}
	}

	fun applyStoryChoice(choice: String) {
		storyChoices.add(choice)
		when (choice) {
			// 第一章 · CSP-S
			"只要你们还愿意来，我就会。" -> students.first { it.id == "LJD" }.favor++
			"我不能保证结果，但我会认真准备每一堂课。" -> students.forEach { it.ability += 1 }
			// 第二章 · NOIP
			"先把今天过完，明天再面对下一步。" -> students.first { it.id == "LZN" }.favor++
			"结果会改变计划，但不会决定你们是谁。" -> students.forEach { it.favor += 1 }
			// 间章 · 初一结束
			"这一年你们都长大了，暑假请好好休息。" -> students.forEach { it.favor += 1 }
			"别急着变强，先记住今天为什么想继续。" -> students.forEach { it.stability += 1 }
			// 间章 · NOIP 后
			"照常讲新题，把节奏稳住。" -> students.forEach { it.technology += 2 }
			"先补最短板，谁弱补谁。" -> students.minByOrNull { it.ability }?.let { it.ability += 4 }
			// 第三章前 · 省队前夜
			"无论谁进，你们都不欠彼此一个道歉。" -> students.forEach { it.favor += 1 }
			"把能控制的做到极致，剩下的交给那天。" -> students.forEach { it.ability += 2 }
			// 终章前 · NOI 前夜
			"就当作一场普通的周日模拟赛。" -> students.forEach { it.ability += 2 }
			"记住这盏灯，然后走进考场。" -> students.forEach { it.favor += 1 }
			// 二周目 · NOIP 面对李佳迪追问的三种回答：越是“凭记忆”强答，越是把眼前的人
			// 当成记忆里的影子，好感/稳定性反受损；诚实或沉默则不额外扣分。
			"（凭记忆抢答）你是李佳迪，永远的第一。" -> {
				// 用记忆里的标签盖住眼前真实的她：她被当成影子，好感与稳定性双双下滑。
				students.forEach {
					it.favor = (it.favor - 1).coerceAtLeast(0)
					it.stability = (it.stability - 1).coerceAtLeast(0)
				}
			}
			"我只是觉得你，很像一个人。" -> {
				// 半吐真话，承认自己在“对照”。没有涨也没有明显扣——一次诚实的僵持。
			}
			"（沉默，不回答。）" -> {
				// 沉默让李佳迪自己读懂了“你看的不是我”。士气微降，是一记无声的钝痛。
				students.forEach { it.morale = (it.morale - 1).coerceAtLeast(0) }
			}
		}
	}

	private fun checkMilestone() {
		// 二周目：王怡钧带着记忆重来，玩家已玩腻经营，时间线大幅压缩——
		// 四个里程碑更快到来，把体验重心从“经营”推向“记忆崩坏”的故事本身。
		val checkpoint = (if (newGamePlus) when {
			week >= 60 -> "NOI"
			week >= 45 -> "省队"
			week >= 30 -> "NOIP"
			week >= 15 -> "CSP-S"
			else -> null
		} else when {
			week >= 200 -> "NOI"
			week >= 160 -> "省队"
			week >= 120 -> "NOIP"
			week >= 80 -> "CSP-S"
			else -> null
		}) ?: return
		if (checkpoint in completedMilestones) return
		val threshold = when (checkpoint) {
			"CSP-S" -> 60
			"NOIP" -> 78
			"省队" -> 96
			else -> 120
		}
		lastCompetition = runCompetition(checkpoint)
		// 二周目 · NOI（NOI Au）阶段：无论玩家怎么经营，这一步都必然失利。
		// 这是“记忆崩坏”叙事的落点——她带着记忆重来，却连一枚金牌都留不住。
		// 但仍然播放二周目专属的 NOI 崩解场景（「我记不清她们了」），而不是通用失败场景。
		if (newGamePlus && checkpoint == "NOI") {
			outcome = GameOutcome.BAD_ENDING
			currentMilestone = checkpoint
			completedMilestones.add(checkpoint)
			endingText = "四个人都走到了 NOI，却没有一个人拿到金牌。你带着上一世的记忆重来，终究还是没能改写这个结局。"
			pendingMajorScene = "NOI"
			return
		}
		// 二周目 · NOI 之前的里程碑（CSP-S/NOIP/省队）一律放行：结局注定在 NOI 崩塌，
		// 前面的每一步都只是通往那一刻的路，不因经营好坏而提前中断“记忆崩坏”的故事。
		if (newGamePlus) {
			currentMilestone = checkpoint
			completedMilestones.add(checkpoint)
			pendingMajorScene = checkpoint
			return
		}
		if (students.any { it.ability < threshold || it.technology < threshold }) {
			outcome = GameOutcome.BAD_ENDING
			val failedStudents = students
				.filter { it.ability < threshold || it.technology < threshold }
				.joinToString("、") { it.name }
			endingText = "${failedStudents}没能通过${checkpoint}阶段。训练班在遗憾中结束了。"
			pendingMajorScene = "失败-$checkpoint"
			return
		}
		currentMilestone = checkpoint
		completedMilestones.add(checkpoint)
		pendingMajorScene = checkpoint
		if (checkpoint == "NOI") {
			outcome = when {
				students.all { it.favor >= 12 } -> GameOutcome.FAMILY_ENDING
				students.any { it.favor >= 12 } -> GameOutcome.ROMANCE_PENDING
				else -> GameOutcome.NORMAL_ENDING
			}
			endingText = when (outcome) {
				GameOutcome.FAMILY_ENDING -> "四个人都走到了 NOI。那间合肥教室，成了你们共同的起点。"
				GameOutcome.NORMAL_ENDING -> "四个人都走到了 NOI。你完成了让她们成为 NOI Au 的目标。"
				else -> ""
			}
		}
	}

	private fun runCompetition(milestone: String): MutableList<CompetitionResult> {
		val scored = students.map { student ->
			val base = (student.ability * 0.6 + student.technology * 0.4).toInt()
			val performanceBonus = when (student.id) {
				"LZN" -> if (week % 4 == 0) 8 else -8
				"WYM" -> if (week % 5 == 0) -4 else 2
				"ZHC" -> 1
				"LJD" -> 4
				else -> 0
			}
			// 士气偏离中位数会小幅拉高或压低临场发挥（-5 ~ +5）。
			val moraleBonus = student.morale - 5
			student to (base + performanceBonus + moraleBonus).coerceIn(0, 100)
		}.sortedByDescending { it.second }
		return scored.mapIndexed { index, (student, score) ->
			CompetitionResult(
				milestone,
				student.id,
				student.name,
				score,
				index + 1,
				performanceDescription(student, score)
			)
		}.toMutableList()
	}

	/** 每周的士气自然衰减：越接近里程碑压力越大，稳定性高的人衰减更慢。 */
	private fun moraleDrift(student: StudentState): Int {
		val pressure = if (week % 40 >= 34) 1 else 0
		val resilient = if (student.stability >= 4) 0 else 1
		return pressure + resilient
	}

	private fun performanceDescription(student: StudentState, score: Int): String = when (student.id) {
		"LZN" -> if (score >= 60) "这次发挥稳定，最后阶段没有慌乱。" else "前半程很快，后半程因为紧张丢掉了几分。"
		"WYM" -> if (score >= 60) "节奏偏慢，但把能拿的分数都稳稳拿到了。" else "开局进入状态较慢，后面一直在追赶时间。"
		"ZHC" -> "几乎没有低级失误，按照自己的节奏完成了整场。"
		"LJD" -> if (score >= 75) "解题速度很快，但在一道难题上停留过久。" else "遇到意外题型后调整得不够及时。"
		else -> "完成了自己的比赛。"
	}

	private fun triggerWeeklyEvent() {
		val event = when (week) {
			2 -> Triple("class-2", "王怡钧", "第一次正式上课，你没有急着讲新算法，而是让四个人各自写下最害怕的题型。李佳迪写的是‘没有’，刘子诺写了整整一页。")
			3 -> Triple("class-3", "刘子诺", "刘子诺抢着回答了前三道题，第四道却把数组下标写成了负数。她愣了两秒：这次不算，我刚才发挥失常！")
			4 -> Triple("wym-4", "吴一鸣", "周日的午后，吴一鸣趴在桌边，红色的眼睛已经快要合上了。她小声问：老师，今天讲完之后，可以陪我去看看年糕吗？")
			5 -> Triple("class-5", "朱皓辰", "你发现朱皓辰总是在最后十分钟整理笔记。她把四个人的错误按原因分成了四类，并默默在黑板角落写下‘先保证正确，再追求速度’。")
			6 -> Triple("rank-6", "全员", "第一次小测排名出来了。李佳迪第一，朱皓辰紧随其后，吴一鸣稳定在中间，刘子诺从第一冲到了最后。她盯着成绩单，难得安静了一会儿。")
			7 -> Triple("plan-7", "王怡钧", "你重新排了训练计划：上午补基础，下午做题，晚上只允许复盘错题。刘子诺提出抗议，吴一鸣打了个哈欠，朱皓辰点头，李佳迪问你能不能再加一道题。")
			8 -> Triple("lzn-8", "刘子诺", "刘子诺把一张写满涂改痕迹的排名表递过来：我这次又掉下去了！不过没关系，下周我一定能超过李佳迪！")
			9 -> Triple("autumn-9", "全员", "合肥下起了第一场秋雨。教室窗玻璃蒙着一层水汽，四个人却没有人想提前离开。你把一道图论题写在黑板上，雨声里开始不断响起键盘声。")
			10 -> Triple("wym-10", "吴一鸣", "吴一鸣今天带来了年糕的照片。她说猫昨晚踩到了键盘，提交了一份全是乱码的代码，然后把照片递给你：年糕也想学编程。")
			11 -> Triple("lzn-11", "刘子诺", "刘子诺第一次主动留下来复盘。她把每次‘发挥失常’的原因写成清单：读题太快、没检查、看到李佳迪就紧张。写完后，她抬头说：下次我先和自己比。")
			12 -> Triple("zhc-12", "朱皓辰", "朱皓辰安静地把错题本推到你面前。她没有解释，只在每一道题旁边写好了自己重新整理的思路。")
			13 -> Triple("winter-13", "李佳迪", "天气转冷，李佳迪把教室空调的温度调高了一度。她说不是自己怕冷，只是‘低温会影响思考效率’，然后把一杯热水推到了吴一鸣手边。")
			14 -> Triple("contest-14", "全员", "模拟赛开始前，你提醒大家不要互相比较。比赛结束，刘子诺兴奋地说自己终于没有因为一道难题卡住，朱皓辰则发现了整套题里唯一的坑。")
			15 -> Triple("review-15", "王怡钧", "你把四人的代码放在一起对照。李佳迪的代码锋利，朱皓辰的代码严谨，刘子诺的代码充满奇思妙想，吴一鸣的代码总有几行像是困着写出来的。")
			16 -> Triple("ljd-16", "李佳迪", "李佳迪在教室门口等你。她语气依旧冷淡：你的课程安排得不错。下周，给我更难的问题。")
			17 -> Triple("holiday-17", "全员", "春节前最后一节课，你没有安排比赛，而是让大家写一封给一年后自己的信。刘子诺写得最快，李佳迪写得最少，吴一鸣写到一半睡着了，朱皓辰替她盖好了本子。")
			18 -> Triple("spring-18", "全员", "新学期开始，四个人的目标都变得具体起来：有人想提高排名，有人想补齐数据结构，有人想证明自己，也有人只说了一句‘我会继续来’。")
			19 -> Triple("pressure-19", "刘子诺", "刘子诺在模拟赛中再次失误，放学后却没有像往常一样开玩笑。你陪她把比赛过程重放了一遍，她最后说：我不是不行，我只是还没学会在紧张的时候相信自己。")
			20 -> Triple("team-20", "全员", "你让四个人互相讲题。刘子诺负责把复杂的东西讲得热闹，朱皓辰负责找漏洞，吴一鸣负责提出最奇怪的问题，李佳迪最后给出了一种更快的做法。")
			21 -> Triple("spring-21", "王怡钧", "你开始把每个人的错题分成‘不会’、‘会但写错’和‘想不到’三类。训练不再只是刷题，而是要知道自己为什么会输。")
			22 -> Triple("wym-22", "吴一鸣", "吴一鸣在讲义边缘画了一只趴着的猫。你以为她没有听课，她却准确指出了例题里一个容易忽略的边界条件。")
			23 -> Triple("lzn-23", "刘子诺", "刘子诺给自己定了一个新规矩：每次提交前必须读三遍题面。她说这听起来很简单，可是对她来说比学新算法还难。")
			24 -> Triple("zhc-24", "朱皓辰", "朱皓辰发现四个人都在重复同一种错误，于是做了一张公共错题表。她没有署名，只把表格放在了讲台上。")
			25 -> Triple("ljd-25", "李佳迪", "李佳迪在白板上写出一种你没有教过的优化。她看起来只是想证明自己做得到，却在你指出漏洞后认真记下了修改方法。")
			26 -> Triple("mock-26", "全员", "第一次四小时模拟赛结束。刘子诺提前交卷后发现漏读了一个条件，吴一鸣最后半小时才进入状态，朱皓辰稳定拿分，李佳迪只错了一道细节题。")
			27 -> Triple("talk-27", "全员", "你没有公布排名，而是让她们写下比赛中最满意的一件事。刘子诺写‘我检查了两遍’，吴一鸣写‘我没有睡着’，朱皓辰和李佳迪都写得很短。")
			28 -> Triple("class-28", "王怡钧", "你第一次讲到图论。刘子诺把树说成了‘不会长叶子的图’，吴一鸣问树如果倒过来算不算，朱皓辰已经开始画图，李佳迪要求再来一道。")
			29 -> Triple("wym-29", "吴一鸣", "吴一鸣今天比所有人都早到。她说年糕把她叫醒了。她趴在桌上补完了昨天的题，虽然中途还是睡着了几分钟。")
			30 -> Triple("lzn-30", "刘子诺", "刘子诺在黑板上给大家讲自己的思路，讲到一半突然卡住。朱皓辰没有替她说下去，只问：你刚才的第二步依据是什么？")
			31 -> Triple("zhc-31", "朱皓辰", "朱皓辰第一次主动提出想做一场限时训练。她说只有在时间压力下，才能知道自己的稳定是不是真的稳定。")
			32 -> Triple("ljd-32", "李佳迪", "李佳迪的训练量已经超过了计划。你让她停下来休息，她沉默片刻后问：如果我不继续做，别人会不会超过我？")
			33 -> Triple("rest-33", "王怡钧", "你把这周的最后一小时改成了散步。四个人沿着合肥街边慢慢走，刘子诺说了很多话，吴一鸣买了猫咪形状的饼干。")
			34 -> Triple("contest-34", "全员", "小型校际交流赛开始。四个人第一次和陌生选手坐在同一排，刘子诺紧张得话变少了，朱皓辰把水杯放到了她手边。")
			35 -> Triple("contest-35", "刘子诺", "交流赛成绩公布，刘子诺排在中间。她先叹气，随后又笑起来：至少这次我没有垫底，也没有靠运气冲到前面。")
			36 -> Triple("summer-36", "全员", "初一的最后一节常规课结束。你让她们整理这一年的变化，四个人从最初的 CSP-S 1= 水平出发，已经开始真正理解什么叫长期训练。")
			37 -> Triple("summer-37", "王怡钧", "暑假计划贴上墙：每天不超过六小时，必须安排运动和休息。李佳迪看了很久，最后没有提出异议。")
			38 -> Triple("summer-38", "吴一鸣", "暑假集训的第一天，吴一鸣抱着年糕的玩偶来到教室。她说这是为了防止自己想家，结果午休时抱着玩偶睡得比平时更熟。")
			39 -> Triple("summer-39", "刘子诺", "刘子诺把一道难题改编成了三个版本，拉着大家一起做。她的题目不一定严谨，但气氛第一次像真正的队伍。")
			40 -> Triple("summer-40", "朱皓辰", "朱皓辰把暑假错题整理成册。她在扉页写下：错误不是失败的证据，是下一次训练的入口。")
			41 -> Triple("summer-41", "李佳迪", "李佳迪第一次在训练中主动请教朱皓辰。朱皓辰想了很久，用一页纸解释了自己的证明。两个人都没有说谢谢。")
			42 -> Triple("summer-42", "全员", "集训中途，你安排了一次无排名比赛。没有奖品，没有公布分数，但四个人都比平时更认真，因为她们开始在意彼此是否能一起解出来。")
			43 -> Triple("summer-43", "全员", "午后的雷雨让教室停电。刘子诺用手机打着手电，朱皓辰继续口述思路，李佳迪闭着眼在脑中推演，吴一鸣问年糕会不会怕打雷。")
			44 -> Triple("summer-44", "王怡钧", "你意识到自己总想把所有事情安排好，却忽略了她们已经有了自己的节奏。新的计划里，你留下了可以自由选择的时间。")
			45 -> Triple("summer-45", "全员", "暑假最后一次模拟赛，四个人都没有拿到满分，却第一次在赛后互相指出优点。你忽然觉得，这个班已经不只是为了成绩而存在。")
			46 -> Triple("school-46", "全员", "初二开学。新的校服、新的课表和更多作业让训练时间变少了。你们决定把每一次周日都用得更认真。")
			47 -> Triple("wym-47", "吴一鸣", "吴一鸣最近总是迟到。她解释说初二的作业变多了。你没有责备她，而是帮她把训练拆成更短的片段，她终于能在困之前完成第一题。")
			48 -> Triple("lzn-48", "刘子诺", "刘子诺在新班级里交到了朋友，却担心别人觉得她只会说不会做。她决定把一道自己最擅长的题讲给同学听。")
			49 -> Triple("zhc-49", "朱皓辰", "朱皓辰的班主任夸她做事可靠。她听完只是点头，课后却把这句话写进了错题本的空白页。")
			50 -> Triple("ljd-50", "李佳迪", "李佳迪在新学期第一次小测中失误。她把成绩单折好，冷静地说：原来我也会因为紧张而犯错。你告诉她，这不是一件需要隐藏的事。")
			51 -> Triple("school-51", "全员", "四个人开始讨论未来想去的学校。答案各不相同，但她们都希望还能一起参加比赛。你提醒她们，目标可以很远，今天的题仍然要一题一题做。")
			52 -> Triple("school-52", "全员", "你们制定了第一次真正的月度目标：不是追求名次，而是四个人都完成自己的薄弱专题。训练表上第一次出现了四种不同的路线。")
			53 -> Triple("pressure-53", "吴一鸣", "吴一鸣因为连续几周状态不好而自责。她说自己是不是根本不适合竞赛。你让她看回最初的错题本：她已经学会了很多，只是成长从来不是直线上升。")
			54 -> Triple("pressure-54", "刘子诺", "刘子诺在一次模拟赛中冲到第一，却没有庆祝太久。她说自己终于明白了，真正想赢的不是李佳迪，而是那个每次失误后都想放弃的自己。")
			55 -> Triple("pressure-55", "朱皓辰", "朱皓辰遇到一道完全没有思路的题。她没有发呆，也没有急着看题解，而是把问题拆成了几个可以确认的小问题。")
			56 -> Triple("pressure-56", "李佳迪", "李佳迪把一份写满批注的讲义交给你。她说其中有些地方比你的原讲义更清楚。你接受了这份修改，她的表情第一次显得有些放松。")
			57 -> Triple("autumn-57", "全员", "秋天再次来到合肥。去年你们还在争论什么是树，今年四个人已经开始讨论不同算法的复杂度和适用边界。时间确实留下了痕迹。")
			58 -> Triple("mock-58", "全员", "CSP-S 前最后一次模拟赛，刘子诺发挥稳定，吴一鸣顺利完成了整场，朱皓辰没有丢掉低级错误，李佳迪主动检查了三遍代码。")
			59 -> Triple("before-csp-59", "王怡钧", "比赛前夜，你没有再发新的题。你只发了一句话：明天认真读题，按自己的节奏来。四个人都回复了收到。")
			60 -> Triple("before-csp-60", "全员", "CSP-S 的考场外，四个人穿着各自习惯的衣服。刘子诺深呼吸，朱皓辰检查文具，李佳迪闭目思考，吴一鸣轻轻摸了摸口袋里的年糕照片。")
			61 -> Triple("csp-61", "全员", "模拟考成绩比预想中低。刘子诺觉得是题目太怪，李佳迪觉得是准备不足，朱皓辰开始分类错题，吴一鸣问能不能先吃点东西。你决定不公布总分。")
			62 -> Triple("csp-62", "王怡钧", "你把训练分成四条路线：基础正确率、算法覆盖、代码速度和心态。四个人第一次拿到了完全不同的周计划。")
			63 -> Triple("csp-63", "李佳迪", "李佳迪完成了所有基础题，却在一道看似简单的题上反复出错。她终于承认，自己太想证明‘这种题不该错’。")
			64 -> Triple("csp-64", "吴一鸣", "吴一鸣带着困意完成了一道很难的题。她自己都不敢相信，直到你把通过记录放到她面前，她才慢慢笑起来。")
			65 -> Triple("csp-65", "刘子诺", "刘子诺在训练中连续三次发挥失常，第四次却突然拿到了全场最高分。她没有再说运气，而是把第四次的流程逐项记了下来。")
			66 -> Triple("csp-66", "朱皓辰", "朱皓辰建议把模拟赛拆成两次半场，先训练读题，再训练收尾。她说稳定不是永远不出错，而是出错后知道怎么回来。")
			67 -> Triple("csp-67", "全员", "四个人开始互相检查代码。李佳迪负责复杂度，朱皓辰负责边界，刘子诺负责寻找反例，吴一鸣负责问一句‘这里为什么这样写’。")
			68 -> Triple("csp-68", "全员", "初秋的风从窗缝里吹进来。你们做完一套综合题，没人讨论谁第一，反而认真争论一道题到底该不该使用更复杂的算法。")
			69 -> Triple("csp-69", "王怡钧", "你发现自己开始害怕她们考不好。可你也清楚，老师不能替学生走进考场，只能把能准备的事情准备好。")
			70 -> Triple("csp-70", "吴一鸣", "吴一鸣主动要求把今天的课提前结束。她说自己最近总是很累。你同意了，她离开前回头说：下周我会来的。")
			71 -> Triple("csp-71", "刘子诺", "刘子诺给每个人写了一张小纸条。给李佳迪的是‘别太快’，给朱皓辰的是‘偶尔相信直觉’，给吴一鸣的是‘记得醒着’，给你的是‘老师也别紧张’。")
			72 -> Triple("csp-72", "朱皓辰", "朱皓辰的错题本已经换成了第二本。她把第一本收进书包最里面，说以后想看看自己是怎么一步步变好的。")
			73 -> Triple("csp-73", "李佳迪", "李佳迪问你，如果考试没有达到目标，是否还会继续教她们。你回答，成绩会决定下一阶段的计划，但不会决定你是否继续相信她们。")
			74 -> Triple("csp-74", "全员", "最后一次高强度模拟赛结束。四个人都很疲惫，却没有人提前离开。你们把每一道错题讲给彼此听，直到教室外的天完全黑下来。")
			75 -> Triple("csp-75", "全员", "距离 CSP-S 还有一个月。你把新题全部收起来，开始做旧题和错题。刘子诺觉得无聊，李佳迪觉得合理，朱皓辰已经列好了复习顺序，吴一鸣睡得更香了。")
			76 -> Triple("csp-76", "王怡钧", "你带她们去教室附近走了一圈，没有讲题。合肥的街灯一盏盏亮起，你提醒大家，竞赛很重要，但它不应该吞掉所有生活。")
			77 -> Triple("csp-77", "全员", "四个人各自选了一道最喜欢的题。刘子诺选了一道有趣的构造题，朱皓辰选了证明最完整的题，李佳迪选了最难的题，吴一鸣选了题面里提到猫的题。")
			78 -> Triple("csp-78", "全员", "考前一周，你们只做短题和熟悉的模板。李佳迪没有要求加题，刘子诺没有抱怨，朱皓辰把文具准备好，吴一鸣把年糕照片放进了笔袋。")
			79 -> Triple("csp-79", "王怡钧", "出发前，你最后检查了一遍训练记录。四个人已经不是刚来时的四个初一学生了，她们仍然会犯错，却已经学会在错误之后继续向前。")
			80 -> Triple("milestone-csp", "全员", "CSP-S 成绩公布。四个人都通过了 1=。刘子诺兴奋地举起成绩单，李佳迪只是轻轻点头，而吴一鸣已经困得靠在了桌边。")
			120 -> Triple("milestone-noip", "全员", "NOIP 结束后，四个人第一次认真讨论起了省队。你把新的训练计划写满整块白板，窗外的合肥已经亮起了冬天的灯。")
			160 -> Triple("milestone-team", "全员", "省队名单公布。朱皓辰沉默地收好通知，刘子诺大声欢呼，李佳迪看向你：接下来，是更大的舞台。")
			else -> null
		} ?: return
		if (eventId == null && pendingMajorScene == null && event.first !in seenEvents) {
			eventId = event.first
			eventSpeaker = event.second
			eventText = event.third
		}
	}

	/**
	 * 随机突发事件：并非每周都触发，且每个事件只出现一次。
	 * 用 [week] 做确定性选择，便于测试。序章未过不打扰玩家；
	 * 到达触发节奏时先选中事件放入队列，一旦弹窗空闲即展示，
	 * 避免被密集的脚本周常事件长期挤占而永远无法出现。
	 */
	private fun triggerRandomEvent() {
		if (!finishedPrologue) return
		// 大约每 6 周选一次，避开脚本周常密集的开局。
		if (pendingRandomEventId == null && week >= 10 && week % 6 == 0) {
			val available = RANDOM_EVENTS.filter { it.id !in seenRandomEvents }
			if (available.isNotEmpty()) {
				pendingRandomEventId = available[(week / 6) % available.size].id
			}
		}
		// 弹窗与重大场景都空闲时，才把排队中的随机事件呈现出来。
		if (eventId != null || pendingMajorScene != null) return
		val queued = pendingRandomEventId ?: return
		val picked = RANDOM_EVENTS.firstOrNull { it.id == queued } ?: run {
			pendingRandomEventId = null
			return
		}
		picked.effect(this)
		seenRandomEvents.add(picked.id)
		pendingRandomEventId = null
		eventId = picked.id
		eventSpeaker = picked.speaker
		eventText = picked.text
	}

	private fun weeklyPerformance(student: StudentState): Int = when (student.id) {
		"WYM" -> if (week % 5 == 0) -1 else 0
		"LZN" -> when (week % 4) {
			0 -> 3
			1 -> -3
			else -> 0
		}
		"ZHC" -> 0
		"LJD" -> if (week % 6 == 0) 1 else 0
		else -> 0
	}

	/** 好感达标后立即进入剧情（玩家点击"进入好感剧情"按钮时调用）。 */
	fun acceptFavorScene() {
		val scene = pendingFavorScene ?: return
		pendingFavorScene = null
		if (pendingMajorScene == null) {
			pendingMajorScene = scene
		} else {
			deferredMajorScene = scene
		}
	}

	private fun triggerFavorEvent(student: StudentState) {
		// 好感达到 4/8/10/12 时各触发一段结构化关系场景。
		// 不再直接设置 pendingMajorScene，而是暂存到 pendingFavorScene，
		// 等玩家主动选择"进入好感剧情"后才真正播放。
		val milestone = when (student.favor) {
			4, 8, 10, 12 -> "关系-${student.id}-${student.favor}"
			else -> null
		} ?: return
		if (pendingFavorScene == null) {
			pendingFavorScene = milestone
		} else {
			// 已有待确认的好感剧情，暂存到 deferredMajorScene。
			deferredMajorScene = milestone
		}
	}

	/**
	 * 扫描全部成就，解锁所有已满足条件、但此前尚未记录的成就。
	 * @return 本次新解锁的成就列表，供界面依次弹出提示。已解锁的不会重复返回。
	 */
	fun checkAchievements(): List<Achievement> {
		val newlyUnlocked = ACHIEVEMENTS.filter { it.id !in unlockedAchievements && it.condition(this) }
		newlyUnlocked.forEach { unlockedAchievements.add(it.id) }
		return newlyUnlocked
	}

	companion object {
		/**
		 * 构造一个二周目（New Game+）新档：从一周目全新开局，但
		 * 置 [newGamePlus] = true，并继承已解锁的成就（“记忆”的一部分）。
		 * @param carryOverAchievements 上一世解锁过的成就 id，跨局保留。
		 */
		fun newGamePlus(slot: Int, carryOverAchievements: Set<String> = emptySet()): SaveFormat =
			SaveFormat().apply {
				this.slot = slot
				this.newGamePlus = true
				this.unlockedAchievements = carryOverAchievements.toMutableSet()
			}
	}
}

/** 一个随机突发事件：立即结算的属性影响 + 弹窗文本。不参与序列化，仅作为静态事件表。 */
private class RandomEvent(
	val id: String,
	val speaker: String,
	val text: String,
	val effect: (SaveFormat) -> Unit
)

/** 随机突发事件的静态表；每个事件在一局游戏里最多触发一次。 */
private val RANDOM_EVENTS: List<RandomEvent> = listOf(
	RandomEvent("rnd-flu", "全员", "流感季来了。四个人陆陆续续请了假，训练进度被打乱。你把这周的难题换成了轻松的复习。") { s ->
		s.students.forEach { it.morale = (it.morale - 1).coerceAtLeast(0) }
	},
	RandomEvent("rnd-online-judge", "刘子诺", "某个在线评测站临时开放了一套高质量套题。刘子诺熬夜刷完，第二天顶着黑眼圈来上课，却难得地一题没错。") { s ->
		s.students.forEach { it.technology += 2 }
		s.students.firstOrNull { it.id == "LZN" }?.let { it.morale = (it.morale + 1).coerceAtMost(10) }
	},
	RandomEvent("rnd-cat-sick", "吴一鸣", "年糕生病住进了宠物医院。吴一鸣整节课都心不在焉，你让她提前回了家。") { s ->
		s.students.firstOrNull { it.id == "WYM" }?.let { it.morale = (it.morale - 2).coerceAtLeast(0) }
	},
	RandomEvent("rnd-alumni", "王怡钧", "一位往届的 OIer 回来做了一次分享。她讲的不是算法，而是如何在长期训练里不把自己耗空。四个人听得比平时任何一节课都认真。") { s ->
		s.students.forEach {
			it.morale = (it.morale + 2).coerceAtMost(10)
			it.stability += 1
		}
	},
	RandomEvent("rnd-exam-week", "全员", "学校月考撞上了周日。四个人只能挤出半天训练，却都没有缺席。") { s ->
		s.students.forEach { it.technology += 1 }
	},
	RandomEvent("rnd-zhc-shine", "朱皓辰", "朱皓辰在一道公认很难的构造题上给出了全场唯一的正解。她照例没有声张，李佳迪却主动过去和她讨论了很久。") { s ->
		s.students.firstOrNull { it.id == "ZHC" }?.let {
			it.morale = (it.morale + 2).coerceAtMost(10)
			it.ability += 2
		}
	},
	RandomEvent("rnd-ljd-slump", "李佳迪", "李佳迪连续两周状态低迷。她没有解释，只是把训练量又加了一倍。你把她的计划强行改回正常节奏。") { s ->
		s.students.firstOrNull { it.id == "LJD" }?.let {
			it.morale = (it.morale + 1).coerceAtMost(10)
			it.stability += 1
		}
	},
	RandomEvent("rnd-power-cut", "全员", "训练中途整栋楼停电。四个人点着手机照明，围在一起口算完了最后一道题，反而笑作一团。") { s ->
		s.students.forEach { it.morale = (it.morale + 1).coerceAtMost(10) }
	},
	RandomEvent("rnd-new-laptop", "李佳迪", "李佳迪换了一台新笔记本，编译速度快了不少。她没有炫耀，只是默默把旧机器擦干净，说要留给下一届。") { s ->
		s.students.firstOrNull { it.id == "LJD" }?.let { it.technology += 3 }
	},
	RandomEvent("rnd-lzn-viral", "刘子诺", "刘子诺把自己的一道构造题发到了论坛，意外被很多人转发讨论。她第一次发现，原来自己也能给别人带来启发。") { s ->
		s.students.firstOrNull { it.id == "LZN" }?.let {
			it.morale = (it.morale + 2).coerceAtMost(10)
			it.favor += 1
		}
	},
	RandomEvent("rnd-wym-dream", "吴一鸣", "吴一鸣说她昨晚梦见自己在考场上一直睡不醒。你陪她把梦里最怕的那类题重新做了一遍，她笑着说：原来醒着也做得出来。") { s ->
		s.students.firstOrNull { it.id == "WYM" }?.let {
			it.morale = (it.morale + 1).coerceAtMost(10)
			it.ability += 2
		}
	},
	RandomEvent("rnd-snow", "全员", "合肥今年下了第一场雪。四个人挤在窗边看了很久，谁也没提做题的事。你破例把这节课提前结束，让她们去楼下堆了个歪歪扭扭的雪人。") { s ->
		s.students.forEach {
			it.morale = (it.morale + 2).coerceAtMost(10)
			it.favor += 1
		}
	},
	RandomEvent("rnd-old-problem", "王怡钧", "你翻出自己当年参赛时的一道原题，讲给她们听。四个人第一次意识到，讲台上的老师也曾是坐在下面、会紧张会出错的选手。") { s ->
		s.students.forEach {
			it.technology += 1
			it.morale = (it.morale + 1).coerceAtMost(10)
		}
	},
	RandomEvent("rnd-zhc-teach", "朱皓辰", "朱皓辰第一次主动上台，把一道图论题从头讲到尾。她讲得很慢，却没有一步含糊。讲完，教室里安静了两秒，随后是刘子诺带头的掌声。") { s ->
		s.students.firstOrNull { it.id == "ZHC" }?.let {
			it.morale = (it.morale + 2).coerceAtMost(10)
			it.favor += 1
		}
		s.students.forEach { it.technology += 1 }
	}
)

/**
 * 一条成就：满足 [condition] 时解锁。
 * 成就只表彰玩家已经达成的进度，不修改任何游戏状态，因此不参与序列化，
 * 存档里只记录已解锁的 [id]（见 [SaveFormat.unlockedAchievements]）。
 */
class Achievement(
	val id: String,
	val title: String,
	val description: String,
	val condition: (SaveFormat) -> Boolean
)

/** 全部成就的静态表。界面通过它渲染成就墙，[SaveFormat.checkAchievements] 通过它判定解锁。 */
val ACHIEVEMENTS: List<Achievement> = listOf(
	Achievement("ach-first-class", "第一堂课", "结束序章，正式成为周日编程班的老师。") { it.finishedPrologue },
	Achievement("ach-csp", "跨过 CSP-S", "四个人全部通过 CSP-S 1= 阶段。") { "CSP-S" in it.completedMilestones },
	Achievement("ach-noip", "站稳 NOIP", "四个人全部通过 NOIP 阶段。") { "NOIP" in it.completedMilestones },
	Achievement("ach-team", "走进省队", "四个人全部进入省队。") { "省队" in it.completedMilestones },
	Achievement("ach-noi", "走向更远的地方", "带四个人一起走到 NOI。") { "NOI" in it.completedMilestones },
	Achievement("ach-mock-master", "模拟赛常客", "在训练中组织过至少一场四小时模拟赛。") { it.lastMockContest.isNotEmpty() },
	Achievement("ach-perfect-morale", "满格心态", "让至少一位学生的士气达到 10。") { s -> s.students.any { it.morale >= 10 } },
	Achievement("ach-heart-to-heart", "亦师亦友", "让至少一位学生的好感达到“交心”（10）。") { s -> s.students.any { it.favor >= 10 } },
	Achievement("ach-confession", "心意相通", "让至少一位学生的好感达到满值（12）。") { s -> s.students.any { it.favor >= 12 } },
	Achievement("ach-all-trusted", "被四个人信任", "让四位学生的好感都达到“信任”（4）以上。") { s -> s.students.all { it.favor >= 4 } },
	Achievement("ach-strong-team", "全员上强度", "让四位学生的水平都达到 100。") { s -> s.students.all { it.ability >= 100 } },
	Achievement("ach-story-teller", "陪她们做过很多选择", "在剧情里累计做出至少 5 次关键选择。") { it.storyChoices.size >= 5 },
	Achievement("ach-eventful", "什么都遇到过", "触发过全部随机突发事件。") { s -> RANDOM_EVENTS.all { it.id in s.seenRandomEvents } },
	Achievement("ach-walk", "散步的意义", "带学生们散过一次步，把周日还给了生活。") { it.walkCount >= 1 },
	Achievement("ach-reviewer", "复盘成习惯", "累计组织过至少 5 次集体复盘。") { it.reviewCount >= 5 },
	Achievement("ach-steady-team", "四个人都很稳", "让四位学生的稳定性都达到 8 以上。") { s -> s.students.all { it.stability >= 8 } },
	Achievement("ach-all-morale", "整支队伍都昂扬", "让四位学生的士气都达到 8 以上。") { s -> s.students.all { it.morale >= 8 } }
)