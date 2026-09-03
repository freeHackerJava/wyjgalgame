package org.example.project.plots

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.example.project.plotsFormat.PlotTree

/**
 * 二周目（New Game+）专属剧情的连通性与叙事弧线测试。
 *
 * 三个变体分别对应“记忆崩坏”的三阶段：细节错位 → 人物混淆 → 现实渗透：
 * - [NgPlusPrologue] 序章：王怡钧以为能凭记忆拯救一切；
 * - [NgPlusNoipScene] NOIP：记忆开始对不上，玩家做的选择改变不了走向；
 * - [NgPlusNoiScene] NOI：名字与面孔彻底错位，只剩一句关于遗忘的提问。
 */
class NewGamePlusScenesTest {

	/** 顺着 Normal 节点的 nextNodeId 走完一条无环线性链，返回终点节点。 */
	private fun walkLinearChain(scene: PlotTree): PlotTree.SingleNode.Normal {
		var id: String? = scene.entryNodeId
		val visited = mutableSetOf<String>()
		var last: PlotTree.SingleNode.Normal? = null
		while (id != null) {
			assertTrue(id in scene.plot, "二周目节点缺失: $id")
			assertTrue(visited.add(id), "二周目剧情出现环: $id")
			val node = scene.plot[id] as PlotTree.SingleNode.Normal
			last = node
			id = node.nextNodeId
		}
		return last!!
	}

	@Test
	fun ngPlusPrologueIsAConnectedChainThatEndsOnTheIllusionOfControl() {
		val last = walkLinearChain(NgPlusPrologue)

		assertEquals(null, last.nextNodeId)
		// 序章收在“以为握着全部答案”的掌控幻觉上，为后续崩解埋下反差。
		assertTrue(last.content.contains("握着这条路的全部答案"))
		assertTrue(last.content.contains("折出裂痕"))

		val texts = NgPlusPrologue.plot.values.mapNotNull { (it as? PlotTree.SingleNode.Normal)?.content }
		// “带着记忆重来、这次一定能更好”的希望基调必须出现。
		assertTrue(texts.any { it.contains("我记得这一天") })
		assertTrue(texts.any { it.contains("这一次，我知道该怎么回答") })
	}

	@Test
	fun ngPlusNoipKeepsThreeMemoryDrivenResponsesThatAllRejoinTheMainline() {
		val option = NgPlusNoipScene.plot["ng-noip-choice"] as PlotTree.SingleNode.Option
		assertEquals(3, option.options.size)

		// 三个分支各自走完，都必须重新汇入 ng-noip-merge，并最终停在同一个终点。
		val terminals = mutableSetOf<String>()
		option.options.forEach { opt ->
			var id: String? = opt.nextNodeId
			val visited = mutableSetOf<String>()
			while (id != null) {
				assertTrue(id in NgPlusNoipScene.plot, "NOIP 分支节点缺失: $id")
				assertTrue(visited.add(id), "NOIP 分支出现环: $id")
				val node = NgPlusNoipScene.plot[id] as PlotTree.SingleNode.Normal
				if (node.nextNodeId == null) terminals.add(id)
				id = node.nextNodeId
			}
		}
		assertEquals(setOf("ng-noip-end"), terminals, "二周目 NOIP 分支未统一收束到无力感的终点")
	}

	@Test
	fun ngPlusNoipSurfacesTheMemoryMismatch() {
		val texts = NgPlusNoipScene.plot.values.mapNotNull { (it as? PlotTree.SingleNode.Normal)?.content }
		// 细节错位（记不清年糕的样子）与人物混淆（分不清哪份需要属于谁）是裂缝阶段的核心。
		assertTrue(texts.any { it.contains("不记得年糕的样子") })
		assertTrue(texts.any { it.contains("哪一份需要，属于哪一个人") })
	}

	@Test
	fun ngPlusNoiEndsOnAcknowledgingPowerlessness() {
		val last = walkLinearChain(NgPlusNoiScene)

		assertEquals(null, last.nextNodeId)
		// 终章落在“空教室 + 椅子微动”的尾声上，不提供任何安慰。
		assertTrue(last.content.contains("一切归于寂静"))

		val texts = NgPlusNoiScene.plot.values.mapNotNull { (it as? PlotTree.SingleNode.Normal)?.content }
		// “记得却记不清”的遗忘提问是二周目结局的落点。
		assertTrue(texts.any { it.contains("但我记不清她们了") })
		assertTrue(texts.any { it.contains("她们，还会记得我吗") })
	}
}
