package com.redapps.tabib.model

import com.google.gson.annotations.SerializedName

data class Doctor(
    @SerializedName("id") var id: Int,
    @SerializedName("surname") var firstName: String,
    @SerializedName("name") var lastName: String,
    @SerializedName("docPicUrl") var photo: String,
    @SerializedName("phone") var phone: String,
    @SerializedName("docSpeciality") var speciality: String,
    @SerializedName("docAdrLati") var latitude: Double,
    @SerializedName("docAdrLongi") var longitude: Double,
    @SerializedName("startHour") var startHour: String,
    @SerializedName("endHour") var endHour: String
)
