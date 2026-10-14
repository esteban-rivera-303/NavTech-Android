package com.zanaaustralis.feature_challanges.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.zanaaustralis.core.secure.crypto.DeviceCryptoSigner
import com.zanaaustralis.core.data.remote.secureHttpLoggingInterceptor
import com.zanaaustralis.core.secure.device.DeviceIdentityManager
import com.zanaaustralis.core.session.SessionProvider
import com.zanaaustralis.feature_auth.data.local.TotpGenerator
import com.zanaaustralis.feature_auth.data.local.TotpManager
import com.zanaaustralis.core.data.local.TotpStorage
import com.zanaaustralis.core.data.remote.SessionValidationInterceptor
import com.zanaaustralis.core.data.remote.TokenAuthenticator
import com.zanaaustralis.core.data.remote.TokenInterceptor
import com.zanaaustralis.feature_auth.presentation.totp.ObserveTotpUseCase
import com.zanaaustralis.feature_challanges.data.local.ChallengeLocalDataSource
import com.zanaaustralis.feature_challanges.data.remote.ChallengeApiService
import com.zanaaustralis.feature_challanges.data.repository.ChallengeRepositoryImpl
import com.zanaaustralis.feature_challanges.domain.repository.ChallengeRepository
import com.zanaaustralis.feature_challanges.domain.usecase.ChallengeUseCases
import com.zanaaustralis.feature_challanges.domain.usecase.GetPendingChallengesUseCase
import com.zanaaustralis.feature_challanges.domain.usecase.RespondChallengeUseCase
import com.zanaaustralis.feature_challanges.domain.usecase.ValidateTotpUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * @author esteban.rivera01
 */

@Module
@InstallIn(SingletonComponent::class)
object ChallengeModule {

    @Provides
    @Singleton
    fun provideJson(): Json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
            explicitNulls = false
        }

    @Provides
    @Singleton
    fun provideChallengeUseCases(
        repository: ChallengeRepository,
        signer: DeviceCryptoSigner,
        deviceIdentityManager: DeviceIdentityManager,
        sessionProvider: SessionProvider,
        json: Json
    ): ChallengeUseCases {
        return ChallengeUseCases(
            getPendingChallenges = GetPendingChallengesUseCase(repository),
            respondChallenge = RespondChallengeUseCase(repository, signer, deviceIdentityManager, sessionProvider, json),
            validateTotp = ValidateTotpUseCase(repository)
        )
    }
    private const val BASE_URL =
        "https://njhhvsmhhpdxmlmcsoao.supabase.co/functions/v1/mobile-api/"

    @Provides
    fun provideChallengeApi(tokenInterceptor: TokenInterceptor,
                            sessionValidationInterceptor: SessionValidationInterceptor,
                            tokenAuthenticator: TokenAuthenticator
    ): ChallengeApiService {
            val logging = secureHttpLoggingInterceptor()

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(tokenInterceptor)
                .addInterceptor(sessionValidationInterceptor)
                .authenticator(tokenAuthenticator)
                .build()

            val gson = GsonBuilder().create()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        return retrofit.create(ChallengeApiService::class.java)

    }

    /*@Provides
    fun provideChallengeApi(retrofit: Retrofit): ChallengeApiService =
        retrofit.create(ChallengeApiService::class.java)*/


    @Provides
    @Singleton
    fun provideChallengeLocalDataSource(@ApplicationContext context: Context): ChallengeLocalDataSource =
        ChallengeLocalDataSource(context, Gson())

    @Provides
    @Singleton
    fun provideChallengeRepository(
        api: ChallengeApiService,
        localDataSource: ChallengeLocalDataSource
    ): ChallengeRepository =
        ChallengeRepositoryImpl(api, localDataSource)

    // TOTP
    @Provides
    @Singleton
    fun provideObserveTotpUseCase(
        totpManager: TotpManager
    ): ObserveTotpUseCase {
        return ObserveTotpUseCase(
            totpManager = totpManager
        )
    }
    @Provides
    @Singleton
    fun provideTotpManager(
        totpStorage: TotpStorage,
        generator: TotpGenerator
    ): TotpManager {
        return TotpManager(
            storage = totpStorage,
            generator = generator
        )
    }
}
