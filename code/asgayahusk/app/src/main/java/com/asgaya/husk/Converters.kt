package com.asgaya.husk

import androidx.room.TypeConverter
import com.asgaya.husk.data.WalletRole
import com.asgaya.husk.data.WalletSourceType

/**
 * Room Type Converters for custom types
 */
class Converters {

    @TypeConverter
    fun fromWalletRole(role: WalletRole): String {
        return role.name
    }

    @TypeConverter
    fun toWalletRole(value: String): WalletRole {
        return WalletRole.valueOf(value)
    }

    @TypeConverter
    fun fromWalletSourceType(sourceType: WalletSourceType): String {
        return sourceType.name
    }

    @TypeConverter
    fun toWalletSourceType(value: String): WalletSourceType {
        return WalletSourceType.valueOf(value)
    }
}
