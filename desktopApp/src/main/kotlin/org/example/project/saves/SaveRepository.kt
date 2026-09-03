package org.example.project.saves

import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * 存档信息，用于在主菜单展示某个存档槽的概况。
 */
data class SaveSlotInfo(
	val slot: Int,
	val week: Int,
	val milestone: String,
	val outcome: GameOutcome,
	val savedAtMillis: Long
)

object SaveRepository {
	/** 可用的存档槽位数量。 */
	const val SLOT_COUNT = 3

	private val json = Json {
		prettyPrint = true
		ignoreUnknownKeys = true
	}

	private val saveDir: Path = Path.of(System.getProperty("user.home"), ".wyjgalgame")

	/** 旧版本使用的单存档路径，用于向后兼容自动迁移到槽 0。 */
	private val legacyPath: Path = saveDir.resolve("save.json")

	private val settingsPath: Path = saveDir.resolve("settings.json")

	/** 全局进度文件：记录跨存档的成就/解锁状态，例如二周目是否已解锁。 */
	private val progressPath: Path = saveDir.resolve("progress.json")

	private fun slotPath(slot: Int): Path = saveDir.resolve("save-$slot.json")

	private fun migrateLegacyIfNeeded() {
		if (Files.exists(legacyPath) && !Files.exists(slotPath(0))) {
			runCatching {
				Files.createDirectories(saveDir)
				Files.copy(legacyPath, slotPath(0))
			}
		}
	}

	fun exists(slot: Int): Boolean {
		migrateLegacyIfNeeded()
		return Files.exists(slotPath(slot))
	}

	/** 是否存在任意存档。 */
	fun hasAnySave(): Boolean = (0 until SLOT_COUNT).any { exists(it) }

	fun load(slot: Int): SaveFormat? {
		if (!exists(slot)) return null
		return runCatching {
			json.decodeFromString<SaveFormat>(Files.readString(slotPath(slot)))
		}.getOrNull()
	}

	fun save(gameState: SaveFormat, slot: Int): Boolean = runCatching {
		Files.createDirectories(saveDir)
		Files.writeString(
			slotPath(slot),
			json.encodeToString(gameState),
			StandardOpenOption.CREATE,
			StandardOpenOption.TRUNCATE_EXISTING,
			StandardOpenOption.WRITE
		)
		true
	}.getOrDefault(false)

	fun delete(slot: Int): Boolean = runCatching {
		Files.deleteIfExists(slotPath(slot))
	}.getOrDefault(false)

	/** 读取某个槽位的概况，不存在或损坏时返回 null。 */
	fun slotInfo(slot: Int): SaveSlotInfo? {
		val state = load(slot) ?: return null
		val savedAt = runCatching { Files.getLastModifiedTime(slotPath(slot)).toMillis() }.getOrDefault(0L)
		return SaveSlotInfo(slot, state.week, state.currentMilestone, state.outcome, savedAt)
	}

	/** 返回所有槽位的概况，未使用的槽位为 null。 */
	fun allSlots(): List<SaveSlotInfo?> = (0 until SLOT_COUNT).map { slotInfo(it) }

	/** 读取全局设置；不存在或损坏时返回默认设置。 */
	fun loadSettings(): GameSettings {
		if (!Files.exists(settingsPath)) return GameSettings()
		return runCatching {
			json.decodeFromString<GameSettings>(Files.readString(settingsPath)).coerced()
		}.getOrDefault(GameSettings())
	}

	/** 保存全局设置。 */
	fun saveSettings(settings: GameSettings): Boolean = runCatching {
		Files.createDirectories(saveDir)
		Files.writeString(
			settingsPath,
			json.encodeToString(settings.coerced()),
			StandardOpenOption.CREATE,
			StandardOpenOption.TRUNCATE_EXISTING,
			StandardOpenOption.WRITE
		)
		true
	}.getOrDefault(false)

	/** 读取跨存档的全局进度；不存在或损坏时返回默认（未解锁）。 */
	fun loadProgress(): GlobalProgress {
		if (!Files.exists(progressPath)) return GlobalProgress()
		return runCatching {
			json.decodeFromString<GlobalProgress>(Files.readString(progressPath))
		}.getOrDefault(GlobalProgress())
	}

	/** 保存跨存档的全局进度。 */
	fun saveProgress(progress: GlobalProgress): Boolean = runCatching {
		Files.createDirectories(saveDir)
		Files.writeString(
			progressPath,
			json.encodeToString(progress),
			StandardOpenOption.CREATE,
			StandardOpenOption.TRUNCATE_EXISTING,
			StandardOpenOption.WRITE
		)
		true
	}.getOrDefault(false)

	/** 二周目是否已解锁（曾经停在过 NOI/NOI Au 失利没拿金牌的失败结局）。 */
	fun isNewGamePlusUnlocked(): Boolean = loadProgress().newGamePlusUnlocked

	/**
	 * 记录一次“NOI（NOI Au）阶段失利、没拿到金牌”的失败结局：永久解锁二周目，
	 * 并把本局已解锁成就并入全局记忆。无论此后存档如何变动，解锁状态都不再丢失。
	 */
	fun recordTopStageFailureCleared(unlockedAchievements: Set<String>) {
		val progress = loadProgress()
		progress.noiAuFailureCleared = true
		progress.carriedAchievements.addAll(unlockedAchievements)
		saveProgress(progress)
	}
}
