package cu.cupet.isp.taxigest.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cu.cupet.isp.taxigest.data.dao.ClientDao
import cu.cupet.isp.taxigest.data.dao.TaxiDao
import cu.cupet.isp.taxigest.data.dao.TripDao
import cu.cupet.isp.taxigest.data.dao.UserDao
import cu.cupet.isp.taxigest.data.model.Client
import cu.cupet.isp.taxigest.data.model.Taxi
import cu.cupet.isp.taxigest.data.model.Trip
import cu.cupet.isp.taxigest.data.model.TripClient
import cu.cupet.isp.taxigest.data.model.User

@Database(
    entities = [Taxi::class, Client::class, Trip::class, TripClient::class, User::class],
    version = 3,
    exportSchema = false
)
abstract class TaxiGestDatabase : RoomDatabase() {
    abstract fun taxiDao(): TaxiDao
    abstract fun clientDao(): ClientDao
    abstract fun tripDao(): TripDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: TaxiGestDatabase? = null

        fun getDatabase(context: Context): TaxiGestDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaxiGestDatabase::class.java,
                    "taxigest_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
