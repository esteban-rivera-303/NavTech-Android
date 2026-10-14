package com.zanaaustralis.feature_kyc.presentation

import java.net.URI

/**
 * @author esteban.rivera01
 */

/**
 * Política del WebView de KYC: solo se navega y se conceden cámara/micrófono
 * a orígenes HTTPS de Didit.
 */
object KycWebPolicy {

    private const val TRUSTED_DOMAIN = "didit.me"
    private const val HTTPS = "https"

    // Valores de android.webkit.PermissionRequest.RESOURCE_*
    private const val RESOURCE_VIDEO_CAPTURE = "android.webkit.resource.VIDEO_CAPTURE"
    private const val RESOURCE_AUDIO_CAPTURE = "android.webkit.resource.AUDIO_CAPTURE"
    private val GRANTABLE_RESOURCES = setOf(RESOURCE_VIDEO_CAPTURE, RESOURCE_AUDIO_CAPTURE)

    fun isTrustedUrl(url: String?): Boolean {
        val uri = url?.let { runCatching { URI(it) }.getOrNull() } ?: return false
        if (!uri.scheme.equals(HTTPS, ignoreCase = true)) return false

        val host = uri.host?.lowercase() ?: return false
        return host == TRUSTED_DOMAIN || host.endsWith(".$TRUSTED_DOMAIN")
    }

    fun grantablePermissions(origin: String?, requested: Array<String>): Array<String> {
        if (!isTrustedUrl(origin)) return emptyArray()
        return requested.filter { it in GRANTABLE_RESOURCES }.toTypedArray()
    }
}
