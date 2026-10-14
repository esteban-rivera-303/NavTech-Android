package com.zanaaustralis.core.session

/**
 * @author esteban.rivera01
 */

interface SessionProvider {
    suspend fun getAccessToken(): String?
}