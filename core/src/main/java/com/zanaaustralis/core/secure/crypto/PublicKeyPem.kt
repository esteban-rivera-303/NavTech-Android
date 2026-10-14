package com.zanaaustralis.core.secure.crypto

import java.security.PublicKey
import java.util.Base64

/**
 * @author esteban.rivera01
 */

/** Codifica la clave pública (X.509 SubjectPublicKeyInfo) en PEM de una sola línea de contenido. */
fun PublicKey.toPem(): String =
    "-----BEGIN PUBLIC KEY-----\n" +
        Base64.getEncoder().encodeToString(encoded) +
        "\n-----END PUBLIC KEY-----"
