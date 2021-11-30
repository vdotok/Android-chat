package com.vdotok.network.di.module

import com.vdotok.network.repository.AccountRepository
import com.vdotok.network.network.api.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Singleton
    @Provides
    fun provideAccountRepository(apiService: ApiService) : AccountRepository {
        return AccountRepository(apiService)
    }

}