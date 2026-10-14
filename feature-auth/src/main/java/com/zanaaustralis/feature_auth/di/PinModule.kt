package com.zanaaustralis.feature_auth.di

import com.zanaaustralis.feature_auth.data.PinRepository
import com.zanaaustralis.feature_auth.data.PinRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * @author esteban.rivera01
 */

@Module
@InstallIn( SingletonComponent::class)
abstract class PinModule {

    @Binds
    abstract fun bindPinRepository(
        impl: PinRepositoryImpl
    ): PinRepository
}
