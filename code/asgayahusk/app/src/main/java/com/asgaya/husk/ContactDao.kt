package com.asgaya.husk

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<Contact>>

    @Query("SELECT * FROM contacts WHERE id = :contactId")
    suspend fun getContactById(contactId: Int): Contact?

    @Query("SELECT * FROM contacts WHERE cashAccount = :cashAccount LIMIT 1")
    suspend fun getContactByCashAccount(cashAccount: String): Contact?

    @Query("SELECT * FROM contacts WHERE publicKey = :publicKey LIMIT 1")
    suspend fun getContactByPublicKey(publicKey: String): Contact?

    @Query("SELECT * FROM contacts WHERE nostrPubkey IS NOT NULL AND nostrPubkey != '' ORDER BY name ASC")
    suspend fun getContactsWithNostr(): List<Contact>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: Contact): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<Contact>)

    @Update
    suspend fun update(contact: Contact)

    @Delete
    suspend fun delete(contact: Contact)

    @Query("DELETE FROM contacts")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM contacts")
    suspend fun getContactCount(): Int
}
