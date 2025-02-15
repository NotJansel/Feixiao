package dev.jansel.feixiao.database.migrations

import dev.jansel.feixiao.database.entities.StreamerData
import dev.jansel.feixiao.utils.getTwitchIdByName
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

suspend fun v2(db: CoroutineDatabase) {
	db.getCollection<StreamerData>("streamerData").findOne(StreamerData::id eq null)?.let {
		db.getCollection<StreamerData>("streamerData").updateOne(
			StreamerData::name eq it.name,
			setValue(StreamerData::id, getTwitchIdByName(it.name))
		)
	}
}
