package com.voxai.di

import android.content.Context
import androidx.room.Room
import com.voxai.BuildConfig
import com.voxai.data.local.ChatMessageDao
import com.voxai.data.local.VoxAIDatabase
import com.voxai.data.remote.OpenAIApiService
import com.voxai.data.repository.ChatRepositoryImpl
import com.voxai.domain.repository.ChatRepository
import com.voxai.util.audio.AudioPlayer
import com.voxai.util.audio.AudioRecorder
import com.voxai.util.audio.VoiceEffectProcessor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideVoxAIDatabase(@ApplicationContext context: Context): VoxAIDatabase {
        return Room.databaseBuilder(
            context,
            VoxAIDatabase::class.java,
            "voxai_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideChatMessageDao(database: VoxAIDatabase): ChatMessageDao {
        return database.chatMessageDao()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val apiKey = BuildConfig.OPENAI_API_KEY.ifEmpty { 
                "YOUR_API_KEY_HERE"
            }
            
            val request = original.newBuilder()
                .header("Authorization", "Bearer $apiKey")
                .method(original.method, original.body)
                .build()
            
            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenAIApiService(okHttpClient: OkHttpClient): OpenAIApiService {
        return Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAIApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        apiService: OpenAIApiService,
        chatMessageDao: ChatMessageDao
    ): ChatRepository {
        return ChatRepositoryImpl(apiService, chatMessageDao)
    }

    @Provides
    @Singleton
    fun provideAudioRecorder(): AudioRecorder {
        return AudioRecorder()
    }

    @Provides
    @Singleton
    fun provideAudioPlayer(): AudioPlayer {
        return AudioPlayer()
    }

    @Provides
    @Singleton
    fun provideVoiceEffectProcessor(): VoiceEffectProcessor {
        return VoiceEffectProcessor()
    }
}
