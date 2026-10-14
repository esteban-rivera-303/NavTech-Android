package com.zanaaustralis.core.session

import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

class SessionInvalidatorImpl @Inject constructor(
    private val sessionManager: SessionManagerImpl
) : SessionInvalidator {

    override suspend fun invalidateSession() {
        sessionManager.onInvalidSessionDetected()
    }

    override suspend fun updateLastActivity() {
        sessionManager.updateLastActivity()
    }
}