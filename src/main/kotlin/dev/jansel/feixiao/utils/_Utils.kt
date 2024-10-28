package dev.jansel.feixiao.utils

import dev.kord.common.entity.Snowflake
import dev.kordex.core.utils.env

val twitchcid = env("TWITCH_CLIENT_ID")
val twitchcs = env("TWITCH_CLIENT_SECRET")
val token = env("TOKEN")
val tserverid = Snowflake(env("TEST_SERVER").toLong())
val tchannelid = Snowflake(env("TEST_CHANNEL").toLong())
