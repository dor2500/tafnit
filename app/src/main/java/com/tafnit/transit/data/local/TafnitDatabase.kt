package com.tafnit.transit.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "favorite_places")
data class FavoritePlaceEntity(
    @PrimaryKey val id: String,
    val label: String,
    val iconKey: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isPinned: Boolean,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_stops")
data class FavoriteStopEntity(
    @PrimaryKey val stopCode: String,
    val stopId: String,
    val nameEn: String,
    val nameHe: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_lines")
data class FavoriteLineEntity(
    @PrimaryKey val lineId: String,
    val lineNumber: String,
    val operatorName: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey val id: String,
    val nameEn: String,
    val nameHe: String,
    val subtitleEn: String,
    val subtitleHe: String,
    val latitude: Double,
    val longitude: Double,
    val category: String,
    val stopCode: String?,
    val searchedAt: Long = System.currentTimeMillis()
)

@Dao
interface FavoritePlaceDao {
    @Query("SELECT * FROM favorite_places ORDER BY isPinned DESC, updatedAt DESC")
    fun observeAll(): Flow<List<FavoritePlaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FavoritePlaceEntity)

    @Query("DELETE FROM favorite_places WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM favorite_places")
    suspend fun count(): Int
}

@Dao
interface FavoriteStopDao {
    @Query("SELECT stopCode FROM favorite_stops")
    fun observeCodes(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteStopEntity)

    @Query("DELETE FROM favorite_stops WHERE stopCode = :code")
    suspend fun deleteByCode(code: String)
}

@Dao
interface FavoriteLineDao {
    @Query("SELECT lineId FROM favorite_lines")
    fun observeIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteLineEntity)

    @Query("DELETE FROM favorite_lines WHERE lineId = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface RecentSearchDao {
    @Query("SELECT * FROM recent_searches ORDER BY searchedAt DESC LIMIT 20")
    fun observeAll(): Flow<List<RecentSearchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RecentSearchEntity)

    @Query("DELETE FROM recent_searches")
    suspend fun clearAll()
}

@Database(
    entities = [
        FavoritePlaceEntity::class,
        FavoriteStopEntity::class,
        FavoriteLineEntity::class,
        RecentSearchEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TafnitDatabase : RoomDatabase() {
    abstract fun favoritePlaceDao(): FavoritePlaceDao
    abstract fun favoriteStopDao(): FavoriteStopDao
    abstract fun favoriteLineDao(): FavoriteLineDao
    abstract fun recentSearchDao(): RecentSearchDao
}
