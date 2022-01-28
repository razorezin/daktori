package com.redapps.tabib.network

import com.redapps.tabib.model.Doctor
import com.redapps.tabib.model.NotificationToken
import com.redapps.tabib.model.User
import com.redapps.tabib.model.UserLogin
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface AuthApiService {

    @POST("auth/login")
    fun login(@Body userLogin: RequestBody): Call<User>

    @POST("notification")
    fun sendToken(@Body token: NotificationToken): Call<NotificationToken>

}