package com.example.data

import android.content.Context
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object DatabaseKeyManager {
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "smart_osm_database_key_v1"
    private const val PREFS = "smart_osm_database_security"
    private const val WRAPPED_KEY = "wrapped_passphrase"
    private const val IV = "wrapped_passphrase_iv"
    private const val PASSPHRASE_BYTES = 32
    private const val GCM_TAG_BITS = 128

    fun getOrCreatePassphrase(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val wrapped = prefs.getString(WRAPPED_KEY, null)
        val iv = prefs.getString(IV, null)

        if (wrapped != null && iv != null) {
            return decrypt(wrapped, iv, getOrCreateKeystoreKey())
        }

        if (context.getDatabasePath("person_db_enc").exists()) {
            throw IllegalStateException(
                "Existing encrypted database has no device-bound key. " +
                    "A controlled database-key migration is required; refusing to reset or downgrade encryption."
            )
        }

        val randomPassphrase = ByteArray(PASSPHRASE_BYTES).also { SecureRandom().nextBytes(it) }
        val encodedPassphrase = Base64.encodeToString(
            randomPassphrase,
            Base64.NO_WRAP or Base64.NO_PADDING
        )

        val encrypted = encrypt(encodedPassphrase, getOrCreateKeystoreKey())
        prefs.edit()
            .putString(WRAPPED_KEY, encrypted.ciphertext)
            .putString(IV, encrypted.iv)
            .apply()

        return encodedPassphrase
    }

    private fun getOrCreateKeystoreKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val existing = keyStore.getKey(KEY_ALIAS, null)
        if (existing is SecretKey) return existing

        val generator = KeyGenerator.getInstance("AES", KEYSTORE_PROVIDER)
        generator.init(
            android.security.keystore.KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or
                    android.security.keystore.KeyProperties.PURPOSE_DECRYPT
            )
                .setKeySize(256)
                .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false)
                .build()
        )
        return generator.generateKey()
    }

    private data class EncryptedValue(val ciphertext: String, val iv: String)

    private fun encrypt(value: String, key: SecretKey): EncryptedValue {
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        return EncryptedValue(
            Base64.encodeToString(
                cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8)),
                Base64.NO_WRAP
            ),
            Base64.encodeToString(iv, Base64.NO_WRAP)
        )
    }

    private fun decrypt(ciphertext: String, iv: String, key: SecretKey): String {
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(
                Cipher.DECRYPT_MODE,
                key,
                GCMParameterSpec(GCM_TAG_BITS, Base64.decode(iv, Base64.NO_WRAP))
            )
            String(
                cipher.doFinal(Base64.decode(ciphertext, Base64.NO_WRAP)),
                StandardCharsets.UTF_8
            )
        } catch (e: Exception) {
            throw IllegalStateException(
                "The device-bound database key could not be recovered. " +
                    "The encrypted database must not be replaced automatically.",
                e
            )
        }
    }
}
