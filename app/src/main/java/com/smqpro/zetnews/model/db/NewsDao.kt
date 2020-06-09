package com.smqpro.zetnews.model.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.smqpro.zetnews.model.response.Result

@Dao
interface NewsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(result: Result): Long

    @Query("SELECT * FROM news WHERE cache = 1 ORDER BY webPublicationDate DESC")
    suspend fun selectCached(): List<Result>

    @Query("SELECT * FROM news WHERE liked = 1 ORDER BY updatedAt DESC")
    fun selectLiked(): LiveData<List<Result>>

    @Delete
    suspend fun deleteResult(result: Result)

    @Query("DELETE FROM news WHERE liked=0 AND cache=1")
    suspend fun deleteCachedNews()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCachedNews(resultList: List<Result>)
}