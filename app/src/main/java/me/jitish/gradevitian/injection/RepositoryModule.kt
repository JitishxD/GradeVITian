package me.jitish.gradevitian.injection

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.jitish.gradevitian.data.repository.AuthRepositoryImpl
import me.jitish.gradevitian.data.repository.PreferencesRepositoryImpl
import me.jitish.gradevitian.data.repository.RecordsRepositoryImpl
import me.jitish.gradevitian.domain.repository.AuthRepository
import me.jitish.gradevitian.domain.repository.PreferencesRepository
import me.jitish.gradevitian.domain.repository.RecordsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindRecordsRepository(impl: RecordsRepositoryImpl): RecordsRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository
}

