package dev.jansel.feixiao.extensions

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.event.message.MessageCreateEvent
import dev.kordex.core.annotations.DoNotChain
import dev.kordex.core.checks.anyGuild
import dev.kordex.core.checks.inChannel
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
				inChannel(Snowflake(1130942639764676709))
			}
			action {
				println("Message detected!")
				val calendar = Calendar.getInstance()
				calendar.time = Date()
				val hour = calendar.get(Calendar.HOUR_OF_DAY)
				val day = calendar.get(Calendar.DAY_OF_WEEK)
				if ((day == Calendar.MONDAY && hour < 5) || (day == Calendar.MONDAY && hour >= 21) ||
					(day == Calendar.TUESDAY && hour < 5) || (day == Calendar.TUESDAY && hour >= 21) ||
					(day == Calendar.WEDNESDAY && hour < 5) || (day == Calendar.WEDNESDAY && hour >= 21) ||
					(day == Calendar.THURSDAY && hour < 5) || (day == Calendar.THURSDAY && hour >= 21) ||
					(day == Calendar.FRIDAY && hour < 5) || (day == Calendar.SUNDAY && hour >= 21)
				) {
					event.message.delete()
					event.member!!.timeout(7.days, "ES HERRSCHT RUHEZEIT!")
				}
			}
		}
	}
}
