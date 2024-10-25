package dev.jansel.feixiao.extensions

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.coalescingDefaultingString
import dev.kordex.core.commands.converters.impl.defaultingString
import dev.kordex.core.commands.converters.impl.user
import dev.kordex.core.components.components
import dev.kordex.core.components.publicButton
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.chatCommand
import dev.kordex.core.extensions.publicSlashCommand
import dev.kordex.core.utils.respond
import dev.jansel.feixiao.TEST_SERVER_ID

class TestExtension : Extension() {
	override val name = "test"

	override suspend fun setup() {
		chatCommand(TestExtension::SlapArgs) {
			name = "slap"
			description = "Ask the bot to slap another user"

			check { failIf(event.message.author == null) }

			action {
				// Don't slap ourselves on request, slap the requester!
				val realTarget = if (arguments.target.id == event.kord.selfId) {
					message.author!!
				} else {
					arguments.target
				}

				message.respond("*slaps ${realTarget.mention} with their ${arguments.weapon}*")
			}
		}

		chatCommand {
			name = "button"
			description = "A simple example command that sends a button."

			check { failIf(event.message.author == null) }

			action {
				message.respond {
					components {
						publicButton {
							label = "Button!"

							action {
								respond {
									content = "You pushed the button!"
								}
							}
						}
					}
				}
			}
		}

		publicSlashCommand(TestExtension::SlapSlashArgs) {
			name = "slap"
			description = "Ask the bot to slap another user"

			guild(TEST_SERVER_ID)  // Otherwise it will take up to an hour to update

			action {
				// Don't slap ourselves on request, slap the requester!
				val realTarget = if (arguments.target.id == event.kord.selfId) {
					member
				} else {
					arguments.target
				}

				respond {
					content = "*slaps ${realTarget?.mention} with their ${arguments.weapon}*"
				}
			}
		}

		publicSlashCommand {
			name = "button"
			description = "A simple example command that sends a button."

			action {
				respond {
					components {
						publicButton {
							label = "Button!"

							action {
								respond {
									content = "You pushed the button!"
								}
							}
						}
					}
				}
			}
		}
	}

	inner class SlapArgs : Arguments() {
		val target by user {
			name = "target"
			description = "Person you want to slap"
		}

		val weapon by coalescingDefaultingString {
			name = "weapon"

			defaultValue = "large, smelly trout"
			description = "What you want to slap with"
		}
	}

	inner class SlapSlashArgs : Arguments() {
		val target by user {
			name = "target"
			description = "Person you want to slap"
		}

		// Slash commands don't support coalescing strings
		val weapon by defaultingString {
			name = "weapon"

			defaultValue = "large, smelly trout"
			description = "What you want to slap with"
		}
	}
}
