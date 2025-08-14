package dev.jansel.feixiao.utils

import com.github.philippheuer.events4j.reactor.ReactorEventHandler
import com.github.twitch4j.TwitchClientBuilder
import com.github.twitch4j.events.ChannelGoLiveEvent
import dev.jansel.feixiao.botRef
import dev.jansel.feixiao.database.collections.StreamerCollection
import dev.jansel.feixiao.twitchClient
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kordex.core.koin.KordExKoinComponent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Twitch : KordExKoinComponent {
	/**
	 * Initialize the Twitch client and register the on-live event handler.
	 * The handler uses coroutines without blocking the event thread and performs null-safe operations.
	 */
	suspend fun init() {
		twitchClient = TwitchClientBuilder.builder()
			.withEnableHelix(true)
			.withDefaultEventHandler(ReactorEventHandler::class.java)
			.withClientId(twitchcid)
			.withClientSecret(twitchcs)
			.build()

		// Register a non-blocking event handler for stream go-live events
		twitchClient?.eventManager?.onEvent(ChannelGoLiveEvent::class.java) { event ->
			dev.jansel.feixiao.logger.info { "${event.channel.name} went live!" }
			kotlinx.coroutines.GlobalScope.launch {
				val streamer = StreamerCollection().getData(event.channel.name)
				if (streamer == null) {
					dev.jansel.feixiao.logger.warn { "No StreamerData found for ${event.channel.name}" }
					return@launch
				}
				val game = event.stream?.gameName ?: "Unknown"
				val title = event.stream?.title ?: ""
				val url = "https://twitch.tv/${event.channel.name}"
				for (server in streamer.servers) {
					val channel = botRef?.kordRef?.getChannelOf<GuildMessageChannel>(server.channelId)
					val role = server.roleId
					val liveMessage = server.liveMessage

					val message = if (liveMessage != null) {
						formatLiveMessage(liveMessage, event.channel.name, game, title, url, role)
					} else if (role != null) {
						"<@&${role.value}> $url went live streaming $game: $title"
					} else {
						"$url went live streaming $game: $title"
					}
					channel?.createMessage(message)
				}
			}
		}
	}

	/**
	 * Small template formatter for live messages. Supported tokens:
	 * {name}, {category}, {title}, {url}, {role}
	 */
	private fun formatLiveMessage(template: String, name: String, category: String, title: String, url: String, role: dev.kord.common.entity.Snowflake?): String {
		return template
			.replace("{name}", name)
			.replace("{category}", category)
			.replace("{title}", title)
			.replace("{url}", url)
			.replace("{role}", role?.let { "<@&${it.value}>" } ?: "")
	}
}
