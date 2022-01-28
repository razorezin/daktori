package com.redapps.tabib.model

import com.google.gson.annotations.SerializedName

data class BookingFetch(
    @SerializedName("idDoc") var idDoc: Int,
    @SerializedName("date") var date: String,
)
