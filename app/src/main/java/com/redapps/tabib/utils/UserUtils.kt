package com.redapps.tabib.utils

import android.content.Context
import com.clovertech.autolib.utils.PrefUtils
import com.google.gson.Gson
import com.redapps.tabib.model.User

object UserUtils {

    fun getCurrentUser(context: Context) : User{
        val userJson = PrefUtils.with(context).getString(PrefUtils.Keys.USER, "")
        return Gson().fromJson(userJson, User::class.java)
    }

}