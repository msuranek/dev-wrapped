plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
    id("org.jetbrains.intellij.platform") version "2.19.0"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity(providers.gradleProperty("platformVersion"))
        pluginVerifier()
        zipSigner()
    }
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("junit:junit:4.13.2")
}

kotlin {
    jvmToolchain(21)
    compilerOptions { freeCompilerArgs.add("-Xjvm-default=all") }
}

intellijPlatform {
    pluginConfiguration {
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")
        ideaVersion { sinceBuild = "252" }
        description = providers.fileContents(layout.projectDirectory.file("MARKETPLACE.md")).asText
        changeNotes = providers.fileContents(layout.projectDirectory.file("docs/MARKETPLACE_CHANGE_NOTES.html")).asText
    }
    pluginVerification {
        ides {
            current()
            providers.environmentVariable("PHPSTORM_HOME").orNull?.let { local(it) }
        }
    }
    publishing { token = providers.environmentVariable("PUBLISH_TOKEN") }
    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }
}

tasks {
    test { useJUnitPlatform() }
    register<JavaExec>("generatePreviewCard") {
        group = "verification"
        description = "Generates the README preview with the production card renderer."
        dependsOn(testClasses)
        classpath = sourceSets.test.get().runtimeClasspath
        mainClass = "com.devwrapped.render.SampleCardGenerator"
        args(layout.projectDirectory.file("docs/dev-wrapped-preview.png").asFile.absolutePath)
    }
    register<JavaExec>("generateArchetypeQaCards") {
        group = "verification"
        description = "Generates ignored sample cards for visual QA of every archetype."
        dependsOn(testClasses)
        classpath = sourceSets.test.get().runtimeClasspath
        mainClass = "com.devwrapped.render.ArchetypeQaGenerator"
        args(layout.buildDirectory.dir("visual-qa").get().asFile.absolutePath)
    }
}
