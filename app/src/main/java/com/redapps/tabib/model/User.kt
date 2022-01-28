package com.redapps.tabib.model

data class User(
    var id : Int,
    var name : String,
    var surname : String,
    var phone : String,
    val startHour: String,
    val endHour: String,
    val docSpeciality: String,
    val docAdrLongi: String,
    val docAdrLati: String
) {
    companion object{
        val DOCTOR = 0
        val PATIENT = 1
    }

}
