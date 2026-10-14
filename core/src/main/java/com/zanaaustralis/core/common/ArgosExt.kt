package com.zanaaustralis.core.common

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone
import javax.crypto.AEADBadTagException

/**
 * @author esteban.rivera01
 */


@RequiresApi(Build.VERSION_CODES.O)
fun parseIsoDate(date: String):Long{
    return java.time.Instant.parse(date).toEpochMilli()
}

fun Throwable.isCryptoError(): Boolean {
    var current: Throwable? = this
    while (current != null) {
        if (current is AEADBadTagException) return true
        current = current.cause
    }
    return false
}

fun formatTimeRemaining(expiresAt: Long): String {
    val now = System.currentTimeMillis()
    val diffMillis = expiresAt - now

    if (diffMillis <= 0) return "Expirado"

    val minutes = diffMillis / 60000
    val hours = minutes / 60

    return when {
        minutes < 1 -> "Menos de 1 minuto"
        minutes < 60 -> "Expira en $minutes min"
        hours < 24 -> "Expira en $hours h"
        else -> "Expira en ${hours / 24} días"
    }
}

fun formatTimestamp(millis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.systemDefault())

    return formatter.format(Instant.ofEpochMilli(millis))
}

fun parseIsoToMillis(iso: String): Long {
    return try {
        OffsetDateTime.parse(iso).toInstant().toEpochMilli()
    } catch (e: Exception) {
        0L
    }
}

fun formatRut(input: String): String {
    val clean = input
        .replace(".", "")
        .replace("-", "")
        .uppercase()
        .filter { it.isDigit() || it == 'K' }
        .take(9)

    if (clean.isEmpty()) return ""

    // Mientras todavía no parece un RUT completo,
    // no forzamos el guión con DV
    if (clean.length <= 7) {
        return formatRutBody(clean)
    }

    val body = clean.dropLast(1)
    val dv = clean.takeLast(1)

    return "${formatRutBody(body)}-$dv"
}

private fun formatRutBody(body: String): String {
    return body
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()
}

 fun String.firstName(): String =
    trim()
        .split("\\s+".toRegex())
        .firstOrNull()
        .orEmpty()

