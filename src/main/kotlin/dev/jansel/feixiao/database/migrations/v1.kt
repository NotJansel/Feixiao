package dev.jansel.feixiao.database.migrations

import org.litote.kmongo.coroutine.CoroutineDatabase

suspend fun v1(db: CoroutineDatabase) {
	db.createCollection("meta")
}
