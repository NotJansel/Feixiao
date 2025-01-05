package dev.jansel.feixiao.modals

import dev.jansel.feixiao.i18n.Translations
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.i18n.types.Key

class EditStreamerModal : ModalForm() {
	override var title = Translations.Streamer.Modals.Edit.title

	val streamerName = lineText {
		label = Translations.Streamer.Modals.Edit.Streamername.label
		placeholder = Translations.Streamer.Modals.Edit.Streamername.placeholder
	}

	val channel = lineText {
		label = Translations.Streamer.Modals.Edit.Channel.label
		placeholder = Translations.Streamer.Modals.Edit.Channel.placeholder
	}

	val role = lineText {
		label = Translations.Streamer.Modals.Edit.Role.label
		placeholder = Translations.Streamer.Modals.Edit.Role.placeholder
	}

	val message = paragraphText {
		label = Translations.Streamer.Modals.Edit.Message.label
		placeholder = Translations.Streamer.Modals.Edit.Message.placeholder
	}
}
