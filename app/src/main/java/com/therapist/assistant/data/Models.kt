package com.therapist.assistant.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class AppointmentStatus { SCHEDULED, COMPLETED, CANCELLED, RESCHEDULED }
enum class RecurrenceType { NONE, WEEKLY, BIWEEKLY }
enum class PaymentMethod { CASH, CARD, TRANSFER, OTHER, NONE }
enum class NoteType { FREE, SOAP }

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val nationalId: String = "",      // کد ملی (اختیاری)
    val birthDateMillis: Long = 0L,   // تاریخ تولد (اختیاری، 0 = خالی)
    val address: String = "",         // آدرس محل زندگی (اختیاری)
    val education: String = "",       // مدرک تحصیلی (اختیاری)
    val isArchived: Boolean = false,   // بایگانی
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "appointments",
    foreignKeys = [ForeignKey(
        entity = Client::class,
        parentColumns = ["id"],
        childColumns = ["clientId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("clientId"), Index("dateTimeMillis"), Index("status")]
)
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val clientName: String,
    val dateTimeMillis: Long,
    val notes: String = "",
    val sessionNotes: String = "",
    val soapSubjective: String = "",
    val soapObjective: String = "",
    val soapAssessment: String = "",
    val soapPlan: String = "",
    val noteTags: String = "",
    val fee: Double = 0.0,
    val isPaid: Boolean = false,
    val paymentMethod: PaymentMethod = PaymentMethod.NONE,
    val status: AppointmentStatus = AppointmentStatus.SCHEDULED,
    val cancelReason: String = "",
    val recurrence: RecurrenceType = RecurrenceType.NONE,
    val recurrenceEndMillis: Long = 0L,
    val parentRecurringId: Long? = null,
    val invoiceSerial: String = ""
)

@Entity(tableName = "questionnaires")
data class QuestionnaireResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val type: String,
    val score: Int = 0,
    val detailsJson: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSetting(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "workshops")
data class Workshop(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val province: String = "خوزستان",
    val city: String = "اهواز",
    val centerName: String = "گل سرخ",
    val location: String = "",
    val durationHours: Int = 4,
    val topics: String = "",
    val trainerName: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "workshop_participants",
    foreignKeys = [
        ForeignKey(entity = Workshop::class, parentColumns = ["id"], childColumns = ["workshopId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Client::class, parentColumns = ["id"], childColumns = ["clientId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("workshopId"), Index("clientId")]
)
data class WorkshopParticipant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workshopId: Long,
    val clientId: Long? = null,
    val fullName: String,
    val birthDateText: String = "",
    val education: String = "",
    val nationalId: String = "",
    val preTestScore: String = "",
    val postTestScore: String = "",
    val phone: String = ""
)
