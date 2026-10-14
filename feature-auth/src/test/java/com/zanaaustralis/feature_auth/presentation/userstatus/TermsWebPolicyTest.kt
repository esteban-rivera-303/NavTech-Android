package com.zanaaustralis.feature_auth.presentation.userstatus

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TermsWebPolicyTest {

    private val termsUrl = "https://terms.example.com/terms"

    @Test
    fun `allows navigation within the terms origin`() {
        assertTrue(TermsWebPolicy.isAllowedNavigation(termsUrl, "https://terms.example.com/terms#privacidad"))
        assertTrue(TermsWebPolicy.isAllowedNavigation(termsUrl, "https://TERMS.example.com/assets/doc"))
    }

    @Test
    fun `blocks other hosts including lookalike subdomains`() {
        assertFalse(TermsWebPolicy.isAllowedNavigation(termsUrl, "https://evil.com/terms"))
        assertFalse(TermsWebPolicy.isAllowedNavigation(termsUrl, "https://terms.example.com.evil.com/"))
        assertFalse(TermsWebPolicy.isAllowedNavigation(termsUrl, "https://sub.terms.example.com/"))
    }

    @Test
    fun `blocks non https schemes`() {
        assertFalse(TermsWebPolicy.isAllowedNavigation(termsUrl, "http://terms.example.com/terms"))
        assertFalse(TermsWebPolicy.isAllowedNavigation(termsUrl, "file:///data/data/app/x"))
        assertFalse(TermsWebPolicy.isAllowedNavigation(termsUrl, "intent://scan#Intent;end"))
    }

    @Test
    fun `blocks malformed or missing urls`() {
        assertFalse(TermsWebPolicy.isAllowedNavigation(termsUrl, null))
        assertFalse(TermsWebPolicy.isAllowedNavigation(termsUrl, "not a url"))
        assertFalse(TermsWebPolicy.isAllowedNavigation("http://terms.example.com", "http://terms.example.com"))
    }
}
