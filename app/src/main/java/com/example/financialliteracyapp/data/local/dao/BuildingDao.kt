package com.example.financialliteracyapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.financialliteracyapp.data.local.entity.BuildingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildingDao {
    @Query("SELECT * FROM buildings")
    fun observeAll(): Flow<List<BuildingEntity>>

    @Query("SELECT * FROM buildings WHERE id = :id")
    fun observeById(id: Long): Flow<BuildingEntity?>

    @Query("SELECT * FROM buildings WHERE district = :district")
    fun observeByDistrict(district: String): Flow<List<BuildingEntity>>

    @Query("SELECT * FROM buildings WHERE type = :type AND district = :district")
    suspend fun getByTypeAndDistrict(type: String, district: String): BuildingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(building: BuildingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(buildings: List<BuildingEntity>)

    @Query("SELECT * FROM buildings WHERE plotId = :plotId LIMIT 1")
    suspend fun getByPlotId(plotId: String): BuildingEntity?

    @Query("DELETE FROM buildings WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM buildings WHERE plotId = :plotId")
    suspend fun deleteByPlotId(plotId: String)

    @Query("DELETE FROM buildings")
    suspend fun clear()
}