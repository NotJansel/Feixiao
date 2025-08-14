package dev.jansel.feixiao.database.collections

import dev.jansel.feixiao.database.Database
import dev.jansel.feixiao.database.entities.Server
import dev.jansel.feixiao.database.entities.StreamerData
import dev.jansel.feixiao.twitchClient
import dev.jansel.feixiao.utils.getTwitchIdByName
import dev.kord.common.entity.Snowflake
import dev.kordex.core.koin.KordExKoinComponent
import org.koin.core.component.inject
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

/**
 * Repository wrapper around the StreamerData collection.
 * Note: This still exposes the underlying collection for existing call sites; a full encapsulation
 * would require broader refactoring and is out of scope for this incremental improvement.
 */
class StreamerCollection : KordExKoinComponent {
	private val db: Database by inject()

	@PublishedApi
	internal val collection = db.mongo.getCollection<StreamerData>()

	/**
	 * Fetch StreamerData by channel/display name.
	 */
	suspend fun getData(channelName: String): StreamerData? =
		collection.findOne(StreamerData::name eq channelName)

	/**
	 * Add a subscription (guild/channel/role/message) for a streamer. Enables the Twitch listener when
	 * the first subscription is added.
	 */
	suspend fun addData(
		guildId: Snowflake,
		channelId: Snowflake,
		streamerName: String,
		roleId: Snowflake?,
		liveMessage: String?
	) {
		val coll = collection.findOne(StreamerData::name eq streamerName)
		if (coll != null) {
			val previousCount = coll.servers.size
			val newServers = coll.servers + listOf(Server(guildId, channelId, roleId, liveMessage))
			collection.updateOne(
				StreamerData::name eq streamerName,
				setValue(StreamerData::servers, newServers)
			)
			if (previousCount == 0 && newServers.isNotEmpty()) {
				// Enable Twitch listener when the first subscription is added
				twitchClient?.clientHelper?.enableStreamEventListener(streamerName)
			}
		} else {
			collection.insertOne(
				StreamerData(streamerName, getTwitchIdByName(streamerName), listOf(Server(guildId, channelId, roleId, liveMessage)))
			)
			// First ever subscription for this streamer, enable listener
			twitchClient?.clientHelper?.enableStreamEventListener(streamerName)
		}
	}

	/**
	 * Update the roleId for a guild subscription.
	 * @return 0 = success, 1 = no Server associated with the guildId, 2 = no StreamerData associated with the streamerName
	 */
	suspend fun updateData(
		streamerName: String,
		roleId: Snowflake,
		guildId: Snowflake,
		noOverload: Boolean = false // this is needed to avoid a conflict with the other updateData function
	): Int {
		val coll = collection.findOne(StreamerData::name eq streamerName)
		if (coll != null) {
			val temp = coll.servers.find { server -> server.guildId == guildId }
			if (temp == null) return 1
			collection.updateMany(
				StreamerData::name eq streamerName,
				setValue(
					StreamerData::servers,
					coll.servers - temp + Server(guildId, temp.channelId, roleId, temp.liveMessage)
				)
			)
			return 0
		}
		return 2
	}

	/**
	 * Update the liveMessage for a guild subscription.
	 * @return 0 = success, 1 = no Server associated with the guildId, 2 = no StreamerData associated with the streamerName
	 */
	suspend fun updateData(
		streamerName: String,
		liveMessage: String?,
		guildId: Snowflake
	): Int {
		val coll = collection.findOne(StreamerData::name eq streamerName)
		if (coll != null) {
			val temp = coll.servers.find { server -> server.guildId == guildId }
			if (temp == null) return 1
			collection.updateMany(
				StreamerData::name eq streamerName,
				setValue(
					StreamerData::servers,
					coll.servers - temp + Server(guildId, temp.channelId, temp.roleId, liveMessage)
				)
			)
			return 0
		}
		return 2
	}

	/**
	 * Update the channelId for a guild subscription.
	 * @return 0 = success, 1 = no Server associated with the guildId, 2 = no StreamerData associated with the streamerName
	 */
	suspend fun updateData(
		streamerName: String,
		channelId: Snowflake,
		guildId: Snowflake
	): Int {
		val coll = collection.findOne(StreamerData::name eq streamerName)
		if (coll != null) {
			val temp = coll.servers.find { server -> server.guildId == guildId }
			if (temp == null) return 1
			collection.updateMany(
				StreamerData::name eq streamerName,
				setValue(
					StreamerData::servers,
					coll.servers - temp + Server(guildId, channelId, temp.roleId, temp.liveMessage)
				)
			)
			return 0
		}
		return 2
	}

	/**
	 * Remove a subscription. If this was the last subscription, delete the document and disable the listener.
	 */
	suspend fun removeData(
		guildId: Snowflake,
		channelId: Snowflake,
		streamerName: String,
		roleId: Snowflake?,
		liveMessage: String?
	) {
		val coll = collection.findOne(StreamerData::name eq streamerName)
		if (coll != null) {
			val newServers = coll.servers - Server(guildId, channelId, roleId, liveMessage)
			if (newServers.isEmpty()) {
				collection.deleteOne(StreamerData::name eq streamerName)
				// Disable Twitch listener when no subscribers remain
				twitchClient?.clientHelper?.disableStreamEventListener(streamerName)
			} else {
				collection.updateOne(
					StreamerData::name eq streamerName,
					setValue(StreamerData::servers, newServers)
				)
			}
		}
	}
}
