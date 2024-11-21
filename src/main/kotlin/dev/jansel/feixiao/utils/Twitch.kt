package dev.jansel.feixiao.utils

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
			.withClientId(twitchcid)
			.withClientSecret(twitchcs)
			.build()

		twitchClient!!.eventManager.onEvent(ChannelGoLiveEvent::class.java) {
			dev.jansel.feixiao.logger.info { "${it.channel.name} went live!" }
			runBlocking {
				launch {
					val streamer = StreamerCollection().getData(it.channel.name)
					val channel = botRef!!.kordRef.getChannelOf<GuildMessageChannel>(streamer!!.servers.first().channelId)
					val role = streamer.servers.first().roleId
					if (role != null) {
						channel?.createMessage("<@&$role> https://twitch.tv/${it.channel.name} went live streaming ${it.stream.gameName}: ${it.stream.title}")
					} else {
						channel?.createMessage("${it.channel.name} went live: ${it.stream.title}")
					}
				}
			}
		}
	}



}
