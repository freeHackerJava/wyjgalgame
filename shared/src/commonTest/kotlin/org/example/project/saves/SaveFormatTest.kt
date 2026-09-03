package org.example.project.saves

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SaveFormatTest {
	@Test
	fun coachingConsumesAnActionAndRaisesFavor() {
		val save = SaveFormat()
		val student = save.students.first()

		assertTrue(save.spend("coach", student.id))
		assertEquals(2, save.actionPoints)
		assertEquals(1, student.favor)
		assertEquals(47, student.ability)
	}

	@Test
	fun ordinaryWeeksNowSurfaceTheirWeeklyEvent() {
		val save = SaveFormat()
		// 第 1 周结束后进入第 2 周，第 2 周有一段周常剧情。
		save.endWeek()

		assertEquals("class-2", save.eventId)
		assertEquals("王怡钧", save.eventSpeaker)
		assertTrue(save.eventText.isNotEmpty())
	}

	@Test
	fun aWeeklyEventOnlyAppearsOnce() {
		val save = SaveFormat()
		save.endWeek()
		assertEquals("class-2", save.eventId)
		save.dismissEvent()
		// 再次经过同一周不会重复触发（已记入 seenEvents）。
		assertTrue("class-2" in save.seenEvents)
	}

	@Test
	fun failingMilestoneStopsTheGame() {
		val save = SaveFormat()
		while (save.week < 80) {
			save.endWeek()
			if (save.eventId != null) save.dismissEvent()
		}

		assertEquals(GameOutcome.BAD_ENDING, save.outcome)
		assertTrue(save.endingText.contains("CSP-S"))
		assertEquals("失败-CSP-S", save.pendingMajorScene)
	}

	@Test
	fun successfulMilestoneCreatesItsOwnEvent() {
		val save = SaveFormat()
		while (save.week < 79) {
			save.endWeek()
			if (save.eventId != null) save.dismissEvent()
		}
		save.students.forEach {
			it.ability = 100
			it.technology = 100
		}
		save.endWeek()

		assertEquals("CSP-S", save.currentMilestone)
		assertEquals("CSP-S", save.pendingMajorScene)
	}

	@Test
	fun invalidStudentDoesNotConsumeAnAction() {
		val save = SaveFormat()

		assertTrue(!save.spend("coach", "missing"))
		assertEquals(3, save.actionPoints)
	}

	@Test
	fun favorMilestoneCreatesARelationshipScene() {
		val save = SaveFormat()
		val student = save.students.first()
		save.actionPoints = 4
		repeat(4) { save.spend("coach", student.id) }

		assertEquals("关系-WYM-4", save.pendingFavorScene)
	}

	@Test
	fun favorStagesReachTheRomanceThreshold() {
		val save = SaveFormat()
		val student = save.students.first()
		save.actionPoints = 12
		repeat(12) {
			save.spend("coach", student.id)
			if (save.eventId != null) save.dismissEvent()
			if (student.favor < 12 && save.pendingFavorScene != null) { save.acceptFavorScene(); save.finishMajorScene() }
		}

		assertEquals(12, student.favor)
		assertEquals("关系-WYM-12", save.pendingFavorScene)
	}

	@Test
	fun favorSceneWaitsForAnExistingEvent() {
		val save = SaveFormat()
		val student = save.students.first()
		save.eventId = "existing-event"
		save.actionPoints = 12
		repeat(12) {
			save.spend("coach", student.id)
			if (student.favor < 12 && save.pendingFavorScene != null) { save.acceptFavorScene(); save.finishMajorScene() }
		}

		// 好感剧情先暂存在 pendingFavorScene，不会自动播放。
		assertEquals(null, save.pendingMajorScene)
		assertEquals("关系-WYM-12", save.pendingFavorScene)
		// 即使弹窗关闭，好感剧情也不会自动升级为 pendingMajorScene。
		save.dismissEvent()
		assertEquals(null, save.pendingMajorScene)
		// 玩家主动确认后才进入。
		save.acceptFavorScene()
		assertEquals("关系-WYM-12", save.pendingMajorScene)
	}

	@Test
	fun majorSceneChoicesChangeStateAndAreSavedInMemory() {
		val save = SaveFormat()
		val originalAbility = save.students.first().ability

		save.applyStoryChoice("我不能保证结果，但我会认真准备每一堂课。")

		assertEquals(originalAbility + 1, save.students.first().ability)
		assertEquals(1, save.storyChoices.size)
		assertEquals("我不能保证结果，但我会认真准备每一堂课。", save.storyChoices.first())
	}

	@Test
	fun interstitialChoiceRaisesTheWeakestStudent() {
		val save = SaveFormat()
		val weakest = save.students.minByOrNull { it.ability }!!
		val before = weakest.ability

		save.applyStoryChoice("先补最短板，谁弱补谁。")

		assertEquals(before + 4, save.students.first { it.id == weakest.id }.ability)
	}

	@Test
	fun interstitialChoiceCanRaiseStability() {
		val save = SaveFormat()
		val before = save.students.map { it.stability }

		save.applyStoryChoice("别急着变强，先记住今天为什么想继续。")

		save.students.forEachIndexed { index, student ->
			assertEquals(before[index] + 1, student.stability)
		}
	}

	@Test
	fun unknownChoiceIsRecordedButChangesNoStats() {
		val save = SaveFormat()
		val snapshot = save.students.map { Triple(it.ability, it.technology, it.favor) }

		save.applyStoryChoice("一句没有任何效果的自定义台词")

		assertEquals(1, save.storyChoices.size)
		save.students.forEachIndexed { index, student ->
			assertEquals(snapshot[index], Triple(student.ability, student.technology, student.favor))
		}
	}

	@Test
	fun cspPreparationLeadsIntoTheMilestoneEvent() {
		val save = SaveFormat()
		while (save.week < 79) save.endWeek()
		save.students.forEach {
			it.ability = 100
			it.technology = 100
		}
		save.endWeek()
		assertEquals("CSP-S", save.pendingMajorScene)
		save.finishMajorScene()
		save.endWeek()
		assertEquals("CSP-S", save.currentMilestone)
		assertEquals(null, save.pendingMajorScene)
	}

	@Test
	fun milestoneStoresRankedCompetitionResults() {
		val save = SaveFormat()
		while (save.week < 79) save.endWeek()
		save.students.forEach {
			it.ability = 100
			it.technology = 100
		}
		save.endWeek()

		assertEquals(4, save.lastCompetition.size)
		assertEquals(listOf(1, 2, 3, 4), save.lastCompetition.map { it.rank })
		assertTrue(save.lastCompetition.any { it.studentName == "刘子诺" && it.performance.contains("发挥") })
	}

	@Test
	fun deferredRelationshipSceneFollowsMajorScene() {
		val save = SaveFormat()
		save.pendingMajorScene = "CSP-S"
		save.deferredMajorScene = "关系-WYM-12"

		save.finishMajorScene()

		assertEquals("关系-WYM-12", save.pendingMajorScene)
		assertEquals(null, save.deferredMajorScene)
	}

	@Test
	fun afterNoipSceneTriggersAtWeek130() {
		val save = SaveFormat()
		save.students.forEach {
			it.ability = 300
			it.technology = 300
		}
		while (save.week < 129) {
			save.endWeek()
			if (save.pendingMajorScene != null) save.finishMajorScene()
			assertEquals(GameOutcome.PLAYING, save.outcome)
		}
		save.endWeek()

		assertEquals("NOIP后", save.pendingMajorScene)
	}

	@Test
	fun beforeTeamSceneTriggersAtWeek150() {
		val save = SaveFormat()
		save.students.forEach {
			it.ability = 300
			it.technology = 300
		}
		while (save.week < 149) {
			save.endWeek()
			if (save.pendingMajorScene != null) save.finishMajorScene()
			assertEquals(GameOutcome.PLAYING, save.outcome)
		}
		save.endWeek()

		assertEquals("省队前夜", save.pendingMajorScene)
	}

	@Test
	fun beforeNoiSceneTriggersAtWeek185() {
		val save = SaveFormat()
		save.students.forEach {
			it.ability = 300
			it.technology = 300
		}
		while (save.week < 184) {
			save.endWeek()
			if (save.pendingMajorScene != null) save.finishMajorScene()
			assertEquals(GameOutcome.PLAYING, save.outcome)
		}
		save.endWeek()

		assertEquals("NOI前夜", save.pendingMajorScene)
	}

	@Test
	fun firstYearSceneTriggersOnceBeforeCsp() {
		val save = SaveFormat()
		while (save.week < 39) save.endWeek()
		save.endWeek()

		assertEquals("初一结束", save.pendingMajorScene)
		save.finishMajorScene()
		save.endWeek()
		assertEquals(null, save.pendingMajorScene)
		assertTrue("初一结束" in save.seenMajorScenes)
	}

	@Test
	fun rememberingASceneNodeSupportsResume() {
		val save = SaveFormat()
		assertEquals(null, save.majorSceneNodeId)

		save.rememberMajorSceneNode("csp-7")
		assertEquals("csp-7", save.majorSceneNodeId)
	}

	@Test
	fun finishingASceneClearsTheResumeNode() {
		val save = SaveFormat()
		save.pendingMajorScene = "CSP-S"
		save.rememberMajorSceneNode("csp-7")

		save.finishMajorScene()

		assertEquals(null, save.majorSceneNodeId)
	}

	@Test
	fun seenPlotNodesTrackReadStatePerScene() {
		val save = SaveFormat()
		assertTrue(!save.isPlotNodeSeen("第一章 · 雨中的名单", "csp-1"))

		save.markPlotNodeSeen("第一章 · 雨中的名单", "csp-1")

		assertTrue(save.isPlotNodeSeen("第一章 · 雨中的名单", "csp-1"))
		// 不同标题下的同一节点 id 互不影响。
		assertTrue(!save.isPlotNodeSeen("终章 · 走向更远的地方", "csp-1"))
	}

	@Test
	fun heartToHeartRaisesMoraleAndIsLimitedToOncePerWeek() {
		val save = SaveFormat()
		val student = save.students.first()
		val beforeMorale = student.morale
		val beforeStability = student.stability

		assertTrue(save.spend("talk", student.id))
		assertEquals((beforeMorale + 3).coerceAtMost(10), student.morale)
		assertEquals(beforeStability + 1, student.stability)
		assertEquals(2, save.actionPoints)
		assertTrue(save.heartToHeartDoneThisWeek)

		// 同一周不能再谈心，行动点不被消耗。
		assertTrue(!save.spend("talk", save.students[1].id))
		assertEquals(2, save.actionPoints)
	}

	@Test
	fun mockContestRecordsRankedResultsAndIsLimitedToOncePerWeek() {
		val save = SaveFormat()

		assertTrue(save.spend("mock"))
		assertEquals(4, save.lastMockContest.size)
		assertEquals("模拟赛", save.lastMockContest.first().milestone)
		assertEquals(listOf(1, 2, 3, 4), save.lastMockContest.map { it.rank })
		assertTrue(save.mockContestDoneThisWeek)

		assertTrue(!save.spend("mock"))
	}

	@Test
	fun weeklyResetClearsSpecialActionFlags() {
		val save = SaveFormat()
		save.spend("talk", save.students.first().id)
		save.spend("mock")
		assertTrue(save.heartToHeartDoneThisWeek)
		assertTrue(save.mockContestDoneThisWeek)

		save.endWeek()

		assertTrue(!save.heartToHeartDoneThisWeek)
		assertTrue(!save.mockContestDoneThisWeek)
	}

	@Test
	fun moraleShiftsCompetitionOutcome() {
		fun scoreOfStrongestWith(morale: Int): Int {
			val save = SaveFormat()
			while (save.week < 79) save.endWeek()
			save.students.forEach {
				it.ability = 100
				it.technology = 100
				it.morale = morale
			}
			save.endWeek()
			return save.lastCompetition.first().score
		}
		// 满士气时的最高分应不低于零士气时（士气对发挥有正向贡献）。
		assertTrue(scoreOfStrongestWith(10) >= scoreOfStrongestWith(0))
	}

	@Test
	fun aRandomEventCanSurfaceOnAPlotSparseWeek() {
		val save = SaveFormat()
		save.finishedPrologue = true
		save.students.forEach {
			it.ability = 300
			it.technology = 300
		}
		var sawRandomEvent = false
		// 第 2–80 周几乎每周都有脚本周常剧情，随机事件会排队等到剧情稀疏的周（约 81 周后）再出现。
		while (save.week < 120 && save.outcome == GameOutcome.PLAYING) {
			save.endWeek()
			if (save.eventId != null) {
				if (save.eventId!!.startsWith("rnd-")) sawRandomEvent = true
				save.dismissEvent()
			}
			if (save.pendingMajorScene != null) save.finishMajorScene()
		}
		assertTrue(sawRandomEvent)
	}

	@Test
	fun eachRandomEventTriggersAtMostOnce() {
		val save = SaveFormat()
		save.finishedPrologue = true
		save.students.forEach {
			it.ability = 300
			it.technology = 300
		}
		val seenSequence = mutableListOf<String>()
		while (save.week < 200 && save.outcome == GameOutcome.PLAYING) {
			save.endWeek()
			if (save.eventId != null) {
				save.eventId!!.let { if (it.startsWith("rnd-")) seenSequence.add(it) }
				save.dismissEvent()
			}
			if (save.pendingMajorScene != null) save.finishMajorScene()
		}
		// 每个随机事件 id 在整局里不会重复出现。
		assertEquals(seenSequence.size, seenSequence.toSet().size)
	}

	@Test
	fun favorTenTriggersTheMidpointRelationshipScene() {
		val save = SaveFormat()
		val student = save.students.first()
		save.actionPoints = 10
		repeat(10) {
			save.spend("coach", student.id)
			if (save.eventId != null) save.dismissEvent()
			if (student.favor < 10 && save.pendingFavorScene != null) { save.acceptFavorScene(); save.finishMajorScene() }
		}

		assertEquals(10, student.favor)
		assertEquals("关系-WYM-10", save.pendingFavorScene)
	}

	@Test
	fun checkAchievementsUnlocksSatisfiedOnesOnlyOnce() {
		val save = SaveFormat()
		// 初始一无所有：还没结束序章，没有任何成就满足。
		assertTrue(save.checkAchievements().isEmpty())
		assertTrue(save.unlockedAchievements.isEmpty())

		save.finishedPrologue = true
		val firstBatch = save.checkAchievements()
		assertTrue(firstBatch.any { it.id == "ach-first-class" })
		assertTrue("ach-first-class" in save.unlockedAchievements)

		// 条件未再变化时，同一成就不会被重复返回。
		assertTrue(save.checkAchievements().none { it.id == "ach-first-class" })
	}

	@Test
	fun mockContestUnlocksTheMockMasterAchievement() {
		val save = SaveFormat()
		assertTrue("ach-mock-master" !in save.unlockedAchievements)

		save.spend("mock")
		val unlocked = save.checkAchievements()

		assertTrue(unlocked.any { it.id == "ach-mock-master" })
	}

	@Test
	fun clearingAllMilestonesUnlocksTheEndgameAchievements() {
		val save = SaveFormat()
		save.finishedPrologue = true
		while (save.week < 205 && save.outcome == GameOutcome.PLAYING) {
			// 每周把属性重新拉满，抵消自然衰减，确保四个里程碑都能通过。
			save.students.forEach {
				it.ability = 300
				it.technology = 300
			}
			save.endWeek()
			if (save.eventId != null) save.dismissEvent()
			if (save.pendingMajorScene != null) save.finishMajorScene()
			save.checkAchievements()
		}

		assertTrue("ach-csp" in save.unlockedAchievements)
		assertTrue("ach-noip" in save.unlockedAchievements)
		assertTrue("ach-team" in save.unlockedAchievements)
		assertTrue("ach-noi" in save.unlockedAchievements)
	}

	@Test
	fun collectingEveryRandomEventUnlocksTheEventfulAchievement() {
		val save = SaveFormat()
		save.finishedPrologue = true
		while (save.week < 205 && save.outcome == GameOutcome.PLAYING) {
			save.students.forEach {
				it.ability = 300
				it.technology = 300
			}
			save.endWeek()
			if (save.eventId != null) save.dismissEvent()
			if (save.pendingMajorScene != null) save.finishMajorScene()
		}
		save.checkAchievements()

		assertTrue("ach-eventful" in save.unlockedAchievements)
	}

	@Test
	fun walkingRaisesMoraleAndFavorButSharesTheWeeklyLimit() {
		val save = SaveFormat()
		val before = save.students.map { Pair(it.morale, it.favor) }

		assertTrue(save.spend("walk"))
		save.students.forEachIndexed { index, student ->
			assertEquals((before[index].first + 1).coerceAtMost(10), student.morale)
			assertEquals(before[index].second + 1, student.favor)
		}
		assertEquals(2, save.actionPoints)
		assertTrue(save.heartToHeartDoneThisWeek)

		// 散步与谈心共用每周一次的额度。
		assertTrue(!save.spend("talk", save.students.first().id))
		assertEquals(2, save.actionPoints)
	}

	@Test
	fun groupReviewRaisesAbilityAndStabilityForEveryone() {
		val save = SaveFormat()
		val before = save.students.map { Pair(it.ability, it.stability) }

		assertTrue(save.spend("review"))
		save.students.forEachIndexed { index, student ->
			assertEquals(before[index].first + 2, student.ability)
			assertEquals(before[index].second + 1, student.stability)
		}
		assertEquals(2, save.actionPoints)
	}

	@Test
	fun favorEightRoutesToAStructuredScene() {
		val save = SaveFormat()
		val student = save.students.first()
		save.actionPoints = 8
		repeat(8) {
			save.spend("coach", student.id)
			if (save.eventId != null) save.dismissEvent()
			if (student.favor < 8 && save.pendingFavorScene != null) { save.acceptFavorScene(); save.finishMajorScene() }
		}

		assertEquals(8, student.favor)
		assertEquals("关系-WYM-8", save.pendingFavorScene)
	}

	@Test
	fun walkingUnlocksTheWalkAchievement() {
		val save = SaveFormat()
		assertTrue("ach-walk" !in save.unlockedAchievements)

		save.spend("walk")
		val unlocked = save.checkAchievements()

		assertEquals(1, save.walkCount)
		assertTrue(unlocked.any { it.id == "ach-walk" })
	}

	@Test
	fun repeatedReviewUnlocksTheReviewerAchievement() {
		val save = SaveFormat()
		// 复盘无每周限制，但每周只有 3 行动点，需要跨周累计到 5 次。
		repeat(5) {
			save.spend("review")
			if (save.actionPoints == 0) save.endWeek()
		}

		assertEquals(5, save.reviewCount)
		assertTrue(save.checkAchievements().any { it.id == "ach-reviewer" })
	}

	@Test
	fun everyAchievementIdIsUnique() {
		assertEquals(ACHIEVEMENTS.size, ACHIEVEMENTS.map { it.id }.toSet().size)
	}

	@Test
	fun newGamePlusFactoryStartsFreshButCarriesMemory() {
		val carried = setOf("ach-first-class", "ach-noi")
		val save = SaveFormat.newGamePlus(slot = 2, carryOverAchievements = carried)

		assertTrue(save.newGamePlus)
		assertEquals(2, save.slot)
		// 继承“记忆”（成就），但其余进度全部从头开始。
		assertEquals(carried, save.unlockedAchievements)
		assertEquals(1, save.week)
		assertTrue(!save.finishedPrologue)
		assertEquals(GameOutcome.PLAYING, save.outcome)
	}

	@Test
	fun aBrandNewSaveIsNotInNewGamePlus() {
		assertTrue(!SaveFormat().newGamePlus)
	}

	@Test
	fun memoryRecallForcedAnswerCostsFavorAndStability() {
		val save = SaveFormat()
		val beforeFavor = save.students.map { it.favor }
		val beforeStability = save.students.map { it.stability }

		// 二周目 NOIP“凭记忆抢答”：用旧标签盖住眼前真实的她，好感与稳定性双双下滑。
		save.applyStoryChoice("（凭记忆抢答）你是李佳迪，永远的第一。")

		save.students.forEachIndexed { index, student ->
			assertEquals((beforeFavor[index] - 1).coerceAtLeast(0), student.favor)
			assertEquals((beforeStability[index] - 1).coerceAtLeast(0), student.stability)
		}
	}

	@Test
	fun memorySilenceQuietlyLowersMorale() {
		val save = SaveFormat()
		val beforeMorale = save.students.map { it.morale }

		// 二周目 NOIP 面对李佳迪的追问选择沉默：士气微降，一记无声的钝痛。
		save.applyStoryChoice("（沉默，不回答。）")

		save.students.forEachIndexed { index, student ->
			assertEquals((beforeMorale[index] - 1).coerceAtLeast(0), student.morale)
		}
	}

	@Test
	fun memoryHonestAnswerLeavesAttributesUntouched() {
		val save = SaveFormat()
		val snapshot = save.students.map { listOf(it.ability, it.technology, it.favor, it.stability, it.morale) }

		// 半吐真话的“很像一个人”：没有涨也没有明显扣——一次诚实的僵持，只留下一条选择记录。
		save.applyStoryChoice("我只是觉得你，很像一个人。")

		assertEquals(1, save.storyChoices.size)
		save.students.forEachIndexed { index, student ->
			assertEquals(snapshot[index], listOf(student.ability, student.technology, student.favor, student.stability, student.morale))
		}
	}

	@Test
	fun newGamePlusTrimmedLoopStillClearsTheFirstMilestone() {
		// 二周目精简界面只剩“集体提升水平 / 集体讲授科技 / 结束本周”，没有上课与单人辅导。
		// NOI 之前的里程碑一律放行，验证前期不会提前触发坏结局、能顺利通过 CSP-S（第15周）。
		val save = SaveFormat.newGamePlus(slot = 0)

		while (save.week < 15 && save.outcome == GameOutcome.PLAYING) {
			// 每周三点行动点：两点提水平、一点提科技，然后结束本周。
			save.spend("ngplus-ability")
			save.spend("ngplus-ability")
			save.spend("ngplus-tech")
			save.endWeek()
		}

		assertEquals(GameOutcome.PLAYING, save.outcome, "二周目精简循环不应在 CSP-S 前触发坏结局")
		assertTrue("CSP-S" in save.completedMilestones, "二周目精简循环应能通过 CSP-S 里程碑")
	}

	@Test
	fun newGamePlusAlwaysFailsAtTheNoiStage() {
		// 二周目无论玩家怎么经营，走到 NOI 都必然失利（拿不到金牌），但前面的里程碑都会通过。
		// 这里故意什么都不做，一路结束本周直到 NOI 周（第60周），验证结局注定崩塌在 NOI。
		val save = SaveFormat.newGamePlus(slot = 0)

		var guard = 0
		while (save.outcome == GameOutcome.PLAYING && guard < 200) {
			save.pendingMajorScene = null // 跳过里程碑场景，直接推进到下一周
			save.finishMajorScene()
			save.endWeek()
			guard++
		}

		assertEquals(GameOutcome.BAD_ENDING, save.outcome, "二周目必然以失败告终")
		// 前三个里程碑都通过了，失败只发生在最高一级的 NOI。
		assertTrue("CSP-S" in save.completedMilestones)
		assertTrue("NOIP" in save.completedMilestones)
		assertTrue("省队" in save.completedMilestones)
		assertTrue("NOI" in save.completedMilestones, "应当走到 NOI 才失败")
		assertEquals("NOI", save.lastCompetition.firstOrNull()?.milestone)
		assertTrue(save.isTopStageFailure(), "NOI 失利应被识别为最高阶段失败")
		// 走的是二周目专属的 NOI 崩解场景，而非通用失败场景。
		assertEquals("NOI", save.pendingMajorScene)
	}

	@Test
	fun newGamePlusStatsDeclineEvenWhenThePlayerTriesEverything() {
		// “过程也真的很难”：即便玩家每周把三点行动全部砸在两种集体行动上，
		// 每周的衰减仍大于收益之和，四个人的水平/科技整体一路下滑——怎么补都补不上。
		val save = SaveFormat.newGamePlus(slot = 0)
		val abilityBefore = save.students.sumOf { it.ability }
		val techBefore = save.students.sumOf { it.technology }

		// 连续几周拼尽全力：每周两点提水平、一点提科技，再结束本周。
		repeat(5) {
			if (save.outcome != GameOutcome.PLAYING) return@repeat
			save.pendingMajorScene = null
			save.finishMajorScene()
			save.spend("ngplus-ability")
			save.spend("ngplus-ability")
			save.spend("ngplus-tech")
			save.endWeek()
		}

		val abilityAfter = save.students.sumOf { it.ability }
		val techAfter = save.students.sumOf { it.technology }
		assertTrue(abilityAfter < abilityBefore, "拼尽全力也压不住水平的下滑")
		assertTrue(techAfter < techBefore, "拼尽全力也压不住科技的下滑")
	}

	@Test
	fun newGamePlusFailsAtNoiEvenWhenThePlayerMaxesOutEveryWeek() {
		// 即便玩家每周都用满两种集体行动，走到 NOI 依然必然失利——难度加大但结局不改。
		val save = SaveFormat.newGamePlus(slot = 0)

		var guard = 0
		while (save.outcome == GameOutcome.PLAYING && guard < 200) {
			save.pendingMajorScene = null
			save.finishMajorScene()
			save.spend("ngplus-ability")
			save.spend("ngplus-ability")
			save.spend("ngplus-tech")
			save.endWeek()
			guard++
		}

		assertEquals(GameOutcome.BAD_ENDING, save.outcome, "拼尽全力也逃不过 NOI 必败")
		assertTrue("CSP-S" in save.completedMilestones)
		assertTrue("NOIP" in save.completedMilestones)
		assertTrue("省队" in save.completedMilestones)
		assertTrue("NOI" in save.completedMilestones, "应当走到 NOI 才失败")
		assertTrue(save.isTopStageFailure(), "NOI 失利应被识别为最高阶段失败")
		assertEquals("NOI", save.pendingMajorScene)
	}

	@Test
	fun onlyTheTopStageFailureUnlocksNewGamePlus() {
		// 只有停在“NOI（NOI Au）阶段失利没拿金牌”的失败结局，才算解锁二周目的入口。
		val noiFailure = SaveFormat().apply {
			outcome = GameOutcome.BAD_ENDING
			lastCompetition = mutableListOf(
				CompetitionResult("NOI", "WYM", "吴一鸣", 40, 1, "")
			)
		}
		assertTrue(noiFailure.isTopStageFailure(), "NOI 阶段失利应能解锁二周目")

		// 早期阶段（如 CSP-S）失利不解锁。
		val earlyFailure = SaveFormat().apply {
			outcome = GameOutcome.BAD_ENDING
			lastCompetition = mutableListOf(
				CompetitionResult("CSP-S", "LZN", "刘子诺", 30, 1, "")
			)
		}
		assertTrue(!earlyFailure.isTopStageFailure(), "CSP-S 阶段失利不应解锁二周目")

		// 成功结局（比如家庭结局、NOI 通关）也不再解锁二周目。
		val familyEnding = SaveFormat().apply {
			outcome = GameOutcome.FAMILY_ENDING
			lastCompetition = mutableListOf(
				CompetitionResult("NOI", "LJD", "李佳迪", 90, 1, "")
			)
		}
		assertTrue(!familyEnding.isTopStageFailure(), "成功结局不应解锁二周目")
	}

	@Test
	fun coachingIsBlockedWhenFavorSceneIsPending() {
		val save = SaveFormat()
		val student = save.students.first()
		save.pendingFavorScene = "关系-WYM-4"

		// 有好感剧情待确认时，coach 不应该继续提升好感。
		assertTrue(!save.spend("coach", student.id))
		assertEquals(0, student.favor)
	}

	@Test
	fun talkingIsBlockedWhenFavorSceneIsPending() {
		val save = SaveFormat()
		val student = save.students.first()
		save.pendingFavorScene = "关系-WYM-4"

		assertTrue(!save.spend("talk", student.id))
		assertEquals(0, student.favor)
	}

	@Test
	fun walkingIsBlockedWhenFavorSceneIsPending() {
		val save = SaveFormat()
		save.pendingFavorScene = "关系-WYM-4"

		assertTrue(!save.spend("walk"))
		save.students.forEach { assertEquals(0, it.favor) }
	}

	@Test
	fun favorSceneResumesAfterAcceptingPendingScene() {
		val save = SaveFormat()
		val student = save.students.first()
		save.pendingFavorScene = "关系-WYM-4"

		// 有待确认的好感剧情时，coach 被阻塞。
		assertTrue(!save.spend("coach", student.id))

		// 确认好感剧情后，coach 恢复正常。
		save.acceptFavorScene()
		assertTrue(save.spend("coach", student.id))
		assertEquals(1, student.favor)
	}
}
