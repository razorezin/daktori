package com.redapps.tabib.utils

import android.content.Context
import android.widget.Toast
import com.redapps.tabib.model.User
import retrofit2.Callback

object ToastUtils {

    private var toast: Toast? = null

    fun longToast(context: Context, message: String?){
        try {
            if (toast != null){
                toast!!.cancel()
            }
            toast = Toast.makeText(context, message, Toast.LENGTH_LONG)
            toast!!.show()
        } catch (e: Exception) {
        }
    }

}