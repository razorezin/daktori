package com.redapps.tabib.network

import com.redapps.tabib.model.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PatientApiService {

    @GET("appointment/{idPatient}")
    fun getAppointmentsByPatient(@Path("idPatient") idPatient: Int): Call<List<Appointment>>

    @GET("treatment/{idPatient}")
    fun getTreatmentsByPatientId(@Path("idPatient") idPatient: Int): Call<List<Treatment>>

    @POST("advice")
    fun sendAdvice(@Body advice: Advice): Call<Message>

}