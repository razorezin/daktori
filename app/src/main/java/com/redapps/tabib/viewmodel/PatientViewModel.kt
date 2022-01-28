package com.redapps.tabib.viewmodel

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.redapps.tabib.cache.CacheDB
import com.redapps.tabib.model.Appointment
import com.redapps.tabib.model.Doctor
import com.redapps.tabib.model.Treatment
import com.redapps.tabib.network.DoctorApiClient
import com.redapps.tabib.network.PatientApiClient
import com.redapps.tabib.repository.PatientRepository
import com.redapps.tabib.utils.ToastUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PatientViewModel: BaseViewModel() {

    val appointments = MutableLiveData<List<Appointment>>()
    val treatments = MutableLiveData<List<Treatment>>()

    fun fetchAppointments(idPatient: Int){
        dataLoading.value = true
        PatientApiClient.instance.getAppointmentsByPatient(idPatient).enqueue(object : Callback<List<Appointment>> {
            override fun onResponse(call: Call<List<Appointment>>, response: Response<List<Appointment>>) {
                dataLoading.value = false
                if (response.isSuccessful){
                    appointments.apply {
                        value = response.body()!!
                    }
                    empty.value = response.body()!!.isEmpty()
                    failed.value = false
                } else {
                    toastMessage.value = "Error  : " + response.message()
                    failed.value = true
                    empty.value = false
                }
            }

            override fun onFailure(call: Call<List<Appointment>>, t: Throwable) {
                toastMessage.value =  "Failed : " + t.message
                dataLoading.value =  false
                failed.value = true
                empty.value = false
            }
        })
    }

    fun getTreatments(context: Context): LiveData<List<Treatment>>{
        return PatientRepository.getInstance().getTreatments(context)
    }

    fun insertTreatment(context: Context, treatment: Treatment){
        PatientRepository.getInstance().insertTreatment(context, treatment)
    }

    fun clearUserCache(context: Context){
        PatientRepository.getInstance().clearUserCache(context)
    }

    fun fetchTreatments(context: Context, idPatient: Int){
        dataLoading.value = true
        PatientApiClient.instance.getTreatmentsByPatientId(idPatient).enqueue(object : Callback<List<Treatment>> {
            override fun onResponse(call: Call<List<Treatment>>, response: Response<List<Treatment>>) {
                dataLoading.value = false
                if (response.isSuccessful){
                    for (treatment in response.body()!!){
                        PatientRepository.getInstance().insertTreatment(context, treatment)
                    }
                    //treatments.value = response.body()!!
                    empty.value = response.body()!!.isEmpty()
                    failed.value = false
                } else {
                    toastMessage.value = "Error  : " + response.message()
                    //failed.value = true
                    //empty.value = false
                }
            }

            override fun onFailure(call: Call<List<Treatment>>, t: Throwable) {
                toastMessage.value =  "Failed : " + t.message
                dataLoading.value =  false
                //failed.value = true
                //empty.value = false
            }
        })
    }
}