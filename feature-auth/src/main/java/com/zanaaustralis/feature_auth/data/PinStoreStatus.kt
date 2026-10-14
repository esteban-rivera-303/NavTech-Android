package com.zanaaustralis.feature_auth.data

/**
 * @author esteban.rivera01
 */

/**
 * Estado del PIN según lo que queda en el dispositivo.
 *
 * [LOST]: hubo un PIN configurado pero el almacén cifrado ya no tiene el hash
 * (p. ej. `CryptoManager` lo borró tras un error del Keystore). Se trata como
 * bloqueado para que el usuario lo restaure vía KYC en vez de crear uno nuevo
 * sin verificación.
 */
enum class PinStoreStatus {
    NONE,
    SET,
    LOST;

    companion object {
        fun resolve(hasHash: Boolean, hasLegacy: Boolean, wasConfigured: Boolean): PinStoreStatus =
            when {
                hasHash || hasLegacy -> SET
                wasConfigured -> LOST
                else -> NONE
            }
    }
}
