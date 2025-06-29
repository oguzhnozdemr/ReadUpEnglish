package com.oguzhanozdemir.readupenglish.di

import android.content.Context
import androidx.room.Room
import com.oguzhanozdemir.readupenglish.database.AppDatabase
import com.oguzhanozdemir.readupenglish.database.BookDao
import com.oguzhanozdemir.readupenglish.database.BookmarkDao
import com.oguzhanozdemir.readupenglish.database.BookContentDao
import com.oguzhanozdemir.readupenglish.database.FavoriteBookDao
import com.oguzhanozdemir.readupenglish.database.WordDao
import com.oguzhanozdemir.readupenglish.service.BookApiService
import com.oguzhanozdemir.readupenglish.service.TranslationApiService
import com.oguzhanozdemir.readupenglish.cache.BookContentCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Network Service for Book API
    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            // Timeout optimizations for faster loading
            .connectTimeout(10, TimeUnit.SECONDS)      // Connect timeout
            .readTimeout(60, TimeUnit.SECONDS)         // Read timeout for large files
            .writeTimeout(30, TimeUnit.SECONDS)        // Write timeout
            .callTimeout(90, TimeUnit.SECONDS)         // Total call timeout
            
            // Connection pool for better performance
            .connectionPool(ConnectionPool(10, 5, TimeUnit.MINUTES))
            
            // Retry on connection failure
            .retryOnConnectionFailure(true)
            
            // Add compression support
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestWithHeaders = originalRequest.newBuilder()
                    .addHeader("Accept-Encoding", "gzip, deflate")
                    .addHeader("Connection", "keep-alive")
                    .addHeader("User-Agent", "ReadUpEnglish/1.0 (Android)")
                    .build()
                chain.proceed(requestWithHeaders)
            }
            
            // Add logging for debugging (remove in production)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            
            .build()

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://gutendex.com/")           // Kitap API için base URL
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())  // Metin için
            .addConverterFactory(GsonConverterFactory.create())     // JSON için
            .build()

    @Provides @Singleton
    fun provideBookApiService(retrofit: Retrofit): BookApiService =
        retrofit.create(BookApiService::class.java)
    @Provides
    fun provideFavoriteBookDao(db: AppDatabase): FavoriteBookDao {
        return db.favoriteBookDao()
    }
    // Translation API Service
    @Provides
    fun provideBaseUrl() = "https://translation.googleapis.com/"

    @Provides
    @Singleton
    fun provideTranslateApi(baseUrl: String): TranslationApiService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TranslationApiService::class.java)
    }

    // Database & DAO
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "readup_database"  // Veritabanı adı
        ).fallbackToDestructiveMigration()  // Migrasyon hatalarını yok say
            .build()
    }

    @Provides @Singleton
    fun provideBookDao(appDatabase: AppDatabase): BookDao = appDatabase.bookDao()

    @Provides @Singleton
    fun provideWordDao(appDatabase: AppDatabase): WordDao = appDatabase.wordDao()

    @Provides @Singleton
    fun provideBookmarkDao(appDatabase: AppDatabase): BookmarkDao = appDatabase.bookmarkDao()

    @Provides @Singleton
    fun provideBookContentDao(appDatabase: AppDatabase): BookContentDao = appDatabase.bookContentDao()

    @Provides @Singleton
    fun provideBookContentCache(bookContentDao: BookContentDao): BookContentCache {
        return BookContentCache(bookContentDao)
    }
}