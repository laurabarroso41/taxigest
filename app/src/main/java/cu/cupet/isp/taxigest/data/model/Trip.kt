package cu.cupet.isp.taxigest.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taxiId: Long,
    val date: Long, // Unix timestamp
    val totalPrice: Double,
    val driverPrice: Double,
    val province: String,
    val hasMinors: Boolean,
    val passengersCount: Int,
    val isClientMessageSent: Boolean = false,
    val tripType: Int = 0 // 0: One way, 1: Round trip
)
