package dev.jansel.feixiao.database.entities

import kotlinx.serialization.Serializable

@Serializable
data class MetaData(
	val version: Int,
	val id: String = "meta"
)
