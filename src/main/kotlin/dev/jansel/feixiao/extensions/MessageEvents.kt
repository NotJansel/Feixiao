package dev.jansel.feixiao.extensions

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.event.message.MessageCreateEvent
import dev.kordex.core.annotations.DoNotChain
import dev.kordex.core.checks.anyGuild
import dev.kordex.core.checks.isNotBot
import dev.kordex.core.checks.notHasPermission
import dev.kordex.core.checks.notHasRole
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.event
import dev.kordex.core.utils.timeout
import java.util.*
import kotlin.time.Duration.Companion.days

class MessageEvents : Extension() {
	override val name = "messageevents"

	@OptIn(DoNotChain::class)
	override suspend fun setup() {
		event<MessageCreateEvent> {
			check {
				anyGuild()
				isNotBot()
				notHasPermission(Permission.ManageMessages)
				notHasRole(Snowflake(1130937397132152852))
			}
			action {
				val calendar = Calendar.getInstance()
				calendar.time = Date()
				val hour = calendar.get(Calendar.HOUR_OF_DAY)
				val day = calendar.get(Calendar.DAY_OF_WEEK)
				if ((day == Calendar.MONDAY && hour < 6) || (day == Calendar.MONDAY && hour >= 22) ||
					(day == Calendar.TUESDAY && hour < 6) || (day == Calendar.TUESDAY && hour >= 22) ||
					(day == Calendar.WEDNESDAY && hour < 6) || (day == Calendar.WEDNESDAY && hour >= 22) ||
					(day == Calendar.THURSDAY && hour < 6) || (day == Calendar.THURSDAY && hour >= 22) ||
					(day == Calendar.FRIDAY && hour < 6) || (day == Calendar.SUNDAY && hour >= 22)
				) {
					event.message.delete()
					event.member!!.timeout(7.days, "ES HERRSCHT RUHEZEIT!")
				}
			}
		}
	}
}
