package com.zanaaustralis.argos.session

import com.zanaaustralis.core.session.SessionManagerImpl
import com.zanaaustralis.core.session.SessionProvider
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

class AndroidSessionProvider @Inject constructor(
    private val sessionManagerImpl: SessionManagerImpl
) : SessionProvider {

    override suspend fun getAccessToken(): String? {
        return sessionManagerImpl.getSession()?.accessToken
    }
}