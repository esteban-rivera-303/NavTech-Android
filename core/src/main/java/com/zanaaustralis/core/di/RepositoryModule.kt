package com.zanaaustralis.core.di

import com.zanaaustralis.core.data.local.TotpStorage
import com.zanaaustralis.core.data.remote.AuthApiService
import com.zanaaustralis.core.data.remote.repository.AuthRepositoryImpl
import com.zanaaustralis.core.data.remote.repository.KycRepositoryImpl
import com.zanaaustralis.core.data.remote.repository.NotificationRepositoryImpl
import com.zanaaustralis.core.domain.repository.AccountRecoveryRepository
import com.zanaaustralis.core.domain.repository.AuthRepository
import com.zanaaustralis.core.domain.repository.KycRepository
import com.zanaaustralis.core.domain.repository.NotificationRepository
import com.zanaaustralis.core.secure.crypto.DeviceCryptoSigner
import com.zanaaustralis.core.secure.device.DeviceIdentityManager
import com.zanaaustralis.core.session.SessionInvalidator
import com.zanaaustralis.core.session.SessionInvalidatorImpl
import com.zanaaustralis.core.session.SessionManagerImpl
import com.zanaaustralis.core.session.TokenStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.zanaaustralis.core.data.remote.repository.AccountRecoveryRepositoryImpl as AccountRecoveryRepositoryImpl1

/**
 * @author esteban.rivera01
 */

@Module
@InstallIn(SingletonComponent::class)
object ProvideModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        @AuthenticatedApi api: AuthApiService,
        sessionManagerImpl: SessionManagerImpl,
        deviceIdentity: DeviceIdentityManager,
        tokenStorage: TokenStorage,
        signer: DeviceCryptoSigner,
        totpStorage: TotpStorage,
    ): AuthRepository = AuthRepositoryImpl(api, sessionManagerImpl, deviceIdentity, tokenStorage,signer,totpStorage)

    @Provides
    @Singleton
    fun provideKycRepository(): KycRepository = KycRepositoryImpl()

    @Provides
    @Singleton
    fun provideNotificationRepository( @AuthenticatedApi api: AuthApiService,): NotificationRepository = NotificationRepositoryImpl(api)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BindModule {

    @Binds
    @Singleton
    abstract fun bindAccountRecoveryRepository(
        impl: AccountRecoveryRepositoryImpl1
    ): AccountRecoveryRepository


    @Binds
    abstract fun bindSessionInvalidator(
        impl: SessionInvalidatorImpl
    ): SessionInvalidator
}