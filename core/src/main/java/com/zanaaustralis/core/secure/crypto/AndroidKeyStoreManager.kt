package com.zanaaustralis.core.secure.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author esteban.rivera01
 */

@Singleton
class AndroidKeyStoreManager @Inject constructor() {

    companion object {
        private const val KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "aegis_device_key"
    }

    private fun loadKeyStore(): KeyStore =
        KeyStore.getInstance(KEYSTORE).apply {
            load(null)
        }
    fun hasKey(): Boolean =
        loadKeyStore().containsAlias(KEY_ALIAS)

    fun generateKeyPairIfNeeded() {
        //val keyStore = loadKeyStore()
        if (hasKey()) return

        val generator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA,
            KEYSTORE
        )

        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .setKeySize(2048)
            .build()

        generator.initialize(spec)
        generator.generateKeyPair()
    }


    fun getPrivateKey(): PrivateKey {
        val keyStore = loadKeyStore()
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            throw IllegalStateException("Device key not found – device not enrolled")
        }

        val entry = keyStore.getEntry(KEY_ALIAS, null)
                as? KeyStore.PrivateKeyEntry
            ?: throw IllegalStateException("Invalid key entry")

        return entry.privateKey
    }


    private fun getPublicKey(): PublicKey {
        val keyStore = loadKeyStore()
        return (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry)
            .certificate.publicKey
    }


    fun publicKeyToPem(): String = getPublicKey().toPem()
    fun regenerateKeyPair() {
        val keyStore = loadKeyStore()

        runCatching {
            if (keyStore.containsAlias(KEY_ALIAS)) {
                keyStore.deleteEntry(KEY_ALIAS)
            }
        }.getOrElse {
            throw IllegalStateException("Error deleting device key", it)
        }

        val generator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA,
            KEYSTORE
        )

        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .setKeySize(2048)
            .build()

        generator.initialize(spec)
        generator.generateKeyPair()
    }
}