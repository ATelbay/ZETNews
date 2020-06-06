package com.smqpro.zetnews.model.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.smqpro.zetnews.model.response.Result

@Dao
interface NewsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(result: Result): Long

    @Query("SELECT * FROM news")
    fun selectAll(): LiveData<List<Result>>

    @Delete
    suspend fun deleteResult(result: Result)

}