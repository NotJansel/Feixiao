package dev.jansel.feixiao.database

import com.github.philippheuer.events4j.reactor.ReactorEventHandler
import com.github.twitch4j.TwitchClientBuilder
import dev.jansel.feixiao.database.collections.MetaCollection
import dev.jansel.feixiao.database.entities.MetaData
import dev.jansel.feixiao.database.migrations.v1
import dev.jansel.feixiao.database.migrations.v2
import dev.jansel.feixiao.twitchClient
import dev.jansel.feixiao.utils.twitchcid
import dev.jansel.feixiao.utils.twitchcs
import dev.kordex.core.koin.KordExKoinComponent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.component.inject

object Migrator : KordExKoinComponent {
	private val logger = KotlinLogging.logger("Migrator Logger")

	private val db: Database by inject()
	private val mainMetaCollection: MetaCollection by inject()

	suspend fun migrate() {
		logger.info { "Starting main database migration" }

		var meta = mainMetaCollection.get()

		if (meta == null) {
			meta = MetaData(0)

			mainMetaCollection.set(meta)
		}

		var currentVersion = meta.version

		logger.info { "Current main database version: v$currentVersion" }

		while (true) {
			val nextVersion = currentVersion + 1

			@Suppress("TooGenericExceptionCaught", "UseIfInsteadOfWhen")
			try {
				when (nextVersion) {
					1 -> v1(db.mongo)
					2 -> {
						var createdForMigration = false
						if (twitchClient == null) {
							logger.info { "Initializing Twitch client for migration v2 (Helix required)" }
							twitchClient = TwitchClientBuilder.builder()
								.withEnableHelix(true)
								.withDefaultEventHandler(ReactorEventHandler::class.java)
								.withClientId(twitchcid)
								.withClientSecret(twitchcs)
								.build()
							createdForMigration = true
						}
						v2(db.mongo)
						if (createdForMigration) {
							try {
								twitchClient?.close()
							} catch (_: Throwable) {
								// ignore
							} finally {
								twitchClient = null
							}
						}
					}
					else -> break
				}

				logger.info { "Migrated database to version $nextVersion." }
			} catch (t: Throwable) {
				logger.error(t) { "Failed to migrate database to version $nextVersion." }

				throw t
			}

			currentVersion = nextVersion
		}

		if (currentVersion != meta.version) {
			meta = meta.copy(version = currentVersion)

			mainMetaCollection.update(meta)

			logger.info { "Finished main database migrations." }
		}
	}
}
