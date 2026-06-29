package cu.cupet.isp.taxigest.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class TripWithDetails(
    @Embedded val trip: Trip,
    @Relation(
        parentColumn = "taxiId",
        entityColumn = "id"
    )
    val taxi: Taxi,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TripClient::class,
            parentColumn = "tripId",
            entityColumn = "clientId"
        )
    )
    val clients: List<Client>,
    @Relation(
        parentColumn = "id",
        entityColumn = "tripId"
    )
    val tripClients: List<TripClient>
)
