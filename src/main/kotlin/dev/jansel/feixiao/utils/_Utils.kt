package dev.jansel.feixiao.utils

import dev.jansel.feixiao.database.Database
import dev.jansel.feixiao.database.collections.MetaCollection
import dev.jansel.feixiao.database.collections.StreamerCollection
import dev.jansel.feixiao.logger
import dev.jansel.feixiao.twitchClient
import dev.kord.common.entity.Snowflake
import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.utils.env
import dev.kordex.core.utils.loadModule
import kotlinx.coroutines.runBlocking
import org.koin.dsl.bind
import java.util.concurrent.ConcurrentHashMap

val twitchcid = env("TWITCH_CLIENT_ID")
val twitchcs = env("TWITCH_CLIENT_SECRET")
val token = env("TOKEN")
val mongoUri = env("MONGO_URI")

// Optional test server/channel. If not provided or invalid, features depending on them will be skipped.
val tserverIdEnv: String? = System.getenv("TEST_SERVER")
val tchannelIdEnv: String? = System.getenv("TEST_CHANNEL")
val tserverid: Snowflake? = tserverIdEnv?.toLongOrNull()?.let { Snowflake(it) }
val tchannelid: Snowflake? = tchannelIdEnv?.toLongOrNull()?.let { Snowflake(it) }

// Simple TTL caches for Twitch id/name resolution to reduce Helix API calls
private data class CacheEntry<T>(val value: T, val expiresAt: Long)
private const val RESOLUTION_TTL_MS = 10 * 60 * 1000L // 10 minutes
private val idToNameCache = ConcurrentHashMap<String, CacheEntry<String>>()
private val nameToIdCache = ConcurrentHashMap<String, CacheEntry<String>>()

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

/**
 * Resolve Twitch display name by user id with basic retry/backoff and TTL caching.
 * Returns null if the client is not initialized or user not found.
 */
fun getTwitchNameById(id: String): String? {
	// Cache lookup
	idToNameCache[id]?.let { if (System.currentTimeMillis() < it.expiresAt) return it.value else idToNameCache.remove(id) }

	val client = twitchClient
	if (client == null) {
		logger.warn { "Twitch client is not initialized; cannot resolve name for id=$id" }
		return null
	}

	var attempt = 0
	var delayMs = 250L
	while (attempt < 3) {
		try {
			val resultList = client.helix?.getUsers(null, listOf(id), null)?.execute()
			val name = resultList?.users?.firstOrNull { it.id == id }?.displayName
			if (name != null) {
				idToNameCache[id] = CacheEntry(name, System.currentTimeMillis() + RESOLUTION_TTL_MS)
				nameToIdCache[name] = CacheEntry(id, System.currentTimeMillis() + RESOLUTION_TTL_MS)
				return name
			}
			return null
		} catch (e: Exception) {
			attempt++
			if (attempt >= 3) {
				logger.error(e) { "Failed to resolve Twitch name for id=$id after $attempt attempts" }
				break
			}
			try { Thread.sleep(delayMs) } catch (_: InterruptedException) {}
			delayMs *= 2
		}
	}
	return null
}

/**
 * Resolve Twitch user id by display name with basic retry/backoff and TTL caching.
 * Returns null if the client is not initialized or user not found.
 */
fun getTwitchIdByName(name: String): String? {
	// Cache lookup
	nameToIdCache[name]?.let { if (System.currentTimeMillis() < it.expiresAt) return it.value else nameToIdCache.remove(name) }

	val client = twitchClient
	if (client == null) {
		logger.warn { "Twitch client is not initialized; cannot resolve id for name=$name" }
		return null
	}

	var attempt = 0
	var delayMs = 250L
	while (attempt < 3) {
		try {
			val resultList = client.helix?.getUsers(null, null, listOf(name))?.execute()
			val id = resultList?.users?.firstOrNull { it.displayName == name }?.id
			if (id != null) {
				nameToIdCache[name] = CacheEntry(id, System.currentTimeMillis() + RESOLUTION_TTL_MS)
				idToNameCache[id] = CacheEntry(name, System.currentTimeMillis() + RESOLUTION_TTL_MS)
				return id
			}
			return null
		} catch (e: Exception) {
			attempt++
			if (attempt >= 3) {
				logger.error(e) { "Failed to resolve Twitch id for name=$name after $attempt attempts" }
				break
			}
			try { Thread.sleep(delayMs) } catch (_: InterruptedException) {}
			delayMs *= 2
		}
	}
	return null
}
