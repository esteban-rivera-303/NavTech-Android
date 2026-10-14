package com.zanaaustralis.core.store.notification

/**
 * @author esteban.rivera01
 */


interface PushTokenStore {
    suspend fun save(token: String)
    suspend fun get(): String?
    suspend fun clear()
}
