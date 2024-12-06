package dev.jansel.feixiao.extensions

import dev.jansel.feixiao.database.collections.StreamerCollection
import dev.jansel.feixiao.database.entities.StreamerData
import dev.jansel.feixiao.i18n.Translations
import dev.jansel.feixiao.twitchClient
import dev.kord.common.entity.Permission
import dev.kordex.core.checks.anyGuild
import dev.kordex.core.checks.hasPermission
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.publicSubCommand
import dev.kordex.core.commands.converters.impl.*
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.publicSlashCommand
import org.litote.kmongo.eq

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
					StreamerCollection().getData(streamer)?.servers?.forEach {
						if (it.guildId == guild!!.id) {
							respond {
								content = "Streamer already exists in this server"
							}
							return@action
						}
					}
					if (arguments.role?.id == guild!!.id) {
						respond {
							content = "This action would implement a everyone ping. to properly use it please make a ping without role and insert the everyone ping manually."
						}
						return@action
					}
					StreamerCollection().addData(
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
					StreamerCollection().collection.findOne(StreamerData::name eq streamer)?.servers?.forEach {
						StreamerCollection().removeData(it.guildId, it.channelId, streamer, it.roleId, it.liveMessage)
					}
					respond {
						content = "Removed streamer $streamer"
					}
				}
			}

			publicSubCommand(::UpdateStreamerArgs) {
				name = Translations.Streamer.Command.Update.name
				description = Translations.Streamer.Command.Update.description
				check {
					anyGuild()
					hasPermission(Permission.ManageGuild)
				}
				action {
					val streamer = arguments.streamer
					val data = StreamerCollection().collection.findOne(StreamerData::name eq streamer)
					if (data != null) {
						val servers = data.servers
						val guildId = guild!!.id
						val roleId = arguments.role
						val channelId = arguments.channel
						val message = arguments.message
						val temp = servers.find { it.guildId == guildId }
						if (roleId?.id== guildId) {
							respond {
								content = "This action would implement a everyone ping. to properly use it please make a ping without role and insert the everyone ping manually."
							}
							return@action
						}
						if (temp != null) {
							if (channelId != null) {
								StreamerCollection().updateData(streamer, channelId.id, guildId)
							}
							if (roleId != null) {
								StreamerCollection().updateData(streamer, roleId.id, guildId, false)
							}
							if (message != null) {
								StreamerCollection().updateData(streamer, message, guildId)
							}
							respond {
								content = "Updated streamer $streamer"
							}
						} else {
							respond {
								content = "No server associated with the guildId"
							}
						}
					} else {
						respond {
							content = "No StreamerData associated with the streamerName"
						}
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

	inner class UpdateStreamerArgs : Arguments() {
		val streamer by string {
			name = Translations.Streamer.Command.Arguments.Update.Streamer.name
			description = Translations.Streamer.Command.Arguments.Update.Streamer.description
		}
		val channel by optionalChannel {
			name = Translations.Streamer.Command.Arguments.Update.Channel.name
			description = Translations.Streamer.Command.Arguments.Update.Channel.description
		}
		val role by optionalRole {
			name = Translations.Streamer.Command.Arguments.Update.Role.name
			description = Translations.Streamer.Command.Arguments.Update.Role.description
		}
		val message by optionalString {
			name = Translations.Streamer.Command.Arguments.Update.Message.name
			description = Translations.Streamer.Command.Arguments.Update.Message.description
		}
	}
}
