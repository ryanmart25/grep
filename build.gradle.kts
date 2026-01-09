plugins {
    id("java")
}

group = "dev.martinez"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("info.picocli:picocli:4.7.7")
}

tasks.test {
    useJUnitPlatform()
}
tasks.jar {
    manifest{
        attributes["Main-Class"] = "dev.martinez.Grep"
    }
}