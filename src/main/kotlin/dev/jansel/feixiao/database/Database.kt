package dev.jansel.feixiao.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import dev.jansel.feixiao.utils.mongoUri
import org.bson.UuidRepresentation
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

class Database {
	private val settings = MongoClientSettings
		.builder()
		.uuidRepresentation(UuidRepresentation.STANDARD)
		.applyConnectionString(ConnectionString(mongoUri))
		.build()

	private val client = KMongo.createClient(settings).coroutine

	val mongo get() = client.getDatabase("Feixiao")

	suspend fun migrate() {
		Migrator.migrate()
	}
}
