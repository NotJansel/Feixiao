package dev.jansel.feixiao.utils

import com.github.philippheuer.events4j.reactor.ReactorEventHandler
import com.github.twitch4j.TwitchClientBuilder
import com.github.twitch4j.events.ChannelGoLiveEvent
import dev.jansel.feixiao.botRef
import dev.jansel.feixiao.database.collections.StreamerCollection
import dev.jansel.feixiao.twitchClient
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kordex.core.koin.KordExKoinComponent
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class Twitch : KordExKoinComponent {
	suspend fun init() {
		twitchClient = TwitchClientBuilder.builder()
			.withEnableHelix(true)
			.withDefaultEventHandler(ReactorEventHandler::class.java)
			.withClientId(twitchcid)
			.withClientSecret(twitchcs)
			.build()

		twitchClient!!.eventManager.onEvent(ChannelGoLiveEvent::class.java) {
			dev.jansel.feixiao.logger.info { "${it.channel.name} went live!" }
			runBlocking {
				launch {
					val streamer = StreamerCollection().getData(it.channel.name)
					for (server in streamer!!.servers) {
						val channel = botRef!!.kordRef.getChannelOf<GuildMessageChannel>(server.channelId)
						val role = server.roleId
						val livemessage = server.liveMessage

						if (role != null) {
							if (livemessage != null) {
								channel?.createMessage(
									livemessage
										.replace("{name}", it.channel.name)
										.replace("{category}", it.stream.gameName)
										.replace("{title}", it.stream.title)
										.replace("{url}", "https://twitch.tv/${it.channel.name}")
										.replace("{role}", "<@&$role>")
								)
							} else {
								channel?.createMessage("<@&$role> https://twitch.tv/${it.channel.name} went live streaming ${it.stream.gameName}: ${it.stream.title}")
							}
						} else {
							if (livemessage != null) {
								channel?.createMessage(
									livemessage
										.replace("{name}", it.channel.name)
										.replace("{category}", it.stream.gameName)
										.replace("{title}", it.stream.title)
										.replace("{url}", "https://twitch.tv/${it.channel.name}")
								)
							} else {
								channel?.createMessage("https://twitch.tv/${it.channel.name} went live streaming ${it.stream.gameName}: ${it.stream.title}")
							}
						}
					}
				}
			}
		}
	}



}
