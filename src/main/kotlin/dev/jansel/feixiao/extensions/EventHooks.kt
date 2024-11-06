package dev.jansel.feixiao.extensions

import dev.jansel.feixiao.database.collections.StreamerCollection
import dev.jansel.feixiao.logger
import dev.jansel.feixiao.twitchClient
import dev.kord.core.event.gateway.ReadyEvent
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.event

class EventHooks : Extension() {
	override val name = "eventhooks"

	override suspend fun setup() {
		event<ReadyEvent> {
			action {
				logger.info { "Bot is ready!" }
				kord.editPresence { listening("the database") }
				// check every entry in the database and enable the stream event listener
				StreamerCollection().collection.find().toList().forEach {
					twitchClient!!.clientHelper.enableStreamEventListener(it.name)
					logger.info { "Enabled stream event listener for ${it.name}" }
				}
			}
		}
	}
}
