package dev.jansel.feixiao.utils

import dev.jansel.feixiao.database.Database
import dev.jansel.feixiao.database.collections.MetaCollection
import dev.jansel.feixiao.database.collections.StreamerCollection
import dev.kord.common.entity.Snowflake
import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.utils.env
import dev.kordex.core.utils.loadModule
import kotlinx.coroutines.runBlocking
import org.koin.dsl.bind

val twitchcid = env("TWITCH_CLIENT_ID")
val twitchcs = env("TWITCH_CLIENT_SECRET")
val token = env("TOKEN")
val tserverid = Snowflake(env("TEST_SERVER").toLong())
val tchannelid = Snowflake(env("TEST_CHANNEL").toLong())
val mongoUri = env("MONGO_URI")

suspend inline fun ExtensibleBotBuilder.database(migrate: Boolean) {
	val db = Database()

	hooks {
		beforeKoinSetup {
			loadModule {
				single { db } bind Database::class
			}

			loadModule {
				single { MetaCollection() } bind MetaCollection::class
				single { StreamerCollection() } bind StreamerCollection::class
			}

			if (migrate) {
				runBlocking { db.migrate() }
			}
		}
	}
}

suspend inline fun ExtensibleBotBuilder.twitch(active: Boolean) {
	hooks {
		beforeKoinSetup {
			loadModule {
				single { Twitch() } bind Twitch::class
			}

			if (active) {
				Twitch().init()
			}
		}
	}

}
