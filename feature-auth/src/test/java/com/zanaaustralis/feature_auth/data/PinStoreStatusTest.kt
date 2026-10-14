package com.zanaaustralis.feature_auth.data

import org.junit.Assert.assertEquals
import org.junit.Test

class PinStoreStatusTest {

    @Test
    fun `no hash and never configured means no pin`() {
        assertEquals(
            PinStoreStatus.NONE,
            PinStoreStatus.resolve(hasHash = false, hasLegacy = false, wasConfigured = false)
        )
    }

    @Test
    fun `stored hash means pin is set`() {
        assertEquals(
            PinStoreStatus.SET,
            PinStoreStatus.resolve(hasHash = true, hasLegacy = false, wasConfigured = true)
        )
    }

    @Test
    fun `hash from before the marker existed still counts as set`() {
        assertEquals(
            PinStoreStatus.SET,
            PinStoreStatus.resolve(hasHash = true, hasLegacy = false, wasConfigured = false)
        )
    }

    @Test
    fun `legacy pin pending migration counts as set`() {
        assertEquals(
            PinStoreStatus.SET,
            PinStoreStatus.resolve(hasHash = false, hasLegacy = true, wasConfigured = false)
        )
    }

    @Test
    fun `configured pin whose encrypted store was wiped is lost`() {
        assertEquals(
            PinStoreStatus.LOST,
            PinStoreStatus.resolve(hasHash = false, hasLegacy = false, wasConfigured = true)
        )
    }
}
