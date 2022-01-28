package com.redapps.tabib.repository

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.redapps.tabib.cache.CacheDB
import com.redapps.tabib.model.Doctor
import com.redapps.tabib.model.Treatment
import com.redapps.tabib.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PatientRepository {

    fun insertTreatment(context: Context, treatment: Treatment){
        CoroutineScope(Dispatchers.IO).launch {
            CacheDB.getDatabase(context).treatmentDAO().insertTreatment(treatment)
        }
    }

    fun clearUserCache(context: Context){
        CoroutineScope(Dispatchers.IO).launch {
            CacheDB.getDatabase(context).treatmentDAO().clearTreatments()
        }
    }

    fun getTreatments(context: Context): LiveData<List<Treatment>> {
        return CacheDB.getDatabase(context).treatmentDAO().getAllTreatments().asLiveData()
    }

    companion object {
        private var INSTANCE: PatientRepository? = null
        fun getInstance() = INSTANCE
            ?: PatientRepository().also {
                INSTANCE = it
            }
    }

}