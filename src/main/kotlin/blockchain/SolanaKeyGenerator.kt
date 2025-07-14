package blockchain

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Provides functionality to generate Solana private keys from BIP-39 mnemonic phrases.
 * This implementation minimizes external dependencies by using standard Java Cryptography Architecture (JCA)
 * for HMAC and PBKDF2 operations.
 */
object SolanaKeyGenerator {

    private const val PBKDF2_ITERATION_COUNT = 2048
    private const val SEED_SIZE_BITS = 512
    private const val HMAC_SHA512_ALGORITHM = "HmacSHA512"
    private val SOLANA_DERIVATION_PATH = intArrayOf(44, 501, 0, 0) // Represents m/44'/501'/0'/0'

    /**
     * Derives a 32-byte Ed25519 private key seed from a BIP-39 mnemonic phrase
     * according to Solana's standards (BIP-39 -> BIP-32/SLIP-0010).
     *
     * @param mnemonic The space-separated mnemonic phrase.
     * @param passphrase An optional passphrase for the mnemonic (defaults to empty).
     * @return A 32-byte array representing the private key seed.
     */
    fun getPrivateKeyFromMnemonic(mnemonic: String, passphrase: String = ""): ByteArray {
        // 1. Mnemonic to Seed
        val seed = mnemonicToSeed(mnemonic, passphrase)
        println("seed: " + seed.joinToString("") { "%02x".format(it) })

        // 2. Seed to Master Key/Chain Code via SLIP-0010
        val masterKey = hmacSha512("ed25519 seed".toByteArray(StandardCharsets.UTF_8), seed)
        var currentKey = masterKey.copyOfRange(0, 32)
        var currentChainCode = masterKey.copyOfRange(32, 64)

        // 3. Derive path m/44'/501'/0'/0'
        // All levels are hardened as per SLIP-0010 for Ed25519
        for (index in SOLANA_DERIVATION_PATH) {
            val hardenedIndex = index or 0x80000000.toInt() // Apply hardening
            val data = ByteBuffer.allocate(37)
                .put(0.toByte()) // 0x00 for private key derivation
                .put(currentKey)
                .putInt(hardenedIndex)
                .array()

            val hmacResult = hmacSha512(currentChainCode, data)
            currentKey = hmacResult.copyOfRange(0, 32)
            currentChainCode = hmacResult.copyOfRange(32, 64)
        }

        return currentKey
    }

    /**
     * Converts a mnemonic phrase to a BIP-39 seed using PBKDF2 with HMAC-SHA512.
     */
    private fun mnemonicToSeed(mnemonic: String, passphrase: String): ByteArray {
        val salt = "mnemonic$passphrase".toByteArray(StandardCharsets.UTF_8)
        val spec = PBEKeySpec(
            mnemonic.toCharArray(),
            salt,
            PBKDF2_ITERATION_COUNT,
            SEED_SIZE_BITS
        )
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
        return factory.generateSecret(spec).encoded
    }

    /**
     * Computes HMAC-SHA512.
     */
    private fun hmacSha512(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance(HMAC_SHA512_ALGORITHM)
        mac.init(SecretKeySpec(key, HMAC_SHA512_ALGORITHM))
        return mac.doFinal(data)
    }
}

fun main() {
    // A known test vector for validation
    val mnemonic = "legal winner thank year wave sausage worth useful legal winner thank yellow"
    val expectedPrivateKeyHex = "23137059f44c35338159248a34c71e53a18c6327739975c8423c1b53b150244a"
    val expectedSolanaAddress = "41j23f4cjd85yD5q2v3c5d6e7f8g9h0iAjBkClDmEnFp" // Replace with actual expected address if known

    println("Mnemonic: \"$mnemonic\"")

    // Generate the private key
    val privateKeyBytes = SolanaKeyGenerator.getPrivateKeyFromMnemonic(mnemonic)
    val privateKeyHex = privateKeyBytes.joinToString("") { "%02x".format(it) }

    // Derive the public key using Bouncy Castle
    val keyParams = Ed25519PrivateKeyParameters(privateKeyBytes, 0)
    val publicKeyParams = keyParams.generatePublicKey()
    val publicKeyBytes = publicKeyParams.encoded

    // Solana address is the Base58 encoding of the public key
    val solanaAddress = Base58.encode(publicKeyBytes)

    println("Generated Private Key (Hex): $privateKeyHex")
    println("Expected Private Key (Hex):  $expectedPrivateKeyHex")
    println("Keys Match: ${privateKeyHex == expectedPrivateKeyHex}")
    println("Generated Solana Address: $solanaAddress")
}