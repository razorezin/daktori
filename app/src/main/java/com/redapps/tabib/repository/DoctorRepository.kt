package com.redapps.tabib.repository

import com.redapps.tabib.model.Doctor
import com.redapps.tabib.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DoctorRepository {

    fun getDoctors(onResult: (isSuccess: Boolean, response: Doctor?) -> Unit) {

        ApiClient.instance.getDoctors().enqueue(object : Callback<Doctor> {
            override fun onResponse(call: Call<Doctor>?, response: Response<Doctor>?) {
                if (response != null && response.isSuccessful)
                    onResult(true, response.body()!!)
                else
                    onResult(false, null)
            }

            override fun onFailure(call: Call<Doctor>?, t: Throwable?) {
                onResult(false, null)
            }

        })
    }

    companion object {
        private var INSTANCE: DoctorRepository? = null
        fun getInstance() = INSTANCE
            ?: DoctorRepository().also {
                INSTANCE = it
            }
    }

}