package com.redapps.tabib.cache

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.redapps.tabib.model.Medicament
import java.util.*

class Converters {

    @TypeConverter
    fun longToDate(long: Long?): Date? {
        return long?.let { Date(it) }
    }

    @TypeConverter
    fun dateToLong(date: Date?): Long? {
        return date?.time
    }


    @TypeConverter
    fun medicamentToString(medic: Medicament?): String? {
        return Gson().toJson(medic)
    }

    @TypeConverter
    fun stringToMedicament(string: String?): Medicament? {
        return Gson().fromJson(string, Medicament::class.java)
    }

    @TypeConverter
    fun medicamentListToString(medic: List<Medicament>?): String? {
        return Gson().toJson(medic)
    }

    @TypeConverter
    fun stringToMedicamentList(string: String?): List<Medicament>? {
        val type = object : TypeToken<List<Medicament>>() {}.type
        return Gson().fromJson(string, type)
    }
}