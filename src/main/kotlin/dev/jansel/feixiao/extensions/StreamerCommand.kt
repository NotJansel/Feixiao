package dev.jansel.feixiao.extensions

import dev.jansel.feixiao.database.collections.StreamerCollection
import dev.jansel.feixiao.twitchClient
import dev.kord.common.entity.Permission
import dev.kordex.core.checks.anyGuild
import dev.kordex.core.checks.hasPermission
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.publicSubCommand
import dev.kordex.core.commands.converters.impl.snowflake
import dev.kordex.core.commands.converters.impl.string
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.publicSlashCommand

class StreamerCommand : Extension() {
	override val name = "streamer"
	override suspend fun setup() {
		publicSlashCommand {
			name = "streamer"
			description = "Streamer commands"

			publicSubCommand(::StreamerArgs) {
				name = "add"
				description = "Add a streamer to the listener"
				check {
					anyGuild()
					hasPermission(Permission.ManageGuild)
				}
				action {
					val streamer = arguments.streamer
					StreamerCollection().updateData(guild!!.id, channel.id, streamer, arguments.role)
					twitchClient!!.clientHelper.enableStreamEventListener(streamer)
					respond {
						content = "Added streamer $streamer"
					}
				}
			}
		}
	}

	inner class StreamerArgs : Arguments() {
		val streamer by string {
			name = "streamer"
			description = "The streamer to add"
			require(true)
		}
		val channel by snowflake {
			name = "announceChannel"
			description = "Channel where the bot will send a message when the streamer goes live"
			require(true)
		}
		val role by snowflake {
			name = "role"
			description = "Role to ping when the streamer goes live"
			require(false)
		}
	}

}
