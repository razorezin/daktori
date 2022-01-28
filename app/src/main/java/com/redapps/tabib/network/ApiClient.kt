package com.redapps.tabib.network

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object ApiClient {

    private const val BASE_URL = "https://8fef409a9816.ngrok.io"

    val instance: ApiService = Retrofit.Builder().run {

        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        val gson = GsonBuilder()
            .enableComplexMapKeySerialization()
            .setPrettyPrinting()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create()

        baseUrl(BASE_URL)
        addConverterFactory(GsonConverterFactory.create(gson))
        client(client)
        build()
    }.create(ApiService::class.java)

}