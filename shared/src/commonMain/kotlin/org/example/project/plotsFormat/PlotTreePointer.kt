package org.example.project.plotsFormat

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class PlotTreePointer(val plot: PlotTree) {
	var currentNodeId by mutableStateOf(plot.entryNodeId)
		private set

	fun getCurrentNode(): PlotTree.SingleNode {
		if (!plot.plot.containsKey(currentNodeId)) {
			throw Exception("节点 $currentNodeId 不存在")
		} else {
			return plot.plot[currentNodeId]!!
		}
	}

	/**
	 * @return true 表示还有剧情，false 反之
	 */
	fun nextNode(option: PlotTree.SingleOption? = null): Boolean {
		when (val currentNode = getCurrentNode()) {
			is PlotTree.SingleNode.Normal -> {
				require(option == null) {
					"普通节点不应该有选项"
				}
				if (currentNode.nextNodeId == null) {
					return false
				}
				currentNodeId = currentNode.nextNodeId
				return true
			}

			is PlotTree.SingleNode.Option -> {
				require(option != null) {
					"选择节点需要选项以跳转到下一个节点"
				}
				val selectedOption = currentNode.options.find { it == option }
					?: throw Exception("该选项不存在: $option")
				currentNodeId = selectedOption.nextNodeId
					?: return false
				return true
			}

			is PlotTree.SingleNode.Custom -> {
				require(option == null) {
					"特殊节点不应该有选项"
				}
				currentNode.block()
				if (currentNode.nextNodeId == null) {
					return false
				}
				currentNodeId = currentNode.nextNodeId
				return true
			}
		}
	}

	fun jumpToNode(nodeId: String) {
		if (plot.plot.containsKey(nodeId)) {
			currentNodeId = nodeId
		}
	}
}