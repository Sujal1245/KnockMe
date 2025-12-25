package com.sujalkumar.knockme.di

import androidx.credentials.CredentialManager
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sujalkumar.knockme.data.datastore.AppUserSerializer
import com.sujalkumar.knockme.data.datasource.UserDataSource
import com.sujalkumar.knockme.data.model.AppUser
import com.sujalkumar.knockme.domain.repository.AuthRepository
import com.sujalkumar.knockme.domain.repository.KnockAlertRepository
import com.sujalkumar.knockme.domain.repository.OtherUsersRepository
import com.sujalkumar.knockme.domain.repository.UserDetailsRepository
import com.sujalkumar.knockme.data.repository.UserDetailsRepositoryImpl
import com.sujalkumar.knockme.data.repository.AuthRepositoryImpl
import com.sujalkumar.knockme.data.repository.OtherUsersRepositoryImpl
import com.sujalkumar.knockme.data.repository.KnockAlertRepositoryImpl
import com.sujalkumar.knockme.ui.addalert.AddKnockAlertViewModel
import com.sujalkumar.knockme.ui.auth.AuthViewModel
import com.sujalkumar.knockme.ui.home.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModelOf
import java.io.File

private const val USER_DATA_STORE_FILE_NAME = "app_user.pb"

val appModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::AddKnockAlertViewModel)

    single<DataStore<AppUser?>> {
        DataStoreFactory.create(
            serializer = AppUserSerializer,
            produceFile = {
                val dir = File(androidContext().filesDir, "datastore")
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                File(dir, USER_DATA_STORE_FILE_NAME)
            }
        )
    }

    single { CredentialManager.create(androidContext()) }
    single<FirebaseAuth> { FirebaseAuth.getInstance() }
    single<FirebaseFirestore> { FirebaseFirestore.getInstance() }

    singleOf(::UserDataSource)
    singleOf(::UserDetailsRepositoryImpl) { bind<UserDetailsRepository>() }
    singleOf(::KnockAlertRepositoryImpl) { bind<KnockAlertRepository>() }
    singleOf(::OtherUsersRepositoryImpl) { bind<OtherUsersRepository>() }
    singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }
}