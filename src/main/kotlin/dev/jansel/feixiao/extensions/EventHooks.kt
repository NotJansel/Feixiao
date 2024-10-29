package dev.jansel.feixiao.extensions

import com.github.twitch4j.TwitchClientBuilder
import com.github.twitch4j.events.ChannelGoLiveEvent
import com.github.twitch4j.events.ChannelGoOfflineEvent
import dev.jansel.feixiao.utils.tchannelid
import dev.jansel.feixiao.utils.tserverid
import dev.jansel.feixiao.utils.twitchcid
import dev.jansel.feixiao.utils.twitchcs
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.gateway.ReadyEvent
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.event
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class EventHooks : Extension() {
	override val name = "eventhooks"

	override suspend fun setup() {
		event<ReadyEvent> {
			action {
				println("Bot is ready!")
				kord.editPresence { playing("osu!") }
				val twitchClient = TwitchClientBuilder.builder()
					.withEnableHelix(true)
					.withClientId(twitchcid)
					.withClientSecret(twitchcs)
					.build()
				twitchClient.clientHelper.enableStreamEventListener("janselosu")
				// Register a listener for when the channel goes live
				twitchClient.eventManager.onEvent(ChannelGoLiveEvent::class.java) {
					runBlocking {
						launch {
							val twitchpingschannel =
								kord.getGuildOrNull(tserverid)?.getChannelOf<GuildMessageChannel>(tchannelid)
							twitchpingschannel?.createMessage("<@&1130981452130037800> ${it.channel.name} is now live at https://twitch.tv/${it.channel.name} streaming ${it.stream.gameName}: ${it.stream.title}")
							kord.editPresence { streaming(it.stream.title, "https://twitch.tv/${it.channel.name}") }
						}
					}
				}
				twitchClient.eventManager.onEvent(ChannelGoOfflineEvent::class.java) {
					runBlocking {
						launch {
							kord.editPresence { playing("osu!") }
						}
					}
				}
			}
		}
	}
}
