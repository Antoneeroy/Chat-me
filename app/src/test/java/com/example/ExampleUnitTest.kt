package com.example

import com.example.crypto.CryptoEngine
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testAes256GcmEncryptionAndDecryption() {
        val secretKey = CryptoEngine.generateAesKey()
        val originalMessage = "Self-hosted E2EE test payload with AES-256-GCM"

        val encryptedPayload = CryptoEngine.encryptText(originalMessage, secretKey)
        assertNotNull(encryptedPayload.ciphertextBase64)
        assertNotNull(encryptedPayload.ivBase64)
        assertEquals("AES-256-GCM", encryptedPayload.algorithm)

        val decryptedMessage = CryptoEngine.decryptText(encryptedPayload, secretKey)
        assertEquals(originalMessage, decryptedMessage)
    }

    @Test
    fun testSha256IntegrityHash() {
        val data = "chatme-media-integrity-check".toByteArray(Charsets.UTF_8)
        val hash = CryptoEngine.sha256Hex(data)
        assertNotNull(hash)
        assertEquals(64, hash.length) // SHA-256 is 64 hex chars
    }

    @Test
    fun testSafetyNumberComputation() {
        val myFingerprint = "5A:8E:1F:B4:9C:20:D7:E3:44:01:29:A0:88:51:7E:11"
        val peerFingerprint = "7F:3A:99:B2:1C:E4:66:88:9A:F0:34:B8:21:77:AC:E1"

        val safetyNumber = CryptoEngine.computeSafetyNumber(myFingerprint, peerFingerprint)
        val blocks = safetyNumber.split(" ")
        assertEquals(12, blocks.size) // 12 groups of 5 digits = 60 digits total
        for (block in blocks) {
            assertEquals(5, block.length)
            assertTrue(block.all { it.isDigit() })
        }
    }
}
