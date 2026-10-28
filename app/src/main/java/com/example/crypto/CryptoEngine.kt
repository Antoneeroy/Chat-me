package com.example.crypto

import android.util.Base64
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

data class EncryptedPayload(
    val ciphertextBase64: String,
    val ivBase64: String,
    val algorithm: String = "AES-256-GCM",
    val keyFingerprint: String,
    val messageCounter: Int,
    val ratchetStep: Int
)

data class PreKeyBundle(
    val identityKeyHex: String,
    val signedPreKeyId: Int,
    val signedPreKeyHex: String,
    val signedPreKeySignature: String,
    val oneTimePreKeyHex: String
)

object CryptoEngine {
    private val secureRandom = SecureRandom()
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    // Generate simulated Ed25519 / EC Identity Key Pair
    fun generateIdentityKeyPair(): KeyPair {
        return try {
            val kpg = KeyPairGenerator.getInstance("EC")
            kpg.initialize(256, secureRandom)
            kpg.generateKeyPair()
        } catch (e: Exception) {
            val kpg = KeyPairGenerator.getInstance("RSA")
            kpg.initialize(2048, secureRandom)
            kpg.generateKeyPair()
        }
    }

    // Generate random 256-bit AES key for symmetric media/message encryption
    fun generateAesKey(): SecretKey {
        val kg = KeyGenerator.getInstance("AES")
        kg.init(256, secureRandom)
        return kg.generateKey()
    }

    fun secretKeyFromHex(hex: String): SecretKey {
        val bytes = hexToBytes(hex)
        return SecretKeySpec(bytes, "AES")
    }

    fun secretKeyToHex(key: SecretKey): String {
        return bytesToHex(key.encoded)
    }

    private fun encodeBase64(bytes: ByteArray): String {
        return try {
            android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        } catch (e: Throwable) {
            java.util.Base64.getEncoder().encodeToString(bytes)
        }
    }

    private fun decodeBase64(str: String): ByteArray {
        return try {
            android.util.Base64.decode(str, android.util.Base64.NO_WRAP)
        } catch (e: Throwable) {
            java.util.Base64.getDecoder().decode(str)
        }
    }

    // AES-256-GCM authenticated encryption
    fun encryptText(
        plainText: String,
        key: SecretKey,
        messageCounter: Int = 1,
        ratchetStep: Int = 1
    ): EncryptedPayload {
        val iv = ByteArray(GCM_IV_LENGTH)
        secureRandom.nextBytes(iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, spec)

        val ciphertext = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val fingerprint = sha256Hex(key.encoded).take(16)

        return EncryptedPayload(
            ciphertextBase64 = encodeBase64(ciphertext),
            ivBase64 = encodeBase64(iv),
            algorithm = "AES-256-GCM",
            keyFingerprint = fingerprint,
            messageCounter = messageCounter,
            ratchetStep = ratchetStep
        )
    }

    // AES-256-GCM decryption
    fun decryptText(payload: EncryptedPayload, key: SecretKey): String {
        return try {
            val iv = decodeBase64(payload.ivBase64)
            val ciphertext = decodeBase64(payload.ciphertextBase64)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            val plaintextBytes = cipher.doFinal(ciphertext)
            String(plaintextBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            "[Decryption Failed: Invalid Key or Corrupted Ciphertext]"
        }
    }

    // SHA-256 hash for file integrity verification
    fun sha256Hex(data: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(data)
        return bytesToHex(digest)
    }

    // Signal Protocol 60-digit safety number calculation
    // Generates 12 groups of 5 decimal digits from public key fingerprints
    fun computeSafetyNumber(myFingerprint: String, contactFingerprint: String): String {
        val sortedKeys = listOf(myFingerprint, contactFingerprint).sorted().joinToString(":")
        val md = MessageDigest.getInstance("SHA-512")
        val hash = md.digest(sortedKeys.toByteArray(Charsets.UTF_8))

        val digits = StringBuilder()
        for (i in 0 until 12) {
            val offset = (i * 4) % (hash.size - 4)
            val value = ((hash[offset].toInt() and 0xFF) shl 24) or
                    ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                    ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                    (hash[offset + 3].toInt() and 0xFF)
            val positiveVal = Math.abs(value) % 100000
            digits.append(String.format("%05d", positiveVal))
            if (i < 11) digits.append(" ")
        }
        return digits.toString()
    }

    // Deterministic QR Matrix Generator for Safety Numbers (21x21 binary grid)
    fun generateQrMatrix(data: String): Array<BooleanArray> {
        val size = 25
        val matrix = Array(size) { BooleanArray(size) { false } }
        val hash = sha256Hex(data.toByteArray(Charsets.UTF_8))

        // Position Detection Patterns (Corners)
        fun drawFinder(r: Int, c: Int) {
            for (i in 0..6) {
                for (j in 0..6) {
                    val isBorder = i == 0 || i == 6 || j == 0 || j == 6
                    val isCenter = i in 2..4 && j in 2..4
                    matrix[r + i][c + j] = isBorder || isCenter
                }
            }
        }

        drawFinder(0, 0)
        drawFinder(0, size - 7)
        drawFinder(size - 7, 0)

        // Alignment Pattern
        for (i in -2..2) {
            for (j in -2..2) {
                val isAlignBorder = Math.abs(i) == 2 || Math.abs(j) == 2
                val isAlignCenter = i == 0 && j == 0
                matrix[size - 7 + i][size - 7 + j] = isAlignBorder || isAlignCenter
            }
        }

        // Timing patterns
        for (i in 8 until size - 8) {
            matrix[6][i] = (i % 2 == 0)
            matrix[i][6] = (i % 2 == 0)
        }

        // Fill data based on hash bits
        var bitIndex = 0
        for (r in 0 until size) {
            for (c in 0 until size) {
                // Skip finders and separators
                if ((r < 8 && c < 8) || (r < 8 && c >= size - 8) || (r >= size - 8 && c < 8)) continue
                if (r == 6 || c == 6) continue
                if (r >= size - 9 && c >= size - 9) continue

                val charVal = hash[bitIndex % hash.length]
                val bit = (charVal.code + r * 7 + c * 13) % 3 != 0
                matrix[r][c] = bit
                bitIndex++
            }
        }

        return matrix
    }

    fun bytesToHex(bytes: ByteArray): String {
        val hexChars = "0123456789ABCDEF"
        val result = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            val i = b.toInt() and 0xFF
            result.append(hexChars[i shr 4])
            result.append(hexChars[i and 0x0F])
        }
        return result.toString()
    }

    private fun hexToBytes(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}
