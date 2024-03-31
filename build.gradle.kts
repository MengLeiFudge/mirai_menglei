plugins {
    val kotlinVersion = "1.8.0"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("net.mamoe.mirai-console") version "2.15.0"
    id("java")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

group = "org.menglei"
version = "0.1.0"

repositories {
    maven("https://maven.aliyun.com/repository/central")
    maven("https://maven.aliyun.com/repository/public")
    maven("https://maven.aliyun.com/repository/google")
    mavenLocal()
    maven("https://repo.mirai.mamoe.net/snapshots")
    maven { url = uri("https://maven.aliyun.com/nexus/content/groups/public/") }
    mavenCentral()
}

dependencies {
    // lombok，编译有效，打包无效
    compileOnly("org.projectlombok:lombok:1.18.32")
    // gradle 5.0以上版本注解处理不再compile classpath，需要增加 annotation processor path
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    implementation("org.slf4j:slf4j-api:2.0.9")
    //mirai已经有了自带的日志转换，不需要logback
    //implementation("ch.qos.logback:logback-core:1.4.11")
    //implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("commons-io:commons-io:2.15.1")
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("org.dom4j:dom4j:2.1.4")
    implementation("com.alibaba.fastjson2:fastjson2:2.0.46.android8")
    implementation("com.google.guava:guava:32.1.3-jre")
    implementation("org.apache.httpcomponents.client5:httpclient5-fluent:5.3")
    implementation("com.google.zxing:javase:3.5.2")
    implementation("org.xerial:sqlite-jdbc:3.45.0.0")
}