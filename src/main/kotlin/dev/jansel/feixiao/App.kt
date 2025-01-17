package dev.jansel.feixiao

import com.github.twitch4j.TwitchClient
import dev.jansel.feixiao.extensions.EventHooks
import dev.jansel.feixiao.extensions.StreamerCommand
import dev.jansel.feixiao.utils.database
import dev.jansel.feixiao.utils.token
import dev.jansel.feixiao.utils.twitch
import dev.kordex.core.ExtensibleBot
import dev.kordex.core.i18n.SupportedLocales
import io.github.oshai.kotlinlogging.KotlinLogging

var twitchClient: TwitchClient? = null
val logger = KotlinLogging.logger { }
var botRef: ExtensibleBot? = null

suspend fun main() {
	botRef = ExtensibleBot(token) {
		database(true)
		twitch(true)
		extensions {
			add(::EventHooks)
			add(::StreamerCommand)
		}
		i18n {
			applicationCommandLocale(SupportedLocales.ENGLISH, SupportedLocales.GERMAN)
		}
	}

	botRef!!.start()
}


