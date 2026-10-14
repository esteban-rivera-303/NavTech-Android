package com.zanaaustralis.argos.di

import com.zanaaustralis.argos.navigation.AppNotificationNavigator
import com.zanaaustralis.argos.notification.PushTokenStoreImpl
import com.zanaaustralis.argos.session.AndroidSessionProvider
import com.zanaaustralis.core.notification.NotificationNavigator
import com.zanaaustralis.core.session.SessionManagerImpl
import com.zanaaustralis.core.store.notification.PushTokenStore
import com.zanaaustralis.feature_auth.ChallengeLocalAuthStore
import com.zanaaustralis.feature_auth.data.ChallengeLocalAuthStoreImpl
import com.zanaaustralis.core.session.SessionProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @author esteban.rivera01
 */

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNotificationNavigator(): NotificationNavigator = AppNotificationNavigator()

    @Provides
    @Singleton
    fun provideSessionProvider(sessionManagerImpl: SessionManagerImpl): SessionProvider = AndroidSessionProvider(sessionManagerImpl)

    @Provides
    @Singleton
    fun provideChallengeLocalAuthStore(
        impl: ChallengeLocalAuthStoreImpl
    ): ChallengeLocalAuthStore = impl

    @Provides
    @Singleton
    fun providePushTokenStore(
        impl: PushTokenStoreImpl
    ): PushTokenStore = impl
}