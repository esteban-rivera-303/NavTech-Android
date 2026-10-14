package com.zanaaustralis.core.di

import android.content.Context
import com.zanaaustralis.core.data.local.TotpStorage
import com.zanaaustralis.core.device_secure.EmulatorDetector
import com.zanaaustralis.core.device_secure.HookDetector
import com.zanaaustralis.core.device_secure.RootDetector
import com.zanaaustralis.core.device_secure.SecurityManagerNavtech
import com.zanaaustralis.core.domain.repository.AccountRecoveryRepository
import com.zanaaustralis.core.domain.repository.AuthRepository
import com.zanaaustralis.core.domain.usescases.AuthUseCases
import com.zanaaustralis.core.domain.usescases.LoginUserUseCase
import com.zanaaustralis.core.domain.usescases.OnboardingUseCase
import com.zanaaustralis.core.domain.usescases.RegisterUserUseCase
import com.zanaaustralis.core.domain.usescases.RequestCodeUseCase
import com.zanaaustralis.core.domain.usescases.UserStatusUseCase
import com.zanaaustralis.core.domain.usescases.VerifyCodeUseCase
import com.zanaaustralis.core.domain.usescases.recovery.ReactivateTrustedDeviceUseCase
import com.zanaaustralis.core.notification.NotificationHelper
import com.zanaaustralis.core.notification.NotificationNavigator
import com.zanaaustralis.core.secure.SecureStorageManager
import com.zanaaustralis.core.secure.crypto.AndroidKeyStoreManager
import com.zanaaustralis.core.secure.crypto.UserAuthKeyStore
import com.zanaaustralis.core.secure.crypto.CryptoManager
import com.zanaaustralis.core.secure.device.DeviceIdentityManager
import com.zanaaustralis.core.secure.util.CryptoHelper
import com.zanaaustralis.core.session.SessionManagerImpl
import com.zanaaustralis.core.store.RecoveryStateStorage
import com.zanaaustralis.core.store.notification.PushTokenStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @author esteban.rivera01
 */

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideAuthUseCases(
        androidKeyStoreManager: AndroidKeyStoreManager,
        deviceIdentityManager: DeviceIdentityManager,
        pushTokenStore: PushTokenStore,
        repository: AuthRepository,
        userAuthKeyStore: UserAuthKeyStore
    ): AuthUseCases {
        return AuthUseCases(
            loginUserUseCase = LoginUserUseCase(repository),
            registerUser = RegisterUserUseCase(repository),
            verifyCode = VerifyCodeUseCase(repository),
            onboarding = OnboardingUseCase(androidKeyStoreManager, deviceIdentityManager, pushTokenStore, repository, userAuthKeyStore),
            userStatus = UserStatusUseCase(repository),
            requestUseCase = RequestCodeUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun provideReactivateTrustedDeviceUseCase(
        repository: AccountRecoveryRepository,
        sessionManagerImpl: SessionManagerImpl,
        androidKeyStoreManager: AndroidKeyStoreManager,
        userAuthKeyStore: UserAuthKeyStore
    ): ReactivateTrustedDeviceUseCase {
        return ReactivateTrustedDeviceUseCase(
            repository, sessionManagerImpl, androidKeyStoreManager, userAuthKeyStore
        )
    }

    @Provides
    @Singleton
    fun provideSecureStorageManager(
        @ApplicationContext context: Context
    ): SecureStorageManager = SecureStorageManager(context)

    @Provides
    @Singleton
    fun provideRecoveryNotificationHelper(
        @ApplicationContext context: Context,
        notificationNavigator: NotificationNavigator
    ): NotificationHelper = NotificationHelper(context, notificationNavigator)

    @Provides
    @Singleton
    fun provideRecoveryStateStorage(
        @ApplicationContext context: Context,
        cryptoManager: CryptoManager
    ): RecoveryStateStorage = RecoveryStateStorage(context, cryptoManager)

    @Provides
    @Singleton
    fun provideCryptoHelper(): CryptoHelper = CryptoHelper

    @Provides
    @Singleton
    fun provideTotpStorage(cryptoManager: CryptoManager): TotpStorage = TotpStorage(cryptoManager)

    @Provides
    @Singleton
    fun provideAndroidKeyStoreManager(): AndroidKeyStoreManager = AndroidKeyStoreManager()

    @Provides
    @Singleton
    fun provideRootDetector(@ApplicationContext context: Context): RootDetector = RootDetector(context)

    @Provides
    @Singleton
    fun provideEmulatorDetector(): EmulatorDetector = EmulatorDetector()

    @Provides
    @Singleton
    fun provideHookDetector(): HookDetector = HookDetector()


    @Provides
    @Singleton
    fun provideSecurityManager(
        rootDetector: RootDetector,
        emulatorDetector: EmulatorDetector,
        hookDetector: HookDetector): SecurityManagerNavtech = SecurityManagerNavtech(rootDetector,emulatorDetector,hookDetector)



}