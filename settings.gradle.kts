pluginManagement {
	plugins {
		// Update this in libs.version.toml when you change it here.
		kotlin("jvm") version "2.0.21"
		kotlin("plugin.serialization") version "2.0.21"

		id("com.github.johnrengelman.shadow") version "8.1.1"

		id("dev.kordex.gradle.docker") version "1.5.8"
		id("dev.kordex.gradle.kordex") version "1.5.8"
	}
	repositories {
		gradlePluginPortal()
		mavenCentral()

		maven("https://snapshots-repo.kordex.dev")
		maven("https://releases-repo.kordex.dev")
	}
}

rootProject.name = "feixiao"
