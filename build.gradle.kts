plugins {
	id("java")
	id("maven-publish")
}

group 	                = "com.ronreynolds"
version                 = "1.0.1"

val assertJVersion      = "3.27.3"		// 2025-01-18
val jUnitJupiterVersion = "5.12.0"      // 2025-02-21
val slf4jVersion        = "2.0.17"      // 2025-02-25

java {
	sourceCompatibility = JavaVersion.VERSION_11
	targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
	mavenLocal()
}

dependencies {
	implementation    ("org.slf4j:slf4j-api:$slf4jVersion")
    implementation    ("org.assertj:assertj-core:$assertJVersion")
	testImplementation("org.junit.jupiter:junit-jupiter:$jUnitJupiterVersion")
	testRuntimeOnly   ("org.junit.platform:junit-platform-launcher")
}

tasks.compileJava {
	options.encoding = "UTF-8"
}

tasks.compileTestJava {
    options.encoding = "UTF-8"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			from(components["java"])
		}
	}
}