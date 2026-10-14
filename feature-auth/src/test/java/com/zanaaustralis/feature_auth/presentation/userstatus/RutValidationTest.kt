package com.zanaaustralis.feature_auth.presentation.userstatus

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RutValidationTest {

    @Test
    fun `accepts valid ruts with or without dots and dash`() {
        assertTrue(isValidRut("12.345.678-5"))
        assertTrue(isValidRut("123456785"))
        assertTrue(isValidRut("7654321-6"))
    }

    @Test
    fun `accepts k and zero check digits in any case`() {
        assertTrue(isValidRut("10.000.013-K"))
        assertTrue(isValidRut("10000013-k"))
        assertTrue(isValidRut("10.000.004-0"))
    }

    @Test
    fun `rejects wrong check digit`() {
        assertFalse(isValidRut("12.345.678-9"))
        assertFalse(isValidRut("10000013-1"))
    }

    @Test
    fun `rejects malformed input`() {
        assertFalse(isValidRut(""))
        assertFalse(isValidRut("5"))
        assertFalse(isValidRut("12a45678-5"))
    }
}
