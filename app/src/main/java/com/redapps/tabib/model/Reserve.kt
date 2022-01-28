package com.redapps.tabib.model

import com.google.gson.annotations.SerializedName

data class Reserve(
    @SerializedName("idDoc") var idDoc: Int,
    @SerializedName("idPatient") var idPatient: Int,
    @SerializedName("date") var date: String
)
