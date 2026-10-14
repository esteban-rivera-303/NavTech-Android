package com.zanaaustralis.feature_kyc.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KycWebPolicyTest {

    private val video = "android.webkit.resource.VIDEO_CAPTURE"
    private val audio = "android.webkit.resource.AUDIO_CAPTURE"
    private val midi = "android.webkit.resource.MIDI_SYSEX"

    @Test
    fun `trusts https didit hosts`() {
        assertTrue(KycWebPolicy.isTrustedUrl("https://verify.didit.me/session/abc"))
        assertTrue(KycWebPolicy.isTrustedUrl("https://verification.didit.me/"))
        assertTrue(KycWebPolicy.isTrustedUrl("https://didit.me"))
    }

    @Test
    fun `rejects non https or foreign hosts`() {
        assertFalse(KycWebPolicy.isTrustedUrl("http://verify.didit.me/session/abc"))
        assertFalse(KycWebPolicy.isTrustedUrl("https://evil.com/?r=didit.me"))
        assertFalse(KycWebPolicy.isTrustedUrl("https://didit.me.evil.com/"))
        assertFalse(KycWebPolicy.isTrustedUrl("https://notdidit.me/"))
        assertFalse(KycWebPolicy.isTrustedUrl("javascript:alert(1)"))
        assertFalse(KycWebPolicy.isTrustedUrl(null))
        assertFalse(KycWebPolicy.isTrustedUrl("not a url"))
    }

    @Test
    fun `grants only camera and microphone to trusted origins`() {
        val granted = KycWebPolicy.grantablePermissions(
            origin = "https://verify.didit.me/",
            requested = arrayOf(video, audio, midi)
        )

        assertEquals(setOf(video, audio), granted.toSet())
    }

    @Test
    fun `grants nothing to untrusted origins`() {
        val granted = KycWebPolicy.grantablePermissions(
            origin = "https://evil.com/",
            requested = arrayOf(video, audio)
        )

        assertTrue(granted.isEmpty())
    }
}
