package com.redapps.tabib.viewmodel

import androidx.lifecycle.MutableLiveData
import com.redapps.tabib.model.Appointment
import com.redapps.tabib.model.BookingFetch
import com.redapps.tabib.model.Doctor
import com.redapps.tabib.network.DoctorApiClient
import com.redapps.tabib.utils.ToastUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class DoctorViewModel: BaseViewModel() {

    val doctors = MutableLiveData<List<Doctor>>()
    val appointments = MutableLiveData<List<Appointment>>()

    fun fetchDoctors(){
        dataLoading.value = true
        DoctorApiClient.instance.getDoctors().enqueue(object : Callback<List<Doctor>> {
            override fun onResponse(call: Call<List<Doctor>>, response: Response<List<Doctor>>) {
                dataLoading.value = false
                if (response.isSuccessful){
                    doctors.apply {
                        value = response.body()!!
                    }
                    empty.value = response.body()!!.isEmpty()
                    failed.value = false
                } else {
                    toastMessage.apply {
                        value = "Error  : " + response.message()
                    }
                    empty.value = false
                    failed.value = true
                }
            }

            override fun onFailure(call: Call<List<Doctor>>, t: Throwable) {
                dataLoading.value = false
                toastMessage.value = "Failed : " + t.message
                empty.value = false
                failed.value = true
            }
        })

    }

    fun fetchAppointments(idDoc: Int, date: String){
        dataLoading.value = true
        DoctorApiClient.instance.getAppointments(BookingFetch(idDoc, date)).enqueue(object : Callback<List<Appointment>> {
            override fun onResponse(call: Call<List<Appointment>>, response: Response<List<Appointment>>) {
                dataLoading.value = false
                if (response.isSuccessful){
                    appointments.value = response.body()!!
                    empty.value = appointments.value!!.isEmpty()
                    failed.value = false
                } else {
                    toastMessage.apply {
                        value = "Error  : " + response.message()
                    }
                    empty.value = false
                    failed.value = true
                }
            }

            override fun onFailure(call: Call<List<Appointment>>, t: Throwable) {
                dataLoading.value = false
                toastMessage.value = "Failed : " + t.message
                empty.value = false
                failed.value = true
            }
        })
    }
}