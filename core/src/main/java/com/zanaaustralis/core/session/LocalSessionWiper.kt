package com.zanaaustralis.core.session

/**
 * @author esteban.rivera01
 */

interface LocalSessionWiper {
    suspend fun wipe()
}