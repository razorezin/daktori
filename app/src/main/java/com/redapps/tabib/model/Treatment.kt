package com.redapps.tabib.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*
import kotlin.collections.ArrayList

@Entity(tableName = "treatments")
data class Treatment(
    @PrimaryKey @SerializedName("idTreatment") var idTreatment: Int,
    @SerializedName("idDoc") var idDoc: Int,
    @SerializedName("idPatient") var idPatient: Int,
    @SerializedName("durationTreatment") var durationTreatment: String,
    @SerializedName("dateStartTreatment") var dateStartTreatment: Date,
    @SerializedName("medicamentList") var medicamentList: List<Medicament>
)
