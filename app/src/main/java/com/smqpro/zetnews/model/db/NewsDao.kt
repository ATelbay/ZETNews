package com.smqpro.zetnews.model.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.smqpro.zetnews.model.response.CurrentPage
import com.smqpro.zetnews.model.response.Result

@Dao
interface NewsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCurrentPage(currentPage: CurrentPage)

    @Query("SELECT * FROM current_page WHERE id = 0")
    suspend fun getCurrentPage(): CurrentPage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(result: Result): Long

    @Query("SELECT * FROM news WHERE cache = 1 ORDER BY timestamp DESC")
    fun selectCachedDesc(): LiveData<List<Result>>

    @Query("SELECT * FROM news WHERE cache = 1 ORDER BY timestamp ASC")
    fun selectCachedAsc(): LiveData<List<Result>>

    @Query("SELECT * FROM news WHERE cache = 1")
    fun selectCached(): LiveData<List<Result>>

    @Query("SELECT * FROM news WHERE liked = 1 ORDER BY updatedAt DESC")
    fun selectLiked(): LiveData<List<Result>>

    @Query("SELECT * FROM news WHERE liked = 1 ORDER BY updatedAt DESC")
    suspend fun getLiked(): List<Result>

    @Delete
    suspend fun deleteResult(result: Result)

    @Delete
    suspend fun deleteResultList(resultList: List<Result>)

    @Query("DELETE FROM news WHERE cache = 1")
    suspend fun deleteCachedNews()

    @Query("DELETE FROM news")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNewsList(resultList: List<Result>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNewsList(resultList: List<Result>)

    @Query("SELECT * FROM news WHERE cache = 1  ORDER BY webPublicationDate ASC LIMIT :rowNumber")
    suspend fun selectForTruncate(rowNumber: Int): List<Result>

    @Query("SELECT COUNT(*) FROM news WHERE cache = 1")
    suspend fun getRowNumber(): Int

    @Transaction
    suspend fun resetCachedNews(resultList: List<Result>) {
        val likedNews = getLiked()
        resultList.forEach { res ->
            likedNews.forEach { liked ->
                liked.cache = false
                if (res.id == liked.id)
                    res.liked = true
            }
        }
        deleteAll()
        upsertNewsList(resultList)
        insertNewsList(likedNews)
    }

    @Transaction
    suspend fun truncateCache() {
        val resultList = selectForTruncate(getRowNumber() - 10)
        deleteResultList(resultList)
    }
}