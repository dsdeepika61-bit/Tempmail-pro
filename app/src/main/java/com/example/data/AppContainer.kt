package com.example.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.example.api.MailTmApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val Context.dataStore by preferencesDataStore(name = "tempmail_settings")

class AppContainer(private val context: Context) {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.mail.tm/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val mailTmApi: MailTmApi by lazy {
        retrofit.create(MailTmApi::class.java)
    }

    val authManager: AuthManager by lazy {
        AuthManager(context.dataStore)
    }

    val emailRepository: EmailRepository by lazy {
        EmailRepository(mailTmApi, authManager)
    }
}
