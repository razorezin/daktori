package com.redapps.tabib.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.redapps.tabib.model.Treatment
import kotlinx.coroutines.flow.Flow

@Dao
interface TreatmentDAO {

    @Query("SELECT * FROM treatments")
    fun getAllTreatments(): Flow<List<Treatment>>

    @Query("DELETE FROM treatments")
    fun clearTreatments()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTreatment(treatment: Treatment)

}