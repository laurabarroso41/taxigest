package cu.cupet.isp.taxigest.data.dao

import androidx.room.*
import cu.cupet.isp.taxigest.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}
