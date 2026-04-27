plugins {
    java
    id("io.github.goooler.shadow") version "8.1.8"
    eclipse
    idea
}

group = "me.orineko"
version = "2.6"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

configurations.all {
    exclude(group = "org.spigotmc", module = "spigot-api")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    testImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    implementation("com.github.huynhphap100:PluginSpigotTools:1.3.6-4")
    compileOnly("me.clip:placeholderapi:2.11.6")
    testImplementation("me.clip:placeholderapi:2.11.6")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.6")
    testImplementation("com.sk89q.worldguard:worldguard-bukkit:7.0.6")
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.108.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
    
    compileOnly("com.github.LoneDev6:api-itemsadder:3.6.1")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.processResources {
    filteringCharset = "UTF-8"
    val props = mapOf("project" to project)
    inputs.property("version", project.version)
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    minimize()
    relocate("me.orineko.pluginspigottools", "me.orineko.thirstbar.tools")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-Djdk.net.URLClassPath.disableClassPathURLCheck=true")
    
    // Only run tests if "test" is explicitly passed in the command line
    onlyIf {
        project.gradle.startParameter.taskNames.contains("test")
    }
}

eclipse {
    classpath {
        file {
            whenMerged {
                val classpath = this as org.gradle.plugins.ide.eclipse.model.Classpath
                classpath.entries.forEach { entry ->
                    if (entry is org.gradle.plugins.ide.eclipse.model.Library) {
                        entry.entryAttributes.remove("gradle_used_by_scope")
                    }
                }
            }
        }
    }
}
