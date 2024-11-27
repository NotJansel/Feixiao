package dev.jansel.feixiao.extensions

import dev.jansel.feixiao.database.collections.StreamerCollection
import dev.jansel.feixiao.i18n.Translations
import dev.jansel.feixiao.twitchClient
import dev.kord.common.entity.Permission
import dev.kordex.core.checks.anyGuild
import dev.kordex.core.checks.hasPermission
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.publicSubCommand
import dev.kordex.core.commands.converters.impl.channel
import dev.kordex.core.commands.converters.impl.optionalRole
import dev.kordex.core.commands.converters.impl.optionalString
import dev.kordex.core.commands.converters.impl.string
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.publicSlashCommand

class StreamerCommand : Extension() {
	override val name = "streaming"
	override suspend fun setup() {
		publicSlashCommand {
			name = Translations.Streamer.Command.name
			description = Translations.Streamer.Command.description

			publicSubCommand(::AddStreamerArgs) {
				name = Translations.Streamer.Command.Add.name
				description = Translations.Streamer.Command.Add.description
				check {
					anyGuild()
					hasPermission(Permission.ManageGuild)
				}
				action {
					val streamer = arguments.streamer
					StreamerCollection().updateData(
						guild!!.id,
						arguments.channel.id,
						streamer,
						arguments.role?.id,
						arguments.message
					)
					twitchClient!!.clientHelper.enableStreamEventListener(streamer)
					respond {
						content = "Added streamer $streamer"
					}
				}
			}

			publicSubCommand(::RemoveStreamerArgs) {
				name = Translations.Streamer.Command.Remove.name
				description = Translations.Streamer.Command.Remove.description
				check {
					anyGuild()
					hasPermission(Permission.ManageGuild)
				}
				action {
					val streamer = arguments.streamer
					StreamerCollection().removeData(guild!!.id, channel.id, streamer, null, null)
					respond {
						content = "Removed streamer $streamer"
					}
				}
			}
		}
	}

	inner class AddStreamerArgs : Arguments() {
		val streamer by string {
			name = Translations.Streamer.Command.Arguments.Add.Streamer.name
			description = Translations.Streamer.Command.Arguments.Add.Streamer.description
			require(true)
		}
		val channel by channel {
			name = Translations.Streamer.Command.Arguments.Add.Channel.name
			description = Translations.Streamer.Command.Arguments.Add.Channel.description
			require(true)
		}
		val role by optionalRole {
			name = Translations.Streamer.Command.Arguments.Add.Role.name
			description = Translations.Streamer.Command.Arguments.Add.Role.description
		}
		val message by optionalString {
			name = Translations.Streamer.Command.Arguments.Add.Message.name
			description = Translations.Streamer.Command.Arguments.Add.Message.description
		}
	}

	inner class RemoveStreamerArgs : Arguments() {
		val streamer by string {
			name = Translations.Streamer.Command.Arguments.Remove.name
			description = Translations.Streamer.Command.Arguments.Remove.description
			require(true)
		}
	}
}
