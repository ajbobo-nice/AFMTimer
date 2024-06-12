plugins {
    id("java")
}

group = "com.nice"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("joda-time:joda-time:2.12.7")
    implementation("com.opencsv:opencsv:5.9")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}