package com.connectycube.messenger.data

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * The Data Access Object for the User class.
 */
@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY name")
    fun getUsersSync(): List<User>

    @Query("SELECT * FROM users ORDER BY name")
    fun getUsers(): LiveData<List<User>>

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUser(userId: Int): LiveData<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: List<User>)

    @Query("delete from users")
    fun delete();

    @Query("SELECT * FROM users WHERE id in (:usersIds)")
    fun getUsersByIds(vararg usersIds: Int): LiveData<List<User>>

    @Query("SELECT * FROM users WHERE id in (:usersIds)")
    fun getUsersByIdsSync(vararg usersIds: Int): List<User>

    @Query("SELECT * FROM users WHERE id in (:usersIds) and id!=(:userId)")
    fun getUsersByIdsPvt( usersIds: List<Int>, userId:Int): List<User>
}