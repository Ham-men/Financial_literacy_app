package com.example.financialliteracyapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.financialliteracyapp.data.local.entity.CatalogItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogItemDao {
    @Query("SELECT * FROM catalog_items ORDER BY `order` ASC")
    fun observeAll(): Flow<List<CatalogItemEntity>>

    @Query("SELECT * FROM catalog_items WHERE category = :category ORDER BY `order` ASC")
    fun observeByCategory(category: String): Flow<List<CatalogItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CatalogItemEntity>)

    @Query("DELETE FROM catalog_items")
    suspend fun clear()
}