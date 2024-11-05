package dev.jansel.feixiao.extensions

import dev.kord.core.event.gateway.ReadyEvent
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.event

class EventHooks : Extension() {
	override val name = "eventhooks"

	override suspend fun setup() {
		event<ReadyEvent> {
			action {
				println("Bot is ready!")
				kord.editPresence { "electing a president..." }
			}
		}
	}
}
