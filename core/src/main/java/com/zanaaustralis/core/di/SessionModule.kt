package com.zanaaustralis.core.di

/**
 * @author esteban.rivera01
 */

import android.content.Context
import com.zanaaustralis.core.secure.crypto.CryptoManager
import com.zanaaustralis.core.secure.device.DeviceIdentityManager
import com.zanaaustralis.core.session.SessionManagerImpl
import com.zanaaustralis.core.session.TokenStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SessionModule {

    @Provides
    @Singleton
    fun provideCryptoManager(@ApplicationContext context: Context): CryptoManager {
        return CryptoManager(context)
    }
 @Provides
    @Singleton
    fun provideTokenStorage(@ApplicationContext context: Context, cryptoManager: CryptoManager): TokenStorage {
        return TokenStorage(context, cryptoManager)
    }

    @Provides
    @Singleton
    fun provideSessionManager(tokenStorage: TokenStorage): SessionManagerImpl {
        return SessionManagerImpl(tokenStorage)
    }

    @Provides
    @Singleton
    fun provideDeviceIdentityManager(@ApplicationContext context: Context, cryptoManager: CryptoManager): DeviceIdentityManager {
        return DeviceIdentityManager(context, cryptoManager)
    }

}