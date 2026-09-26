package com.example.financialliteracyapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.financialliteracyapp.data.local.entity.PeriodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PeriodDao {
    @Query("SELECT * FROM periods ORDER BY day ASC")
    fun observeAll(): Flow<List<PeriodEntity>>

    @Query("SELECT * FROM periods ORDER BY day DESC LIMIT 1")
    suspend fun last(): PeriodEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(period: PeriodEntity)

    @Query("SELECT COUNT(*) FROM periods WHERE success = 1")
    suspend fun countSuccessful(): Int
}