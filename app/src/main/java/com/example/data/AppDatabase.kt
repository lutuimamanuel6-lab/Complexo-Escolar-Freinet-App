package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)
}

@Dao
interface SchoolOrderDao {
    @Query("SELECT * FROM school_orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<SchoolOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: SchoolOrder)

    @Query("DELETE FROM school_orders WHERE id = :orderId")
    suspend fun deleteOrderById(orderId: Int)

    @Query("DELETE FROM school_orders")
    suspend fun clearAllOrders()
}

@Dao
interface SubjectGradeDao {
    @Query("SELECT * FROM subject_grades ORDER BY subjectName ASC")
    fun getAllGrades(): Flow<List<SubjectGrade>>

    @Query("SELECT * FROM subject_grades WHERE term = :term ORDER BY subjectName ASC")
    fun getGradesByTerm(term: String): Flow<List<SubjectGrade>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: SubjectGrade)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrades(grades: List<SubjectGrade>)

    @Query("DELETE FROM subject_grades WHERE id = :gradeId")
    suspend fun deleteGradeById(gradeId: Int)

    @Query("DELETE FROM subject_grades")
    suspend fun clearAllGrades()
}

@Dao
interface CanteenFoodItemDao {
    @Query("SELECT * FROM canteen_items ORDER BY name ASC")
    fun getAllCanteenItems(): Flow<List<CanteenFoodItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCanteenItem(item: CanteenFoodItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCanteenItems(items: List<CanteenFoodItem>)

    @Query("DELETE FROM canteen_items WHERE id = :id")
    suspend fun deleteCanteenItemById(id: Int)

    @Query("DELETE FROM canteen_items")
    suspend fun clearAllCanteenItems()
}

@Dao
interface StoreProductItemDao {
    @Query("SELECT * FROM store_items ORDER BY name ASC")
    fun getAllStoreItems(): Flow<List<StoreProductItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoreItem(item: StoreProductItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoreItems(items: List<StoreProductItem>)

    @Query("DELETE FROM store_items WHERE id = :id")
    suspend fun deleteStoreItemById(id: Int)

    @Query("DELETE FROM store_items")
    suspend fun clearAllStoreItems()
}

@Database(
    entities = [
        UserProfile::class,
        SchoolOrder::class,
        SubjectGrade::class,
        CanteenFoodItem::class,
        StoreProductItem::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun schoolOrderDao(): SchoolOrderDao
    abstract fun subjectGradeDao(): SubjectGradeDao
    abstract fun canteenFoodItemDao(): CanteenFoodItemDao
    abstract fun storeProductItemDao(): StoreProductItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "freinet_school_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
