package com.zanaaustralis.core.secure

import com.zanaaustralis.core.BuildConfig

/**
 * @author esteban.rivera01
 */

/**
 * Flags de seguridad que dependen de cambios coordinados con el backend.
 * Se activan en build con `-Pargos.signatureV4=true` (ver docs/BACKEND_SECURITY_CHANGES.md).
 */
object SecurityFlags {

    /** Firma v4 con nonce + hash del payload y clave ligada a biometría (B1, B2). */
    val signatureV4Enabled: Boolean = BuildConfig.SIGNATURE_V4_ENABLED
}
