package com.redapps.tabib.network

import com.google.gson.GsonBuilder
import com.redapps.tabib.utils.AppConstants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object DoctorApiClient {


    val instance: DoctorApiService = Retrofit.Builder().run {

        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        val gson = GsonBuilder()
            .setLenient()
            .enableComplexMapKeySerialization()
            .setPrettyPrinting()
            .setDateFormat(AppConstants.DATE_FORMAT)
            .create()

        baseUrl(AppConstants.BASE_URL)
        addConverterFactory(GsonConverterFactory.create(gson))
        client(client)
        build()
    }.create(DoctorApiService::class.java)

}