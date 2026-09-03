package org.example.project.plots

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.example.project.plotsFormat.PlotTree
import org.example.project.plotsFormat.PlotTreePointer

class MajorScenesTest {
	@Test
	fun cspSceneKeepsBothMajorResponses() {
		val pointer = PlotTreePointer(CspScene)
		repeat(21) { assertTrue(pointer.nextNode()) }

		val option = pointer.getCurrentNode() as PlotTree.SingleNode.Option
		assertEquals(2, option.options.size)
		assertEquals("你会怎么回答李佳迪？", option.question)
	}

	@Test
	fun failureSceneHasA完整FirstPersonEpilogue() {
		val texts = FailureScene.plot.values.mapNotNull { node ->
			(node as? PlotTree.SingleNode.Normal)?.content
		}

		assertTrue(texts.size >= 70)
		assertTrue(texts.any { it.contains("NOI Ag") })
		assertTrue(texts.any { it.contains("ICPC Final Au") })
		assertTrue(texts.any { it.contains("我不能用‘你已经很努力了’") })
		assertTrue(texts.any { it.contains("把板擦放到黑板上") })
	}

	@Test
	fun everyStudentHasAFullRelationshipScene() {
		listOf(WymRelationshipScene, LznRelationshipScene, ZhcRelationshipScene, LjdRelationshipScene).forEach { scene ->
			assertTrue(scene.plot.size >= 20)
		}
	}

	@Test
	fun everyStudentHasAFavorEightScene() {
		listOf(WymFavor8Scene, LznFavor8Scene, ZhcFavor8Scene, LjdFavor8Scene).forEach { scene ->
			// 好感 8 支线是一条无环、终点悬空的线性剧情。
			var id: String? = scene.entryNodeId
			val visited = mutableSetOf<String>()
			var last: PlotTree.SingleNode.Normal? = null
			while (id != null) {
				assertTrue(id in scene.plot, "好感8节点缺失: $id")
				assertTrue(visited.add(id), "好感8剧情出现环: $id")
				val node = scene.plot[id] as PlotTree.SingleNode.Normal
				last = node
				id = node.nextNodeId
			}
			assertTrue(scene.plot.size >= 10)
			assertEquals(null, last?.nextNodeId)
		}
	}

	@Test
	fun noiSceneEndsImmediatelyAfterTheFatalQuestion() {
		val finalNode = NoiScene.plot["noi-62"] as PlotTree.SingleNode.Normal

		// 结局落在王怡钧的自我承认上：她不再抛出"我配吗"式的提问，而是承认这道题从没替自己解过。
		assertTrue(finalNode.content.contains("用她们的奖牌来回答"))
		assertTrue(finalNode.content.contains("从来没有替自己解过"))
		assertEquals(null, finalNode.nextNodeId)
	}

	@Test
	fun noiSceneStaysFullyConnectedFromEntryToTheFatalQuestion() {
		// 从入口顺着 nextNodeId 一路走，必须不出现悬空引用，并且最终停在 noi-62。
		var id: String? = NoiScene.entryNodeId
		val visited = mutableSetOf<String>()
		var last: String? = null
		while (id != null) {
			assertTrue(id in NoiScene.plot, "剧情节点缺失: $id")
			assertTrue(visited.add(id), "剧情出现环: $id")
			last = id
			id = (NoiScene.plot[id] as PlotTree.SingleNode.Normal).nextNodeId
		}

		assertEquals("noi-62", last)
		// 扩写后的终章比原先更细腻，节点数应明显增加。
		assertTrue(NoiScene.plot.size >= 60)
	}

	@Test
	fun everyFailureVariantIsAConnectedChainSharingTheReflection() {
		mapOf(
			"CSP-S" to FailureScene,
			"NOIP" to NoipFailureScene,
			"省队" to TeamFailureScene,
			"NOI" to NoiFailureScene
		).forEach { (label, scene) ->
			// 失败结局以线性文本为主，中途插入一次“不改变走向”的玩家互动选项：
			// 两个分支都必须重新汇入主线，并最终停在 failure-80。
			val terminals = mutableSetOf<String>()

			fun walk(start: String) {
				var id: String? = start
				val visited = mutableSetOf<String>()
				while (id != null) {
					assertTrue(id in scene.plot, "[$label] 剧情节点缺失: $id")
					assertTrue(visited.add(id), "[$label] 剧情出现环: $id")
					when (val node = scene.plot[id]) {
						is PlotTree.SingleNode.Normal -> {
							if (node.nextNodeId == null) terminals.add(id)
							id = node.nextNodeId
						}
						is PlotTree.SingleNode.Option -> {
							// 选项分支互不改变最终走向：每个分支各自走完，最后都要汇回主线。
							node.options.forEach { opt -> opt.nextNodeId?.let { walk(it) } }
							id = null
						}
						else -> error("[$label] 失败结局出现不支持的节点类型: $id")
					}
				}
			}

			walk(scene.entryNodeId)

			// 无论玩家选哪个分支，最终都只停在共用独白的终点 failure-80。
			assertEquals(setOf("failure-80"), terminals, "[$label] 失败结局未统一收束到共用独白的终点")
			assertTrue(scene.plot.size >= 70, "[$label] 失败结局段落过少")
			assertTrue("failure-70" in scene.plot, "[$label] 缺少共用反思独白节点")
			// 长篇独白中间应保留一次玩家互动选项。
			assertTrue(scene.plot["failure-64-choice"] is PlotTree.SingleNode.Option, "[$label] 缺少独白中的玩家互动选项")
		}
	}

	@Test
	fun failureOpeningsAreDistinctPerCheckpoint() {
		fun opening(scene: PlotTree) = (scene.plot["failure-1"] as PlotTree.SingleNode.Normal).content
		val openings = listOf(FailureScene, NoipFailureScene, TeamFailureScene, NoiFailureScene).map(::opening)
		// 四个阶段的开场文本应两两不同。
		assertEquals(openings.size, openings.toSet().size)
		assertTrue(opening(NoipFailureScene).contains("NOIP"))
		assertTrue(opening(TeamFailureScene).contains("省队"))
		assertTrue(opening(NoiFailureScene).contains("最后一扇门"))
	}

	@Test
	fun prologueIsAConnectedChainThatEndsOnTheSh020Goal() {
		var id: String? = Prologue.entryNodeId
		val visited = mutableSetOf<String>()
		var last: PlotTree.SingleNode.Normal? = null
		while (id != null) {
			assertTrue(id in Prologue.plot, "序章节点缺失: $id")
			assertTrue(visited.add(id), "序章出现环: $id")
			val node = Prologue.plot[id] as PlotTree.SingleNode.Normal
			last = node
			id = node.nextNodeId
		}
		assertEquals(null, last?.nextNodeId)
		// 序章已扩写，节点数明显增加，且保留四位学生的自我介绍与最终目标。
		assertTrue(Prologue.plot.size >= 30)
		val texts = Prologue.plot.values.mapNotNull { (it as? PlotTree.SingleNode.Normal)?.content }
		assertTrue(texts.any { it.contains("刘子诺") })
		assertTrue(texts.any { it.contains("吴一鸣") })
		assertTrue(texts.any { it.contains("朱皓辰") })
		assertTrue(texts.any { it.contains("李佳迪") })
		assertTrue(texts.any { it.contains("NOI Au") })
	}
}
