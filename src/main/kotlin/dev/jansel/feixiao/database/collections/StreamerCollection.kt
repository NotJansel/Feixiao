package dev.jansel.feixiao.database.collections

import dev.jansel.feixiao.database.Database
import dev.jansel.feixiao.database.entities.Server
import dev.jansel.feixiao.database.entities.StreamerData
import dev.kord.common.entity.Snowflake
import dev.kordex.core.koin.KordExKoinComponent
import org.koin.core.component.inject
import org.litote.kmongo.elemMatch
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

class StreamerCollection : KordExKoinComponent {
	private val db:
		Database by inject()

	@PublishedApi
	internal val collection =
		db.mongo.getCollection<StreamerData>()

	suspend fun getData(channelName: String): StreamerData? =
		collection.findOne(StreamerData::name eq channelName)

	suspend fun addData(
		guildId: Snowflake,
		channelId: Snowflake,
		streamerName: String,
		roleId: Snowflake?,
		liveMessage: String?
	) {
		val coll = collection.findOne(StreamerData::name eq streamerName)
		if (coll != null) {
			collection.updateOne(
				StreamerData::name eq streamerName,
				setValue(StreamerData::servers, coll.servers + listOf(Server(guildId, channelId, roleId, liveMessage)))
			)
		} else {
			collection.insertOne(
				StreamerData(streamerName, listOf(Server(guildId, channelId, roleId, liveMessage)))
			)
		}
	}

	/**
	 * Update the roleId
	 * @param streamerName: The name of the streamer
	 * @param roleId: The roleId to update
	 * @param guildId: The guildId to update
	 * @param noOverload: This is needed to avoid a conflict with the other updateData function, set to true or false, doesn't matter
	 * @return 0 = success, 1 = no Server associated with the guildId, 2 = no StreamerData associated with the streamerName
	 */

	suspend fun updateData(
		streamerName: String,
		roleId: Snowflake,
		guildId: Snowflake,
		noOverload : Boolean = false // this is needed to avoid a conflict with the other updateData function
	): Int {
		val coll = collection.findOne(StreamerData::name eq streamerName)
		if (coll != null) {
			val temp = coll.servers.find { server -> server.guildId == guildId }
			if (temp == null) return 1
			collection.updateMany(
				StreamerData::name eq streamerName,
				setValue(StreamerData::servers, coll.servers - temp + Server(guildId, temp.channelId, roleId, temp.liveMessage))
			)
			return 0
		}
		return 2
	}

	/**
	 * Update the liveMessage
	 * @param streamerName: The name of the streamer
	 * @param liveMessage: The liveMessage to update
	 * @param guildId: The guildId to update
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
				setValue(StreamerData::servers, coll.servers - temp + Server(guildId, temp.channelId, temp.roleId, liveMessage))
			)
			return 0
		}
		return 2
	}

	/**
	 * Update the channelId
	 * @param streamerName: The name of the streamer
	 * @param channelId: The channelId to update
	 * @param guildId: The guildId to update
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
				setValue(StreamerData::servers, coll.servers - temp + Server(guildId, channelId, temp.roleId, temp.liveMessage))
			)
			return 0
		}
		return 2
	}

	suspend fun removeData(
		guildId: Snowflake,
		channelId: Snowflake,
		streamerName: String,
		roleId: Snowflake?,
		liveMessage: String?
	) {
		val coll = collection.findOne(StreamerData::name eq streamerName)
		if (coll != null) {
			collection.updateOne(
				StreamerData::name eq streamerName,
				setValue(StreamerData::servers, coll.servers - Server(guildId, channelId, roleId, liveMessage))
			)
		}
	}
}
