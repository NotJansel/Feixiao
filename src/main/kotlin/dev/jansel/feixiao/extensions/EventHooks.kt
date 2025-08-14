package dev.jansel.feixiao.extensions

import dev.jansel.feixiao.database.collections.StreamerCollection
import dev.jansel.feixiao.database.entities.StreamerData
import dev.jansel.feixiao.logger
import dev.jansel.feixiao.twitchClient
import dev.jansel.feixiao.utils.getTwitchNameById
import dev.jansel.feixiao.utils.tchannelid
import dev.jansel.feixiao.utils.tserverid
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.gateway.ReadyEvent
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.event
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

class EventHooks : Extension() {
	override val name = "eventhooks"

	/**
	 * Sets presence and enables Twitch stream listeners for streamers with subscribers at startup.
	 * If TEST_SERVER/TEST_CHANNEL are not set, the "Bot Online!" message is skipped.
	 */
	override suspend fun setup() {
		event<ReadyEvent> {
			action {
				logger.info { "Bot is ready!" }
				// Optionally announce online status to test guild/channel if configured
				val onlineLog = if (tserverid != null && tchannelid != null) {
					kord.getGuildOrNull(tserverid)?.getChannelOf<GuildMessageChannel>(tchannelid)
				} else null
				onlineLog?.createMessage("Bot Online!")
				kord.editPresence { listening("the database") }

				// Enable stream event listeners for all streamers that have at least one subscribing server
				val repo = StreamerCollection()
				repo.collection.find().toList().forEach { data ->
					if (data.servers.isNotEmpty()) {
						val id = data.id
						val currentName = if (id != null) getTwitchNameById(id) ?: data.name else data.name
						if (!currentName.isNullOrBlank()) {
							twitchClient?.clientHelper?.enableStreamEventListener(currentName)
							logger.info { "Enabled stream event listener for $currentName" }
							repo.collection.updateOne(StreamerData::name eq data.name, setValue(StreamerData::name, currentName))
						}
					} else {
						logger.info { "No servers are listening to ${data.name}, deleting from the database..." }
						twitchClient?.clientHelper?.disableStreamEventListener(data.name)
						repo.collection.deleteMany(StreamerData::name eq data.name)
					}
				}
			}
		}
	}
}
