package com.zanaaustralis.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorTest {

    // ---------- Email ----------

    @Test
    fun `valid emails pass`() {
        assertTrue(validateEmail("ana.perez+test@example.cl").isValid)
    }

    @Test
    fun `blank and malformed emails fail with a message`() {
        assertEquals("El email no puede estar vacío", validateEmail(" ").error)
        assertEquals("Formato de email inválido", validateEmail("ana@").error)
        assertFalse(validateEmail("ana@example").isValid)
    }

    // ---------- RUT ----------

    @Test
    fun `valid ruts pass including k and zero check digits`() {
        assertTrue(validateRut("12.345.678-5").isValid)
        assertTrue(validateRut("7654321-6").isValid)
        assertTrue(validateRut("10.000.013-k").isValid)
        assertTrue(validateRut("10000004-0").isValid)
    }

    @Test
    fun `rut errors explain the problem`() {
        assertEquals("El RUT no puede estar vacío", validateRut("").error)
        assertEquals("RUT demasiado corto", validateRut("1-9").error)
        assertEquals("El RUT contiene caracteres inválidos", validateRut("12A45678-5").error)
        assertEquals("Dígito verificador incorrecto", validateRut("12.345.678-9").error)
    }

    // ---------- Teléfono ----------

    @Test
    fun `chilean mobile numbers in e164 pass`() {
        assertTrue(validatePhone("+56912345678").isValid)
    }

    @Test
    fun `other phone formats fail`() {
        assertEquals("El teléfono no puede estar vacío", validatePhone("").error)
        assertFalse(validatePhone("912345678").isValid)
        assertFalse(validatePhone("+5691234567").isValid)
        assertFalse(validatePhone("+569123456789").isValid)
    }
}
