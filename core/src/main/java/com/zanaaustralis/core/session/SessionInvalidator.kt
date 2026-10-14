package com.zanaaustralis.core.session

/**
 * @author esteban.rivera01
 */

interface SessionInvalidator {

    suspend fun updateLastActivity()
    suspend fun invalidateSession()
}