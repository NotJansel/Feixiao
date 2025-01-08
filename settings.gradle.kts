pluginManagement {
	plugins {
		// Update this in libs.version.toml when you change it here.
		kotlin("jvm") version "2.1.0"
		kotlin("plugin.serialization") version "2.1.0"

		id("com.github.johnrengelman.shadow") version "8.1.1"

		id("dev.kordex.gradle.docker") version "1.6.0"
		id("dev.kordex.gradle.kordex") version "1.6.0"
	}
	repositories {
		gradlePluginPortal()
		mavenCentral()

		maven("https://releases-repo.kordex.dev")
		maven("https://snapshots-repo.kordex.dev")
	}
}

rootProject.name = "feixiao"
