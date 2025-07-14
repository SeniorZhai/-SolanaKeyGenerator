plugins {
    kotlin("jvm") version "1.9.23"
    application
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // JUnit for testing
    testImplementation(kotlin("test"))

    // Bouncy Castle for Ed25519 cryptography
    implementation("org.bouncycastle:bcprov-jdk18on:1.77")

    // BitcoinJ for Base58 encoding
    implementation("org.bitcoinj:bitcoinj-core:0.17")

    // Libraries for BIP32/BIP39
    implementation("cash.z.ecc.android:kotlin-bip39:1.0.4")
    implementation("com.github.walleth.kethereum:bip32:0.86.0")
    implementation("com.github.mixinnetwork:tink-eddsa:0.0.13")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("blockchain.SolanaKeyGeneratorKt")
}