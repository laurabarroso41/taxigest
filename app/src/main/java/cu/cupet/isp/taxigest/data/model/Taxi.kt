package cu.cupet.isp.taxigest.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "taxis")
data class Taxi(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val seatsCount: Int,
    val carType: String,
    val hasAirConditioning: Boolean,
    val cellphone: String?,
    val fixPhone:String?
)
