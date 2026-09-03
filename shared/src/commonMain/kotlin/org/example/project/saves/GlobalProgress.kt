package org.example.project.saves

import kotlinx.serialization.Serializable

/**
 * 跨存档的全局进度，独立于任何单个存档槽，持久化到 ~/.wyjgalgame/progress.json。
 *
 * 用来记录“只要玩过就永久生效”的解锁状态。目前唯一的用途是：任意存档在
 * NOI（NOI Au）阶段失利、没能拿到金牌的失败结局里，永久解锁二周目——
 * 即使那个存档被删除或覆盖，解锁也不会丢失。
 */
@Serializable
data class GlobalProgress(
	/** 是否停在过“NOI（NOI Au）失利没拿金牌”的失败结局，从而解锁二周目。 */
	var noiAuFailureCleared: Boolean = false,
	/** 历史上解锁过的全部成就 id 的并集，供二周目继承“记忆”。 */
	var carriedAchievements: MutableSet<String> = mutableSetOf()
) {
	val newGamePlusUnlocked: Boolean get() = noiAuFailureCleared
}
