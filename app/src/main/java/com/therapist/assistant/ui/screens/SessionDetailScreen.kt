package com.therapist.assistant.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.therapist.assistant.data.Appointment
import com.therapist.assistant.data.AppointmentStatus
import com.therapist.assistant.data.PaymentMethod
import com.therapist.assistant.ui.components.*
import com.therapist.assistant.ui.theme.Amber500
import com.therapist.assistant.ui.theme.Emerald500
import com.therapist.assistant.util.IcsExporter
import com.therapist.assistant.util.PdfExporter
import androidx.core.content.FileProvider
import java.io.File
import com.therapist.assistant.util.JalaliDate
import com.therapist.assistant.util.MessageHelper
import com.therapist.assistant.viewmodel.AppointmentViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    viewModel: AppointmentViewModel,
    appointmentId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onClientClick: (Long) -> Unit
) {
    var appt by remember { mutableStateOf<Appointment?>(null) }
    var clientPhone by remember { mutableStateOf("") }
    var showCancelDialog by remember { mutableStateOf(false) }
    var cancelReason by remember { mutableStateOf("") }
    val template by viewModel.msgTemplate.collectAsState()
    val clinicName by viewModel.clinicName.collectAsState()
    val clinicAddress by viewModel.clinicAddress.collectAsState()
    val clinicPhone by viewModel.clinicPhone.collectAsState()
    val context = LocalContext.current
    val currency = remember { NumberFormat.getNumberInstance(Locale("fa")) }

    LaunchedEffect(appointmentId) {
        appt = viewModel.getAppointment(appointmentId)
        appt?.let { a ->
            viewModel.getClient(a.clientId)?.let { clientPhone = it.phone }
        }
    }

    // refresh when returning from edits via collecting all
    val all by viewModel.allAppointments.collectAsState()
    LaunchedEffect(all, appointmentId) {
        appt = all.find { it.id == appointmentId } ?: viewModel.getAppointment(appointmentId)
    }

    val a = appt
    Scaffold { padding ->
        if (a == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            GradientTopBar(
                title = "جزئیات جلسه",
                subtitle = a.clientName,
                onBack = onBack,
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "ویرایش", tint = androidx.compose.ui.graphics.Color.White)
                    }
                }
            )
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
            // Status + client header
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(52.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                a.clientName.take(1),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            TextButton(
                                onClick = { onClientClick(a.clientId) },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    a.clientName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            StatusBadge(a.status)
                        }
                    }
                    HorizontalDivider()
                    InfoRow(Icons.Default.CalendarMonth, "تاریخ", JalaliDate.formatDateLong(a.dateTimeMillis))
                    InfoRow(Icons.Default.Schedule, "ساعت", JalaliDate.formatTime(a.dateTimeMillis))
                    if (clientPhone.isNotBlank()) {
                        InfoRow(Icons.Default.Phone, "تلفن", JalaliDate.toFa(clientPhone))
                    }
                }
            }

            // Financial card
            Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("مالی", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("مبلغ جلسه")
                        Text(
                            if (a.fee > 0) "${currency.format(a.fee)} تومان" else "—",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("وضعیت پرداخت")
                        FilterChip(
                            selected = a.isPaid,
                            onClick = { viewModel.togglePaid(a) },
                            label = { Text(if (a.isPaid) "پرداخت‌شده" else "بدهکار") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                containerColor = Amber500.copy(alpha = 0.15f)
                            )
                        )
                    }
                    if (a.isPaid && a.paymentMethod != PaymentMethod.NONE) {
                        val method = when (a.paymentMethod) {
                            PaymentMethod.CASH -> "نقد"
                            PaymentMethod.CARD -> "کارت"
                            PaymentMethod.TRANSFER -> "کارت‌به‌کارت"
                            PaymentMethod.OTHER -> "سایر"
                            PaymentMethod.NONE -> "—"
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("روش پرداخت")
                            Text(method, fontWeight = FontWeight.Medium)
                        }
                    }
                    if (a.invoiceSerial.isNotBlank()) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("شماره فاکتور")
                            Text(a.invoiceSerial, fontWeight = FontWeight.Medium)
                        }
                    }
                    if (a.fee > 0) {
                        OutlinedButton(
                            onClick = {
                                val text = buildString {
                                    appendLine("صورتحساب $clinicName")
                                    appendLine("شماره: ${a.invoiceSerial.ifBlank { "—" }}")
                                    appendLine("مراجع: ${a.clientName}")
                                    appendLine("تاریخ: ${JalaliDate.formatDateTime(a.dateTimeMillis)}")
                                    appendLine("مبلغ: ${currency.format(a.fee)} تومان")
                                    appendLine("وضعیت: ${if (a.isPaid) "پرداخت‌شده" else "بدهکار"}")
                                }
                                context.startActivity(
                                    Intent.createChooser(
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, text)
                                            putExtra(Intent.EXTRA_SUBJECT, "صورتحساب ${a.clientName}")
                                        },
                                        "اشتراک صورتحساب"
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Share, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("اشتراک صورتحساب")
                        }
                        OutlinedButton(
                            onClick = {
                                val ics = IcsExporter.build(listOf(a), clinicName)
                                context.startActivity(
                                    Intent.createChooser(
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "text/calendar"
                                            putExtra(Intent.EXTRA_TEXT, ics)
                                            putExtra(Intent.EXTRA_SUBJECT, "session.ics")
                                        },
                                        "افزودن به تقویم"
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("خروجی تقویم (ICS)")
                        }
                        OutlinedButton(
                            onClick = {
                                val file = File(context.cacheDir, "invoice_${a.id}.pdf")
                                PdfExporter.writeStyledPdf(
                                    file = file,
                                    brand = PdfExporter.Brand(
                                        title = clinicName.ifBlank { "درمانگرنو" },
                                        subtitle = "صورتحساب جلسه مشاوره",
                                        phone = clinicPhone,
                                        address = clinicAddress
                                    ),
                                    introLines = listOf(
                                        "شماره سریال: ${a.invoiceSerial.ifBlank { "—" }}",
                                        "تاریخ صدور: ${JalaliDate.formatDate(System.currentTimeMillis())}"
                                    ),
                                    tableColumns = listOf(
                                        PdfExporter.TableColumn("شرح", 0.45f),
                                        PdfExporter.TableColumn("مقدار", 0.55f)
                                    ),
                                    tableRows = listOf(
                                        listOf("مراجع", a.clientName),
                                        listOf("تاریخ جلسه", JalaliDate.formatDateTime(a.dateTimeMillis)),
                                        listOf("مبلغ (تومان)", currency.format(a.fee.toLong())),
                                        listOf("وضعیت پرداخت", if (a.isPaid) "پرداخت‌شده" else "بدهکار"),
                                        listOf("روش پرداخت", a.paymentMethod.name)
                                    ),
                                    sections = listOf(
                                        "یادداشت" to listOf(
                                            a.sessionNotes.ifBlank { "—" },
                                            "این سند توسط نرم‌افزار درمانگرنو تولید شده است."
                                        )
                                    ),
                                    footer = "محرمانه — $clinicName"
                                )
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                context.startActivity(
                                    Intent.createChooser(
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "application/pdf"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        },
                                        "اشتراک PDF"
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("خروجی PDF صورتحساب")
                        }
                    }
                }
            }

            // SOAP / notes
            Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(1.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("یادداشت جلسه", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (a.noteTags.isNotBlank()) {
                        Text("🏷 ${a.noteTags}", color = MaterialTheme.colorScheme.primary)
                    }
                    if (a.notes.isNotBlank()) {
                        SoapBlock("یادداشت کوتاه", a.notes)
                    }
                    val hasSoap = listOf(a.soapSubjective, a.soapObjective, a.soapAssessment, a.soapPlan)
                        .any { it.isNotBlank() }
                    if (hasSoap) {
                        SoapBlock("S — گفته‌های مراجع", a.soapSubjective)
                        SoapBlock("O — مشاهدات", a.soapObjective)
                        SoapBlock("A — ارزیابی", a.soapAssessment)
                        SoapBlock("P — برنامه", a.soapPlan)
                    } else if (a.sessionNotes.isNotBlank()) {
                        SoapBlock("یادداشت آزاد", a.sessionNotes)
                    }
                    if (!hasSoap && a.sessionNotes.isBlank() && a.notes.isBlank()) {
                        Text("یادداشتی ثبت نشده", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (a.cancelReason.isNotBlank()) {
                        Text("دلیل لغو/جابجایی: ${a.cancelReason}", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Actions
            if (a.status == AppointmentStatus.SCHEDULED) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = {
                            viewModel.updateStatus(a, AppointmentStatus.COMPLETED)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("انجام شد")
                    }
                    OutlinedButton(
                        onClick = { showCancelDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Close, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("لغو")
                    }
                }
            }

            // Remind via message
            if (clientPhone.isNotBlank() && a.status == AppointmentStatus.SCHEDULED) {
                Text("یادآوری به مراجع", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                val text = MessageHelper.buildReminderText(template, a.clientName, a.dateTimeMillis)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = { MessageHelper.openSms(context, clientPhone, text) }, label = { Text("پیامک") })
                    AssistChip(onClick = { MessageHelper.openWhatsApp(context, clientPhone, text) }, label = { Text("واتساپ") })
                    AssistChip(onClick = { MessageHelper.openEitaa(context, clientPhone, text) }, label = { Text("ایتا/سایر") })
                }
            }

            OutlinedButton(
                onClick = {
                    viewModel.deleteAppointment(a)
                    onBack()
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, null)
                Spacer(Modifier.width(8.dp))
                Text("حذف جلسه")
            }
            }
        }
    }

    if (showCancelDialog && appt != null) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("لغو جلسه") },
            text = {
                OutlinedTextField(
                    value = cancelReason,
                    onValueChange = { cancelReason = it },
                    label = { Text("دلیل لغو") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateStatus(appt!!, AppointmentStatus.CANCELLED, cancelReason.ifBlank { "لغو از جزئیات" })
                    showCancelDialog = false
                    onBack()
                }) { Text("تأیید لغو") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("انصراف") }
            }
        )
    }
}

@Composable
private fun StatusBadge(status: AppointmentStatus) {
    val (label, color) = when (status) {
        AppointmentStatus.SCHEDULED -> "زمان‌بندی‌شده" to MaterialTheme.colorScheme.primary
        AppointmentStatus.COMPLETED -> "انجام‌شده" to MaterialTheme.colorScheme.secondary
        AppointmentStatus.CANCELLED -> "لغو‌شده" to MaterialTheme.colorScheme.error
        AppointmentStatus.RESCHEDULED -> "جابجا‌شده" to Color(0xFF6A1B9A)
    }
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.15f)) {
        Text(label, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = color, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text("$label: ", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SoapBlock(title: String, body: String) {
    if (body.isBlank()) return
    Column {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(body, style = MaterialTheme.typography.bodyMedium)
    }
}
