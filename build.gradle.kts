import dev.kordex.gradle.plugins.docker.file.*
import dev.kordex.gradle.plugins.kordex.DataCollection

plugins {
	distribution

	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.serialization)

	alias(libs.plugins.kordex.plugin)
	alias(libs.plugins.kordex.docker)
}

group = "dev.jansel"
version = "1.2-SNAPSHOT"

dependencies {

	implementation(libs.kotlin.stdlib)
	implementation(libs.kx.ser)
	implementation(libs.kx.coroutines)
	implementation(libs.twitch4j)
	implementation(libs.events4j)
	implementation(libs.kmongo)

	// Logging dependencies
	implementation(libs.groovy)
	implementation(libs.jansi)
	implementation(libs.logback)
	implementation(libs.logback.groovy)
	implementation(libs.logging)
}

// Configure distributions plugin
distributions {
	main {
		distributionBaseName = project.name

		contents {
			// Copy the LICENSE file into the distribution
			from("LICENSE")

			// Exclude src/main/dist/README.md
			exclude("README.md")
		}
	}
}


kordEx {
	kordExVersion = "2.3.1-SNAPSHOT"
	jvmTarget = 21
	ignoreIncompatibleKotlinVersion = true

	bot {
		// See https://docs.kordex.dev/data-collection.html
		dataCollection(DataCollection.None)

		mainClass = "dev.jansel.feixiao.AppKt"
	}

	i18n {
		classPackage = "dev.jansel.feixiao.i18n"
		translationBundle = "feixiao.strings"
	}
}

// Automatically generate a Dockerfile. Set `generateOnBuild` to `false` if you'd prefer to manually run the
// `createDockerfile` task instead of having it run whenever you build.
docker {
	// Create the Dockerfile in the root folder.
	file(rootProject.file("Dockerfile"))

	commands {
		// Each function (aside from comment/emptyLine) corresponds to a Dockerfile instruction.
		// See: https://docs.docker.com/reference/dockerfile/

		from("openjdk:21-jdk-slim")

		emptyLine()

		comment("Create required directories")
		runShell("mkdir -p /bot/plugins")
		runShell("mkdir -p /bot/data")
		runShell("mkdir -p /dist/out")

		emptyLine()

		// Add volumes for locations that you need to persist. This is important!
		comment("Declare required volumes")
		volume("/bot/data")  // Storage for data files
		volume("/bot/plugins")  // Plugin ZIP/JAR location

		emptyLine()

		comment("Copy the distribution files into the container")
		copy("build/distributions/${project.name}-${project.version}.tar", "/dist")

		emptyLine()

		comment("Extract the distribution files, and prepare them for use")
		runShell("tar -xf /dist/${project.name}-${project.version}.tar -C /dist/out")

		if (file("src/main/dist/plugins").isDirectory) {
			runShell("mv /dist/out/${project.name}-${project.version}/plugins/* /bot/plugins")
		}

		runShell("chmod +x /dist/out/${project.name}-${project.version}/bin/$name")

		emptyLine()

		comment("Clean up unnecessary files")
		runShell("rm /dist/${project.name}-${project.version}.tar")

		emptyLine()

		comment("Set the correct working directory")
		workdir("/bot")

		emptyLine()

		comment("Run the distribution start script")
		entryPointExec("/dist/out/${project.name}-${project.version}/bin/$name")
	}
}
