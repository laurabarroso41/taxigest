package cu.cupet.isp.taxigest.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "trip_clients",
    primaryKeys = ["tripId", "clientId"],
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Client::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tripId"), Index("clientId")]
)
data class TripClient(
    val tripId: Long,
    val clientId: Long,
    val pickupAddress: String,
    val destinationAddress: String,
    val arrivalTime: Long, // Unix timestamp
    val departureTime: Long // Unix timestamp
)
