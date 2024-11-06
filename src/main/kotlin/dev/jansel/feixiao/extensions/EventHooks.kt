package dev.jansel.feixiao.extensions

import dev.jansel.feixiao.database.collections.StreamerCollection
import dev.jansel.feixiao.twitchClient
import dev.kord.core.event.gateway.ReadyEvent
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.event

class EventHooks : Extension() {
	override val name = "eventhooks"

	override suspend fun setup() {
		event<ReadyEvent> {
			action {
				println("Bot is ready!")
				kord.editPresence { listening("to the database") }
				// check every entry in the database and enable the stream event listener
				StreamerCollection().collection.find().toList().forEach {
					twitchClient!!.clientHelper.enableStreamEventListener(it.name)
					println("Enabled stream event listener for ${it.name}")
				}
			}
		}
	}
}
