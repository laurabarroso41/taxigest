package cu.cupet.isp.taxigest.data.dao

import androidx.room.*
import cu.cupet.isp.taxigest.data.model.Taxi
import kotlinx.coroutines.flow.Flow

@Dao
interface TaxiDao {
    @Query("SELECT * FROM taxis")
    fun getAllTaxis(): Flow<List<Taxi>>

    @Query("SELECT * FROM taxis WHERE id = :id")
    suspend fun getTaxiById(id: Long): Taxi?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaxi(taxi: Taxi): Long

    @Update
    suspend fun updateTaxi(taxi: Taxi)

    @Delete
    suspend fun deleteTaxi(taxi: Taxi)
}
