package com.zanaaustralis.core.secure.util

/**
 * @author esteban.rivera01
 */

import android.util.Base64
import java.security.PublicKey
import javax.crypto.Cipher

// TODO ELIMINAR
object CryptoHelper {

    /**
     * Encripta un payload JSON usando la llave pública RSA-OAEP almacenada localmente.
     * @param payload Cuerpo JSON a encriptar.
     * @param publicKey Llave pública en formato PublicKey (obtenida de RsaKeyManager).
     * @return Cadena Base64 con los datos cifrados.
     */
    fun encryptPayload(payload: String, publicKey: PublicKey): String {
        return try {
            val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            val encryptedBytes = cipher.doFinal(payload.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Error al encriptar el payload: ${e.message}")
        }
    }

    /**
     * Decodifica una llave pública almacenada como PEM (BEGIN/END PUBLIC KEY)
     */
    fun decodePublicKeyFromPem(pem: String): PublicKey {
        val cleanPem = pem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), "")
        val encoded = Base64.decode(cleanPem, Base64.DEFAULT)
        val keySpec = java.security.spec.X509EncodedKeySpec(encoded)
        val keyFactory = java.security.KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec)
    }
}