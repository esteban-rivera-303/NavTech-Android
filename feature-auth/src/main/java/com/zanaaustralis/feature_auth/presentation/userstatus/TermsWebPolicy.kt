package com.zanaaustralis.feature_auth.presentation.userstatus

import java.net.URI

/**
 * @author esteban.rivera01
 */

/**
 * Política del WebView de términos: la página es una SPA (necesita JavaScript),
 * así que solo se permite navegar dentro del mismo origen HTTPS que la carga.
 */
object TermsWebPolicy {

    private const val HTTPS = "https"

    fun isAllowedNavigation(termsUrl: String, targetUrl: String?): Boolean {
        val terms = parseHttps(termsUrl) ?: return false
        val target = targetUrl?.let { parseHttps(it) } ?: return false
        return terms.host.equals(target.host, ignoreCase = true) && terms.port == target.port
    }

    private fun parseHttps(url: String): URI? {
        val uri = runCatching { URI(url) }.getOrNull() ?: return null
        if (!uri.scheme.equals(HTTPS, ignoreCase = true) || uri.host == null) return null
        return uri
    }
}
