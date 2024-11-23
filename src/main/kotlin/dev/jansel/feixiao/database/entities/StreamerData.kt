package dev.jansel.feixiao.database.entities

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
data class StreamerData(
	val name: String,
	val servers: List<Server>
)

@Serializable
data class Server(
	val guildId: Snowflake,
	val channelId: Snowflake,
	val roleId: Snowflake?,
	val liveMessage: String?
)
