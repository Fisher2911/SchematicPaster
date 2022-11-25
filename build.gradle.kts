plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.1"
    id("maven-publish")
}

group = "io.github.fisher2911"
version = "1.0.2-beta"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
    maven("https://jitpack.io")
    maven("https://maven.enginehub.org/repo/")
}


dependencies {
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldedit:worldedit-core:7.2.12-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.7-SNAPSHOT")
    compileOnly("com.github.Fisher2911:FisherLib:-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:23.0.0")
    implementation("org.bstats:bstats-bukkit:3.0.0")
    implementation("com.zaxxer:HikariCP:3.3.0")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    build {
        dependsOn(jar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }

    shadowJar {
        relocate("com.zaxxer.hikari", "io.github.fisher2911.schematicpaster.hikari")
        relocate("org.bstats", "io.github.fisher2911.schematicpaster.bstats")
        archiveFileName.set("SchematicPaster-${version}.jar")
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        filteringCharset = Charsets.UTF_8.name()
    }

}
