package dev.jansel.feixiao.database.collections

import dev.jansel.feixiao.database.Database
import dev.jansel.feixiao.database.entities.MetaData
import dev.kordex.core.koin.KordExKoinComponent
import org.koin.core.component.inject
import org.litote.kmongo.eq

class MetaCollection : KordExKoinComponent {
	private val db: Database by inject()

	@PublishedApi
	internal val collection = db.mongo.getCollection<MetaData>()

	suspend fun get(): MetaData? =
		collection.findOne()

	suspend fun set(meta: MetaData) =
		collection.insertOne(meta)

	suspend fun update(meta: MetaData) =
		collection.findOneAndReplace(
			MetaData::id eq "meta",
			meta
		)
}
