package com.therapist.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.therapist.assistant.data.Appointment
import com.therapist.assistant.data.AppointmentStatus
import com.therapist.assistant.ui.components.*
import com.therapist.assistant.ui.theme.Amber500
import com.therapist.assistant.ui.theme.Emerald500
import com.therapist.assistant.ui.theme.Sky500
import com.therapist.assistant.util.JalaliDate
import com.therapist.assistant.util.MessageHelper
import com.therapist.assistant.viewmodel.AppointmentViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: AppointmentViewModel,
    onAddClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    onClientsClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onReportsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAllAppointments: () -> Unit,
    onDebtsClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onSelfCareClick: () -> Unit = {},
    onDocumentsClick: () -> Unit = {},
    onBusinessCardClick: () -> Unit = {},
    onMoreClick: () -> Unit = {}
) {
    val todayList by viewModel.getTodayAppointments().collectAsState(initial = emptyList())
    val unpaid by viewModel.unpaidAppointments.collectAsState()
    val clients by viewModel.clients.collectAsState()
    val upcoming by viewModel.upcomingAppointments.collectAsState()
    val template by viewModel.msgTemplate.collectAsState()
    val clinicName by viewModel.clinicName.collectAsState()
    val context = LocalContext.current
    val currency = remember { NumberFormat.getNumberInstance(Locale("fa")) }
    val dateLabel = remember { JalaliDate.formatDateLong(System.currentTimeMillis()) }
    val nextUp = remember(upcoming) { upcoming.take(4) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp),
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("نوبت جدید", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Hero header
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.88f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.72f)
                                )
                            )
                        )
                ) {
                    Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 28.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    clinicName.ifBlank { "گل سرخ" },
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    dateLabel,
                                    color = Color.White.copy(alpha = 0.9f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            IconButton(
                                onClick = onSettingsClick,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.18f))
                            ) {
                                Icon(Icons.Default.Settings, "تنظیمات", tint = Color.White)
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HeroStat(
                                Modifier.weight(1f),
                                "${todayList.size}",
                                "امروز",
                                Icons.Default.Event
                            )
                            HeroStat(
                                Modifier.weight(1f),
                                "${upcoming.size}",
                                "پیش‌رو",
                                Icons.Default.Schedule
                            )
                            HeroStat(
                                Modifier.weight(1f),
                                "${unpaid.size}",
                                "بدهکار",
                                Icons.Default.AccountBalanceWallet
                            )
                        }
                    }
                }
            }

            // Quick navigation
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { NavChip("جلسات", Icons.Default.EventNote, onAllAppointments) }
                    item { NavChip("مراجعین", Icons.Default.People, onClientsClick) }
                    item { NavChip("بدهی‌ها", Icons.Default.AccountBalanceWallet, onDebtsClick) }
                    item { NavChip("جستجو", Icons.Default.Search, onSearchClick) }
                    item { NavChip("بیشتر", Icons.Default.Apps, onMoreClick) }
                }
            }

            // Next up strip
            if (nextUp.isNotEmpty()) {
                item {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        SectionHeader("نوبت بعدی", "همه", onAllAppointments)
                    }
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(nextUp, key = { it.id }) { a ->
                            SoftCard(
                                modifier = Modifier.width(168.dp),
                                onClick = { onEditClick(a.id) }
                            ) {
                                Text(
                                    JalaliDate.formatTime(a.dateTimeMillis),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    JalaliDate.formatDate(a.dateTimeMillis),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    a.clientName,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (a.fee > 0) {
                                    Text(
                                        "${currency.format(a.fee)} ت",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (a.isPaid) Emerald500 else Amber500
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Today's sessions
            item {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SectionHeader(
                        "جلسات امروز",
                        if (todayList.isNotEmpty()) "${todayList.size} جلسه" else null
                    )
                }
            }

            if (todayList.isEmpty()) {
                item {
                    Box(Modifier.padding(horizontal = 16.dp)) {
                        EmptyState(
                            icon = Icons.Default.EventAvailable,
                            title = "امروز جلسه‌ای ندارید",
                            subtitle = "زمان خوبی برای برنامه‌ریزی یا استراحت است",
                            actionLabel = "ثبت نوبت",
                            onAction = onAddClick
                        )
                    }
                }
            } else {
                items(todayList, key = { it.id }) { appt ->
                    Box(Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                        TodaySessionCard(
                            appt = appt,
                            currency = currency,
                            onOpen = { onEditClick(appt.id) },
                            onComplete = { viewModel.updateStatus(appt, AppointmentStatus.COMPLETED) },
                            onCancel = {
                                viewModel.updateStatus(appt, AppointmentStatus.CANCELLED, "لغو سریع")
                            },
                            onSms = {
                                val phone = clients.find { it.id == appt.clientId }?.phone.orEmpty()
                                val text = MessageHelper.buildReminderText(
                                    template, appt.clientName, appt.dateTimeMillis
                                )
                                MessageHelper.openSms(context, phone, text)
                            },
                            onWhatsApp = {
                                val phone = clients.find { it.id == appt.clientId }?.phone.orEmpty()
                                val text = MessageHelper.buildReminderText(
                                    template, appt.clientName, appt.dateTimeMillis
                                )
                                if (phone.isNotBlank()) MessageHelper.openWhatsApp(context, phone, text)
                                else MessageHelper.openEitaa(context, "", text)
                            }
                        )
                    }
                }
            }

            // Debts teaser
            if (unpaid.isNotEmpty()) {
                item {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        SectionHeader("بدهکاران", "مدیریت", onDebtsClick)
                    }
                }
                items(unpaid.take(4), key = { "d-${it.id}" }) { appt ->
                    Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        SoftCard(onClick = { onEditClick(appt.id) }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AvatarCircle(appt.clientName, Amber500, 42)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(appt.clientName, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        JalaliDate.formatDate(appt.dateTimeMillis),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "${currency.format(appt.fee)} ت",
                                        fontWeight = FontWeight.Bold,
                                        color = Amber500
                                    )
                                    TextButton(
                                        onClick = { viewModel.togglePaid(appt) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                    ) { Text("تسویه") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroStat(
    modifier: Modifier,
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(6.dp))
        Text(value, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun TodaySessionCard(
    appt: Appointment,
    currency: NumberFormat,
    onOpen: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    onSms: () -> Unit,
    onWhatsApp: () -> Unit
) {
    SoftCard(onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    JalaliDate.formatTime(appt.dateTimeMillis),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    appt.clientName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (appt.notes.isNotBlank()) {
                    Text(
                        appt.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (appt.fee > 0) {
                    Text(
                        "${currency.format(appt.fee)} تومان · ${if (appt.isPaid) "پرداخت‌شده" else "بدهکار"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (appt.isPaid) Emerald500 else Amber500
                    )
                }
            }
            StatusPill(
                when (appt.status) {
                    AppointmentStatus.SCHEDULED -> "زمان‌بندی"
                    AppointmentStatus.COMPLETED -> "انجام"
                    AppointmentStatus.CANCELLED -> "لغو"
                    AppointmentStatus.RESCHEDULED -> "جابجا"
                },
                when (appt.status) {
                    AppointmentStatus.SCHEDULED -> MaterialTheme.colorScheme.primary
                    AppointmentStatus.COMPLETED -> Emerald500
                    AppointmentStatus.CANCELLED -> MaterialTheme.colorScheme.error
                    AppointmentStatus.RESCHEDULED -> Sky500
                }
            )
        }
        if (appt.status == AppointmentStatus.SCHEDULED) {
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = onComplete,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("انجام")
                }
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("لغو")
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onSms) {
                    Icon(Icons.Default.Sms, "پیامک", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onWhatsApp) {
                    Icon(Icons.Default.Chat, "واتساپ", tint = Emerald500)
                }
            }
        }
    }
}
