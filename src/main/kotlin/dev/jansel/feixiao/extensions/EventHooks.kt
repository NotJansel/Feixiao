package dev.jansel.feixiao.extensions

import dev.jansel.feixiao.database.collections.StreamerCollection
import dev.jansel.feixiao.database.entities.StreamerData
import dev.jansel.feixiao.logger
import dev.jansel.feixiao.twitchClient
import dev.jansel.feixiao.utils.tchannelid
import dev.jansel.feixiao.utils.tserverid
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.gateway.ReadyEvent
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.event
import org.litote.kmongo.eq

class EventHooks : Extension() {
	override val name = "eventhooks"

	override suspend fun setup() {
		event<ReadyEvent> {
			action {
				logger.info { "Bot is ready!" }
				val onlineLog =
					kord.getGuildOrNull(tserverid)?.getChannelOf<GuildMessageChannel>(tchannelid)
				onlineLog?.createMessage("Bot Online!")
				kord.editPresence { listening("the database") }
				// check every entry in the database and enable the stream event listener if a server is listening to the streamer
				StreamerCollection().collection.find().toList().forEach {
					if (it.servers.isNotEmpty()) {
						twitchClient!!.clientHelper.enableStreamEventListener(it.name)
						logger.info { "Enabled stream event listener for ${it.name}" }
					} else {
						logger.info { "No servers are listening to ${it.name}, deleting from the database..." }
						StreamerCollection().collection.deleteMany(StreamerData::name eq it.name)
					}
				}
			}
		}
	}
}
