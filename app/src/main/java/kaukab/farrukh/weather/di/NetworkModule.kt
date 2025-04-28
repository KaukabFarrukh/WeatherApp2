package kaukab.farrukh.weather.di

import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kaukab.farrukh.weather.BuildConfig
import kaukab.farrukh.weather.data.network.WeatherApi
import kaukab.farrukh.weather.utils.BASE_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

import kaukab.farrukh.weather.data.network.ChatApiService
import kaukab.farrukh.weather.utils.BASE_URL_OPENAI


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    @Singleton
    @Provides
    fun provideOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        val okHttpClient = OkHttpClient.Builder()
        return if (BuildConfig.DEBUG) {
            okHttpClient.addInterceptor(httpLoggingInterceptor).build()
        } else {
            okHttpClient.build()
        }
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .client(okHttpClient)
        .build()

    @Singleton
    @Provides
    fun provideWeatherApi(retrofit: Retrofit): WeatherApi = retrofit
        .create(WeatherApi::class.java)


    @Singleton
    @Provides
    fun provideChatApiService(): ChatApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()

                println("🔗 Request URL: ${originalRequest.url}") // 👈 Add this line

                val newRequest = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
                    .build()
                chain.proceed(newRequest)
            }
            .build()


        return Retrofit.Builder()
            .baseUrl(BASE_URL_OPENAI)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ChatApiService::class.java)
    }


}