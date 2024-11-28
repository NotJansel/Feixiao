package dev.jansel.feixiao.database.collections

import dev.jansel.feixiao.database.Database
import dev.jansel.feixiao.database.entities.Server
import dev.jansel.feixiao.database.entities.StreamerData
import dev.kord.common.entity.Snowflake
import dev.kordex.core.koin.KordExKoinComponent
import org.koin.core.component.inject
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

	suspend fun updateData(
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
