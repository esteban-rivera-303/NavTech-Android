package com.zanaaustralis.feature_kyc.di


import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.zanaaustralis.core.data.remote.TokenInterceptor
import com.zanaaustralis.core.data.remote.secureHttpLoggingInterceptor
import com.zanaaustralis.core.secure.device.DeviceIdentityManager
import com.zanaaustralis.feature_kyc.data.IdentityValidationRepository
import com.zanaaustralis.feature_kyc.data.KycRepository
import com.zanaaustralis.feature_kyc.data.remote.IdentityValidationApi
import com.zanaaustralis.feature_kyc.data.remote.KycApi
import com.zanaaustralis.feature_kyc.domain.KycUseCases
import com.zanaaustralis.feature_kyc.domain.StartRestorePinValidationUseCase
import com.zanaaustralis.feature_kyc.domain.VerifyRestorePinUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


/**
 * @author esteban.rivera01
 */
@Module
@InstallIn(SingletonComponent::class)
object KycModule {

    private const val BASE_URL =
        "https://njhhvsmhhpdxmlmcsoao.supabase.co/functions/v1/mobile-api/"

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    fun provideKycApi(): KycApi {

        val logging = secureHttpLoggingInterceptor()

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(KycApi::class.java)
    }

    @Provides
    fun provideKycRepository(api: KycApi): KycRepository = KycRepository(api)

    @Provides
    fun provideKycUseCases(repo: KycRepository): KycUseCases = KycUseCases(repo)

    @Provides
    fun provideIdentityValidationApi(tokenInterceptor: TokenInterceptor): IdentityValidationApi {

        val logging = secureHttpLoggingInterceptor()

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(tokenInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(IdentityValidationApi::class.java)
    }


    @Provides
    fun provideIdentityValidationRepository(api: IdentityValidationApi): IdentityValidationRepository = IdentityValidationRepository(api)

    @Provides
    fun provideStartRestorePinValidationUseCase(repo: IdentityValidationRepository,  deviceIdentity: DeviceIdentityManager): StartRestorePinValidationUseCase =
        StartRestorePinValidationUseCase(repo,deviceIdentity)
    fun provideVerifyRestorePinUseCase(repo: IdentityValidationRepository,  deviceIdentity: DeviceIdentityManager): VerifyRestorePinUseCase =
        VerifyRestorePinUseCase(repo,deviceIdentity)

}