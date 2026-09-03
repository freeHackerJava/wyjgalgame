package org.example.project.plotsFormat

class PlotTree(
	val plot: Map<String, SingleNode>,
	val entryNodeId: String
) {
	data class SingleOption(
		val content: String,
		val nextNodeId: String?
	)

	sealed class SingleNode {
		data class Normal(
			val id: String,
			val speaker: Characters,
			val content: String,
			val nextNodeId: String?
		) : SingleNode()

		data class Option(
			val id: String,
			val question: String,
			val options: List<SingleOption>
		) : SingleNode()

		data class Custom(
			val id: String,
			val block: () -> Unit,
			val nextNodeId: String?
		) : SingleNode()
	}

	init {
		require(plot.containsKey(entryNodeId)) {
			"剧情树的入口节点不存在"
		}
	}

	class Builder {
		private val nodes = mutableMapOf<String, SingleNode>()
		private var entryId: String? = null

		fun normal(
			id: String,
			speaker: Characters,
			content: String,
			nextNodeId: String?
		) = apply {
			nodes[id] = SingleNode.Normal(id, speaker, content, nextNodeId)
		}

		fun option(
			id: String,
			question: String,
			options: List<Pair<String, String?>>
		) = apply {
			val optionList = options.map { (content, nextId) ->
				SingleOption(content, nextId)
			}
			nodes[id] = SingleNode.Option(id, question, optionList)
		}

		fun custom(
			id: String,
			block: () -> Unit,
			nextNodeId: String?
		) = apply {
			nodes[id] = SingleNode.Custom(id, block, nextNodeId)
		}

		fun entry(id: String) = apply {
			entryId = id
		}

		fun build(): PlotTree {
			return PlotTree(nodes.toMap(), entryId ?: nodes.keys.firstOrNull() ?: error("节点为空"))
		}
	}

	companion object {
		fun plotTree(block: Builder.() -> Unit): PlotTree {
			return Builder().apply(block).build()
		}
	}
}