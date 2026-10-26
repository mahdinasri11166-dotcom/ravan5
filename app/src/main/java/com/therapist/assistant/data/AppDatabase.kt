package com.therapist.assistant.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.security.MessageDigest

class Converters {
    @TypeConverter fun fromStatus(s: AppointmentStatus): String = s.name
    @TypeConverter fun toStatus(v: String): AppointmentStatus =
        try { AppointmentStatus.valueOf(v) } catch (_: Exception) { AppointmentStatus.SCHEDULED }

    @TypeConverter fun fromRecurrence(r: RecurrenceType): String = r.name
    @TypeConverter fun toRecurrence(v: String): RecurrenceType =
        try { RecurrenceType.valueOf(v) } catch (_: Exception) { RecurrenceType.NONE }

    @TypeConverter fun fromPayment(p: PaymentMethod): String = p.name
    @TypeConverter fun toPayment(v: String): PaymentMethod =
        try { PaymentMethod.valueOf(v) } catch (_: Exception) { PaymentMethod.NONE }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        listOf(
            "ALTER TABLE appointments ADD COLUMN soapSubjective TEXT NOT NULL DEFAULT ''",
            "ALTER TABLE appointments ADD COLUMN soapObjective TEXT NOT NULL DEFAULT ''",
            "ALTER TABLE appointments ADD COLUMN soapAssessment TEXT NOT NULL DEFAULT ''",
            "ALTER TABLE appointments ADD COLUMN soapPlan TEXT NOT NULL DEFAULT ''",
            "ALTER TABLE appointments ADD COLUMN noteTags TEXT NOT NULL DEFAULT ''",
            "ALTER TABLE appointments ADD COLUMN paymentMethod TEXT NOT NULL DEFAULT 'NONE'",
            "ALTER TABLE appointments ADD COLUMN cancelReason TEXT NOT NULL DEFAULT ''",
            "ALTER TABLE appointments ADD COLUMN recurrenceEndMillis INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE appointments ADD COLUMN invoiceSerial TEXT NOT NULL DEFAULT ''"
        ).forEach { sql -> try { db.execSQL(sql) } catch (_: Exception) {} }
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS questionnaires (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                clientId INTEGER NOT NULL,
                type TEXT NOT NULL,
                score INTEGER NOT NULL DEFAULT 0,
                detailsJson TEXT NOT NULL DEFAULT '',
                createdAt INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS app_settings (
                key TEXT PRIMARY KEY NOT NULL,
                value TEXT NOT NULL
            )
        """.trimIndent())
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        listOf(
            "ALTER TABLE clients ADD COLUMN nationalId TEXT NOT NULL DEFAULT ''",
            "ALTER TABLE clients ADD COLUMN birthDateMillis INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE clients ADD COLUMN address TEXT NOT NULL DEFAULT ''"
        ).forEach { sql -> try { db.execSQL(sql) } catch (_: Exception) {} }
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        try { db.execSQL("ALTER TABLE clients ADD COLUMN education TEXT NOT NULL DEFAULT ''") } catch (_: Exception) {}
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS workshops (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                province TEXT NOT NULL DEFAULT 'خوزستان',
                city TEXT NOT NULL DEFAULT 'اهواز',
                centerName TEXT NOT NULL DEFAULT 'گل سرخ',
                location TEXT NOT NULL DEFAULT '',
                durationHours INTEGER NOT NULL DEFAULT 4,
                topics TEXT NOT NULL DEFAULT '',
                trainerName TEXT NOT NULL DEFAULT '',
                dateMillis INTEGER NOT NULL,
                createdAt INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS workshop_participants (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                workshopId INTEGER NOT NULL,
                clientId INTEGER,
                fullName TEXT NOT NULL,
                birthDateText TEXT NOT NULL DEFAULT '',
                education TEXT NOT NULL DEFAULT '',
                nationalId TEXT NOT NULL DEFAULT '',
                preTestScore TEXT NOT NULL DEFAULT '',
                postTestScore TEXT NOT NULL DEFAULT '',
                phone TEXT NOT NULL DEFAULT '',
                FOREIGN KEY(workshopId) REFERENCES workshops(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_workshop_participants_workshopId ON workshop_participants(workshopId)")
    }
}


val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        try { db.execSQL("ALTER TABLE clients ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0") } catch (_: Exception) {}
    }
}

@Database(
    entities = [
        Client::class, Appointment::class, QuestionnaireResult::class, AppSetting::class,
        Workshop::class, WorkshopParticipant::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun questionnaireDao(): QuestionnaireDao
    abstract fun workshopDao(): WorkshopDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private fun passphrase(context: Context): ByteArray {
            val seed = "golsorkh-" + context.packageName
            return MessageDigest.getInstance("SHA-256").digest(seed.toByteArray())
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                System.loadLibrary("sqlcipher")
                val factory = SupportOpenHelperFactory(passphrase(context))
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "therapist_assistant_db_enc"
                )
                    .openHelperFactory(factory)
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
