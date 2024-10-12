import org.jetbrains.kotlin.gradle.tasks.KotlinCompile



plugins {
	id("org.springframework.boot") version "3.1.3"
	id("io.spring.dependency-management") version "1.1.3"
	id ("org.jetbrains.kotlin.plugin.allopen") version "1.7.10"
	kotlin("jvm") version "1.8.22"
	kotlin("plugin.spring") version "1.8.22"
	kotlin("plugin.jpa") version "1.8.22"
}

allOpen {
	annotation("javax.persistence.Entity")
}
group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.kafka:spring-kafka")
	implementation ("io.github.microutils:kotlin-logging:2.0.10")
	//jackson
	implementation ("com.fasterxml.jackson.core:jackson-databind:2.15.2")
	implementation ("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.15.2")
	implementation("org.apache.pdfbox:pdfbox:2.0.29")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	implementation("commons-fileupload:commons-fileupload:1.5")
	implementation ("com.google.code.gson:gson:2.8.8")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.0")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.3.0")
	implementation ("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")


	//Email
	implementation("org.springframework.boot:spring-boot-starter-mail")

	//Security
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.projectlombok:lombok:1.18.26")
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
	implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation ("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation ("org.springframework.boot:spring-boot-starter-websocket")
	implementation ("org.springframework.security:spring-security-cas")
	implementation("org.springframework.boot:spring-boot-starter-data-ldap")
	
	runtimeOnly("org.postgresql:postgresql")
	implementation ("org.mariadb.jdbc:mariadb-java-client:2.7.3")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.kafka:spring-kafka-test")


}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"

	}

}

tasks.withType<Test> {
	useJUnitPlatform()
}

