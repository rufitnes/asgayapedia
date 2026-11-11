package com.asgaya.husk

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.asgaya.husk.data.Wallet
import com.asgaya.husk.data.WalletDao
import com.asgaya.husk.data.CashoutMessage
import com.asgaya.husk.data.CashoutMessageDao
import com.asgaya.husk.data.ListingDao
import com.asgaya.husk.data.ListingEntity
import com.asgaya.husk.data.BoardEventDao
import com.asgaya.husk.data.BoardEventEntity
import com.asgaya.husk.remittance.Remittance
import com.asgaya.husk.remittance.RemittanceDao
import com.asgaya.husk.transaction.PendingTransaction
import com.asgaya.husk.transaction.PendingTransactionDao

@Database(
    entities = [
        ParsedNotification::class,
        CovenantRequest::class,
        Contact::class,
        Wallet::class,
        Remittance::class,
        PendingTransaction::class,
        CashoutMessage::class,
        ListingEntity::class,
        BoardEventEntity::class
    ],
    version = 30,  // v30: `cashout_messages.counterpartyNpub` (notify the recipient on broadcast, `ui-rethink/41`)
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
    abstract fun covenantDao(): CovenantDao
    abstract fun contactDao(): ContactDao
    abstract fun walletDao(): WalletDao
    abstract fun remittanceDao(): RemittanceDao
    abstract fun pendingTransactionDao(): PendingTransactionDao
    abstract fun cashoutMessageDao(): CashoutMessageDao
    abstract fun listingDao(): ListingDao
    abstract fun boardEventDao(): BoardEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** v19 → v20: the covenant's funding outpoint (`covenant_requests.fundingTxid/fundingVout`). */
        private val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE covenant_requests ADD COLUMN fundingTxid TEXT")
                db.execSQL("ALTER TABLE covenant_requests ADD COLUMN fundingVout INTEGER")
            }
        }

        /** v20 → v21: counterparty npubs for the [COVENANT_RESOLVED] directions (`ui-rethink/16` P1). */
        private val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE remittances ADD COLUMN funderNostrPubkey TEXT")
                db.execSQL("ALTER TABLE remittances ADD COLUMN senderNostrPubkey TEXT")
            }
        }

        /** v21 → v22: the fund-request pay window (`covenant_requests.payWindowSeconds`). */
        private val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE covenant_requests ADD COLUMN payWindowSeconds INTEGER")
            }
        }

        /** v22 → v23: the recipient's CashAccount on the remittance — the reference "Send again" must preserve (`ui-rethink/34`). */
        private val MIGRATION_22_23 = object : Migration(22, 23) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE remittances ADD COLUMN cashAccount TEXT")
            }
        }

        private val MIGRATION_23_24 = object : Migration(23, 24) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE covenant_requests ADD COLUMN assetCurrency TEXT")
            }
        }

        private val MIGRATION_24_25 = object : Migration(24, 25) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE covenant_requests ADD COLUMN payAmount REAL")
                db.execSQL("ALTER TABLE covenant_requests ADD COLUMN payCurrency TEXT")
            }
        }

        private val MIGRATION_25_26 = object : Migration(25, 26) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE covenant_requests ADD COLUMN feePercent REAL")
            }
        }

        private val MIGRATION_26_27 = object : Migration(26, 27) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE remittances ADD COLUMN senderCashAccount TEXT")
            }
        }

        /** v27 → v28: the ad's declared rate margin (`listings.spreadPercent`, `bulletin-board/20`). */
        private val MIGRATION_27_28 = object : Migration(27, 28) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE listings ADD COLUMN spreadPercent REAL NOT NULL DEFAULT 0")
            }
        }

        /** v28 → v29: collapse the ad rate into a single `fee` — drop `listings.spreadPercent` (`bulletin-board/20`). */
        private val MIGRATION_28_29 = object : Migration(28, 29) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `listings_new` (`id` TEXT NOT NULL, `adType` TEXT NOT NULL, " +
                        "`cashAccount` TEXT NOT NULL, `publicKey` TEXT NOT NULL, `nostrPubkey` TEXT, " +
                        "`assetsCsv` TEXT NOT NULL, `paymentMethodsCsv` TEXT NOT NULL, `fee` REAL NOT NULL, " +
                        "`minPricePercent` INTEGER NOT NULL, `location` TEXT, `minAmount` REAL, `maxAmount` REAL, " +
                        "`availableHours` TEXT, `isOnline` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, " +
                        "`establishment` TEXT, `contactPhone` TEXT, `contactIban` TEXT, `notes` TEXT, PRIMARY KEY(`id`))"
                )
                db.execSQL(
                    "INSERT INTO `listings_new` (`id`,`adType`,`cashAccount`,`publicKey`,`nostrPubkey`,`assetsCsv`," +
                        "`paymentMethodsCsv`,`fee`,`minPricePercent`,`location`,`minAmount`,`maxAmount`,`availableHours`," +
                        "`isOnline`,`updatedAt`,`establishment`,`contactPhone`,`contactIban`,`notes`) " +
                        "SELECT `id`,`adType`,`cashAccount`,`publicKey`,`nostrPubkey`,`assetsCsv`,`paymentMethodsCsv`," +
                        "`fee`,`minPricePercent`,`location`,`minAmount`,`maxAmount`,`availableHours`,`isOnline`,`updatedAt`," +
                        "`establishment`,`contactPhone`,`contactIban`,`notes` FROM `listings`"
                )
                db.execSQL("DROP TABLE `listings`")
                db.execSQL("ALTER TABLE `listings_new` RENAME TO `listings`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_listings_publicKey` ON `listings` (`publicKey`)")
            }
        }

        /** v29 → v30: the counterparty npub on a cash-out message (notify the recipient on broadcast). */
        private val MIGRATION_29_30 = object : Migration(29, 30) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cashout_messages ADD COLUMN counterpartyNpub TEXT")
            }
        }

        /** All migrations, in order — inspectable by tests (`room-migrations/`, `ui-rethink/16` P0-6). */
        val MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_19_20, MIGRATION_20_21, MIGRATION_21_22, MIGRATION_22_23, MIGRATION_23_24, MIGRATION_24_25, MIGRATION_25_26, MIGRATION_26_27, MIGRATION_27_28, MIGRATION_28_29, MIGRATION_29_30)

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bizum_parser_database"
                )
                .addMigrations(*MIGRATIONS)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}