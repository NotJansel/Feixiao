package dev.jansel.feixiao

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.github.twitch4j.TwitchClient
import dev.jansel.feixiao.extensions.EventHooks
import dev.jansel.feixiao.extensions.StreamerCommand
import dev.jansel.feixiao.utils.database
import dev.jansel.feixiao.utils.token
import dev.jansel.feixiao.utils.twitch
import dev.jansel.feixiao.utils.twitchcid
import dev.jansel.feixiao.utils.twitchcs
import dev.jansel.feixiao.utils.mongoUri
import dev.kordex.core.ExtensibleBot
import dev.kordex.core.i18n.SupportedLocales
import io.github.oshai.kotlinlogging.KotlinLogging
import org.slf4j.LoggerFactory

var twitchClient: TwitchClient? = null
val logger = KotlinLogging.logger { }
var botRef: ExtensibleBot? = null

/**
 * Application entry point: configures logging, validates required environment variables,
 * starts the bot, and registers a shutdown hook to cleanly close resources.
 */
suspend fun main() {
	val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
	val rootLogger = loggerContext.getLogger("org.mongodb.driver")
	rootLogger.level = Level.OFF

	// Fail-fast environment validation
	val missing = mutableListOf<String>()
	if (token.isBlank()) missing += "TOKEN"
	if (twitchcid.isBlank()) missing += "TWITCH_CLIENT_ID"
	if (twitchcs.isBlank()) missing += "TWITCH_CLIENT_SECRET"
	if (mongoUri.isBlank()) missing += "MONGO_URI"
	if (missing.isNotEmpty()) {
		logger.error { "Missing required environment variables: ${missing.joinToString(", ")}. Aborting startup." }
		return
	}

	// Register shutdown hook for graceful shutdown
	Runtime.getRuntime().addShutdownHook(Thread {
		try {
			logger.info { "Shutting down: closing Twitch client" }
			twitchClient?.close()
		} catch (t: Throwable) {
			logger.error(t) { "Error while closing Twitch client" }
		}
	})

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

	botRef?.start()
}


