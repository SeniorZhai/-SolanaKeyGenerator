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

    /**
     * Derives a 32-byte Ed25519 private key seed from a BIP-39 mnemonic phrase
     * according to Solana's standards (BIP-39 -> BIP-32/SLIP-0010).
     *
     * @param mnemonic The space-separated mnemonic phrase.
     * @param passphrase An optional passphrase for the mnemonic (defaults to empty).
     * @param derivationPath The derivation path as an IntArray (e.g., intArrayOf(44, 501, 0, 0)).
     * @return A 32-byte array representing the private key seed.
     */
    fun getPrivateKeyFromMnemonic(mnemonic: String, passphrase: String = "", derivationPath: IntArray): ByteArray {
        // 1. Mnemonic to Seed
        val seed = mnemonicToSeed(mnemonic, passphrase)
        println("seed: " + seed.joinToString("") { "%02x".format(it) })

        // 2. Seed to Master Key/Chain Code via SLIP-0010
        val masterKey = hmacSha512("ed25519 seed".toByteArray(StandardCharsets.UTF_8), seed)
        var currentKey = masterKey.copyOfRange(0, 32)
        var currentChainCode = masterKey.copyOfRange(32, 64)

        // 3. Derive path m/44'/501'/0'/0'
        // All levels are hardened as per SLIP-0010 for Ed25519
        for (index in derivationPath) {
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
    val mnemonic = "legal winner thank year wave sausage worth useful legal winner thank yellow"
    println("Mnemonic: \"$mnemonic\"")

    for (i in 0..9) {
        val derivationPath = intArrayOf(44, 501, i, 0) // m/44'/501'/i'/0'
        println("\n--- Derivation Path: m/44'/501'/0'/$i' ---")

        // Generate the private key
        val privateKeyBytes = SolanaKeyGenerator.getPrivateKeyFromMnemonic(mnemonic, derivationPath = derivationPath)

        // Derive the public key using Bouncy Castle
        val keyParams = Ed25519PrivateKeyParameters(privateKeyBytes, 0)
        val publicKeyParams = keyParams.generatePublicKey()
        val publicKeyBytes = publicKeyParams.encoded

        // Solana address is the Base58 encoding of the public key
        val solanaAddress = Base58.encode(publicKeyBytes)

        println("Generated Solana Address for index $i: $solanaAddress")
    }
}