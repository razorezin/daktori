package com.redapps.tabib.ui.treatment

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.impl.utils.futures.SettableFuture
import com.clovertech.autolib.utils.PrefUtils
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import com.redapps.tabib.model.Advice
import com.redapps.tabib.model.Message
import com.redapps.tabib.network.PatientApiClient
import com.redapps.tabib.utils.ToastUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@SuppressLint("RestrictedApi")
class AdviceWorker(val context: Context, val workParams: WorkerParameters)
    : ListenableWorker(context,workParams) {

    lateinit var future : SettableFuture<Result>

    override fun startWork(): ListenableFuture<Result> {
        future = SettableFuture.create()

        // Get advice
        val adviceJson = PrefUtils.with(context).getString(PrefUtils.Keys.ADVICE_TO_SEND, "")
        if (adviceJson != ""){
            val advice = Gson().fromJson(adviceJson, Advice::class.java)
            sendAdvice(advice)
        }

        return future
    }

    private fun sendAdvice(advice: Advice) {
        PatientApiClient.instance.sendAdvice(advice).enqueue(object :
            Callback<Message> {
            override fun onResponse(call: Call<Message>, response: Response<Message>) {
                if (response.isSuccessful){
                    ToastUtils.longToast(context, "Advice sent!")
                    PrefUtils.with(context).save(PrefUtils.Keys.ADVICE_TO_SEND, "")
                } else {
                    ToastUtils.longToast(context, "Error while sending advice : " + response.message())
                }
            }

            override fun onFailure(call: Call<Message>, t: Throwable) {
                ToastUtils.longToast(context, "Failed to send advice : " + t.message)
            }
        })
    }


}