package cu.cupet.isp.taxigest.data.dao

import androidx.room.*
import cu.cupet.isp.taxigest.data.model.Trip
import cu.cupet.isp.taxigest.data.model.TripClient
import cu.cupet.isp.taxigest.data.model.TripWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Transaction
    @Query("SELECT * FROM trips ORDER BY date DESC")
    fun getAllTripsWithDetails(): Flow<List<TripWithDetails>>

    @Transaction
    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun getTripWithDetailsById(id: Long): TripWithDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: Trip): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTripClient(tripClient: TripClient)

    @Update
    suspend fun updateTrip(trip: Trip)

    @Delete
    suspend fun deleteTrip(trip: Trip)

    @Query("DELETE FROM trip_clients WHERE tripId = :tripId")
    suspend fun deleteTripClientsByTripId(tripId: Long)

    @Transaction
    suspend fun insertTripWithClients(trip: Trip, clients: List<TripClient>) {
        val tripId = insertTrip(trip)
        clients.forEach { insertTripClient(it.copy(tripId = tripId)) }
    }

    @Transaction
    suspend fun updateTripWithClients(trip: Trip, clients: List<TripClient>) {
        updateTrip(trip)
        deleteTripClientsByTripId(trip.id)
        clients.forEach { insertTripClient(it.copy(tripId = trip.id)) }
    }

    // Report queries
    @Query("SELECT SUM(totalPrice - driverPrice) FROM trips WHERE date BETWEEN :startDate AND :endDate")
    fun getGainsReport(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT date, COUNT(*) as count FROM trips WHERE date BETWEEN :startDate AND :endDate GROUP BY date")
    fun getTripsByDateReport(startDate: Long, endDate: Long): Flow<List<DateCount>>

    @Query("SELECT taxiId, SUM(totalPrice - driverPrice) as gain FROM trips WHERE date BETWEEN :startDate AND :endDate GROUP BY taxiId")
    fun getGainByTaxiReport(startDate: Long, endDate: Long): Flow<List<TaxiGain>>

    @Query("SELECT date, SUM(passengersCount) as count FROM trips WHERE date BETWEEN :startDate AND :endDate GROUP BY date")
    fun getClientsCountReport(startDate: Long, endDate: Long): Flow<List<DateCount>>

    @Query("SELECT tripType, SUM(passengersCount) as count FROM trips WHERE date BETWEEN :startDate AND :endDate GROUP BY tripType")
    fun getPassengersByTripTypeReport(startDate: Long, endDate: Long): Flow<List<TripTypeCount>>

    @Transaction
    @Query("SELECT * FROM trips WHERE date BETWEEN :startDate AND :endDate")
    fun getTripsWithDetailsByDate(startDate: Long, endDate: Long): Flow<List<TripWithDetails>>

    data class DateCount(val date: Long, val count: Int)
    data class TaxiGain(val taxiId: Long, val gain: Double)
    data class TripTypeCount(val tripType: Int, val count: Int)
}
