package org.example.project.saves

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameSettingsTest {
	@Test
	fun defaultsAreSensible() {
		val settings = GameSettings()
		assertEquals(24, settings.textSpeedMillis)
		assertEquals(1200, settings.autoPlayDelayMillis)
		assertTrue(settings.skipReadOnly)
		assertTrue(!settings.fullscreen)
	}

	@Test
	fun coercedClampsOutOfRangeValues() {
		val tooFast = GameSettings(textSpeedMillis = -30, autoPlayDelayMillis = 10).coerced()
		assertEquals(0, tooFast.textSpeedMillis)
		assertEquals(200, tooFast.autoPlayDelayMillis)

		val tooSlow = GameSettings(textSpeedMillis = 9000, autoPlayDelayMillis = 999999).coerced()
		assertEquals(200, tooSlow.textSpeedMillis)
		assertEquals(5000, tooSlow.autoPlayDelayMillis)
	}

	@Test
	fun presetsAreWithinTheAllowedRange() {
		GameSettings.TEXT_SPEED_PRESETS.forEach { (_, value) ->
			assertEquals(value, GameSettings(textSpeedMillis = value).coerced().textSpeedMillis)
		}
		GameSettings.AUTO_SPEED_PRESETS.forEach { (_, value) ->
			assertEquals(value, GameSettings(autoPlayDelayMillis = value).coerced().autoPlayDelayMillis)
		}
	}
}
