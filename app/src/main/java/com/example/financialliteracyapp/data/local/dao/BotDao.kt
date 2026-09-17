package com.example.financialliteracyapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.financialliteracyapp.data.local.entity.BotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BotDao {
    @Query("SELECT * FROM bots")
    fun observeAll(): Flow<List<BotEntity>>

    @Query("SELECT * FROM bots")
    suspend fun getAllOnce(): List<BotEntity>

    @Query("SELECT COUNT(*) FROM bots")
    suspend fun count(): Int

    @Query("SELECT * FROM bots WHERE workBuildingId = :buildingId")
    fun observeByWorkplace(buildingId: Long): Flow<List<BotEntity>>

    @Query("SELECT * FROM bots WHERE homeBuildingId = :buildingId")
    suspend fun getByHomeBuilding(buildingId: Long): List<BotEntity>

    @Query("SELECT * FROM bots WHERE state IN ('HOME', 'MOVING_TO_WORK') AND workBuildingId = :buildingId")
    suspend fun getWorkersForBuilding(buildingId: Long): List<BotEntity>

    @Query("SELECT * FROM bots WHERE hasCar = 1")
    suspend fun getCarOwners(): List<BotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(bots: List<BotEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(bot: BotEntity)

    @Query("DELETE FROM bots")
    suspend fun clear()
}