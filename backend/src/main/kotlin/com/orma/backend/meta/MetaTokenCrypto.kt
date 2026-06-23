package com.orma.backend.meta

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class MetaTokenCrypto(
    secret: String,
) {
    private val key = SecretKeySpec(
        MessageDigest.getInstance("SHA-256").digest(secret.toByteArray(Charsets.UTF_8)),
        "AES",
    )
    private val random = SecureRandom()

    fun encrypt(value: String): String {
        val nonce = ByteArray(12)
        random.nextBytes(nonce)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, nonce))
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(nonce + encrypted)
    }

    fun decrypt(value: String): String {
        val bytes = Base64.getDecoder().decode(value)
        require(bytes.size > 12) { "Invalid encrypted Meta token." }
        val nonce = bytes.copyOfRange(0, 12)
        val encrypted = bytes.copyOfRange(12, bytes.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, nonce))
        return String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }
}
