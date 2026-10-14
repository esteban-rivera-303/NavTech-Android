package com.zanaaustralis.core.di

import com.google.gson.GsonBuilder
import com.zanaaustralis.core.data.remote.AccountRecoveryApi
import com.zanaaustralis.core.data.remote.secureHttpLoggingInterceptor
import com.zanaaustralis.core.data.remote.AuthApiService
import com.zanaaustralis.core.data.remote.SessionValidationInterceptor
import com.zanaaustralis.core.data.remote.TokenAuthenticator
import com.zanaaustralis.core.data.remote.TokenInterceptor
import com.zanaaustralis.core.session.SessionInvalidator
import com.zanaaustralis.core.session.SessionInvalidatorImpl
import com.zanaaustralis.core.session.SessionManagerCached
import com.zanaaustralis.core.session.SessionManagerImpl
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * @author esteban.rivera01
 */

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // TODO #TODO: implementar certificate pinning (OkHttp CertificatePinner o
    //  res/xml/network_security_config.xml) cuando exista el servidor de
    //  producción; el actual (njhhvsmhhpdxmlmcsoao.supabase.co) es de prueba.
    //  Usar pin principal + pin de respaldo y aplicarlo también a los clientes
    //  de KycModule y ChallengeModule.
    private const val BASE_URL =
        "https://njhhvsmhhpdxmlmcsoao.supabase.co/functions/v1/mobile-api/"

    @Provides
    @Singleton
    fun provideRetrofit(
        tokenInterceptor: TokenInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): Retrofit {
        val logging = secureHttpLoggingInterceptor()

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(tokenInterceptor)
            .authenticator(tokenAuthenticator)
            .build()

        val gson = GsonBuilder().create()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @NoAuthApi
    fun provideAuthApiWithoutAuth(tokenInterceptor: TokenInterceptor,): AuthApiService {
        val logging = secureHttpLoggingInterceptor()
        val gson = GsonBuilder().create()
        val client = OkHttpClient.Builder()
            .addInterceptor(tokenInterceptor)
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .client(client)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    @AuthenticatedApi
    fun provideApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideAccountRecoveryApi(retrofit: Retrofit): AccountRecoveryApi =
        retrofit.create(AccountRecoveryApi::class.java)

    @Provides
    @Singleton
    fun provideTokenInterceptor(sessionManagerCached: SessionManagerCached, sessionManager: SessionManagerImpl): TokenInterceptor =
        TokenInterceptor(sessionManagerCached, sessionManager)

    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        @NoAuthApi apiService: dagger.Lazy<AuthApiService>,
        sessionInvalidator: SessionInvalidator,
        sessionManager: SessionManagerImpl
    ): TokenAuthenticator =
        TokenAuthenticator(
            sessionManager = sessionManager,
            sessionInvalidator = sessionInvalidator,
            authApi = apiService
        )


    @Provides
    @Singleton
    fun provideSessionValidationInterceptor(sessionInvalidatorImpl: SessionInvalidatorImpl): SessionValidationInterceptor =
        SessionValidationInterceptor(sessionInvalidatorImpl)

/*
    @Provides
    @Singleton
    fun provideSessionInvalidator(
        impl: SessionInvalidatorImpl
    ): SessionInvalidator = impl
*/

    @Provides
    @Singleton
    fun provideSessionSessionInvalidatorImpl(sessionManagerImpl: SessionManagerImpl): SessionInvalidatorImpl =
        SessionInvalidatorImpl(sessionManagerImpl)
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NoAuthApi