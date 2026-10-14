package com.zanaaustralis.core.common

/**
 * @author esteban.rivera01
 */

data class ValidationResult(
    val isValid: Boolean,
    val error: String? = null
)

fun validateEmail(email: String): ValidationResult {
    if (email.isBlank()) {
        return ValidationResult(false, "El email no puede estar vacío")
    }

    val emailRegex =
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()

    return if (emailRegex.matches(email)) {
        ValidationResult(true)
    } else {
        ValidationResult(false, "Formato de email inválido")
    }
}


fun validateRut(rut: String): ValidationResult {
    if (rut.isBlank()) return ValidationResult(false, "El RUT no puede estar vacío")

    val clean = rut.replace(".", "").replace("-", "").uppercase()

    if (clean.length < 8) return ValidationResult(false, "RUT demasiado corto")

    val body = clean.dropLast(1)
    val dv = clean.last()

    if (!body.all { it.isDigit() }) {
        return ValidationResult(false, "El RUT contiene caracteres inválidos")
    }

    var m = 0
    var s = 1

    body.reversed().forEach {
        s = (s + (it.code - 48) * (9 - m++ % 6)) % 11
    }

    val expectedDv = if (s > 0) (s + 47).toChar() else 'K'

    return if (dv == expectedDv) {
        ValidationResult(true)
    } else {
        ValidationResult(false, "Dígito verificador incorrecto")
    }
}

fun validatePhone(phone: String): ValidationResult {
    if (phone.isBlank()) return ValidationResult(false, "El teléfono no puede estar vacío")

    val regex = "^\\+56\\d{9}\$".toRegex()

    return if (regex.matches(phone)) {
        ValidationResult(true)
    } else {
        ValidationResult(false, "El teléfono debe estar en formato +569XXXXXXXX")
    }
}

