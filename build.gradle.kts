    plugins {
        application
        kotlin("jvm") version "1.9.22"
    }

    group = "org.example"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
    dependencies {
        implementation("mysql:mysql-connector-java:8.0.33")
        implementation("com.squareup.retrofit2:retrofit:2.10.0")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
        implementation("com.squareup.retrofit2:converter-gson:2.10.0")
        testImplementation("org.jetbrains.kotlin:kotlin-test")
    }
    application{
        mainClass.set("MainKt")
    }
    tasks.jar {
        dependsOn(listOf("compileJava", "compileKotlin", "processResources"))
        archiveClassifier.set("standalone")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest { attributes(mapOf("Main-Class" to application.mainClass)) }
        val sourcesMain = sourceSets.main.get()
        val contents = configurations.runtimeClasspath.get()
            .map { if (it.isDirectory) it else zipTree(it) } + sourcesMain.output
        from(contents)
    }
    tasks.test {
        useJUnitPlatform()
    }
    kotlin {
        jvmToolchain(21)
    }
    java{
        JavaVersion.VERSION_21
    }
