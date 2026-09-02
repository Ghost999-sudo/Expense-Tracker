package com.example.expensetracker.util

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {

    private const val ITERATIONS = 100_000

    private const val KEY_LENGTH = 256

    private val random = SecureRandom()

    fun generateSalt(): String {

        val bytes = ByteArray(16)

        random.nextBytes(bytes)

        return Base64.encodeToString(
            bytes,
            Base64.NO_WRAP
        )
    }

    fun hash(
        password: String,
        salt: String
    ): String {

        val spec = PBEKeySpec(
            password.toCharArray(),
            Base64.decode(salt, Base64.NO_WRAP),
            ITERATIONS,
            KEY_LENGTH
        )

        val factory =
            SecretKeyFactory.getInstance(
                "PBKDF2WithHmacSHA1"
            )

        val bytes =
            factory.generateSecret(spec).encoded

        return Base64.encodeToString(
            bytes,
            Base64.NO_WRAP
        )
    }

    fun verify(
        password: String,
        salt: String,
        expectedHash: String
    ): Boolean {

        return hash(password, salt) == expectedHash
    }
}