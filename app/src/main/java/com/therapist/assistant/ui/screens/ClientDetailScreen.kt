package com.therapist.assistant.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.therapist.assistant.data.Appointment
import com.therapist.assistant.data.AppointmentStatus
import com.therapist.assistant.data.QuestionnaireResult
import com.therapist.assistant.ui.components.*
import com.therapist.assistant.ui.theme.Amber500
import com.therapist.assistant.ui.theme.Emerald500
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.compose.ui.platform.LocalContext
import com.therapist.assistant.assessment.AssessmentCatalog
import com.therapist.assistant.util.ClientFileExporter
import com.therapist.assistant.util.JalaliDate
import kotlinx.coroutines.launch
import java.io.File
import com.therapist.assistant.viewmodel.AppointmentViewModel
import java.text.NumberFormat
import java.util.*

private sealed class TimelineItem(val time: Long) {
    data class Session(val appt: Appointment) : TimelineItem(appt.dateTimeMillis)
    data class Questionnaire(val q: QuestionnaireResult) : TimelineItem(q.createdAt)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(
    viewModel: AppointmentViewModel,
    clientId: Long,
    onBack: () -> Unit,
    onEditClient: () -> Unit,
    onAddAppointment: () -> Unit,
    onEditAppointment: (Long) -> Unit,
    onPhq9: () -> Unit = {},
    onDass21: () -> Unit = {},
    onDocuments: () -> Unit = {}
) {
    var clientName by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }
    var birthDateMillis by remember { mutableLongStateOf(0L) }
    var address by remember { mutableStateOf("") }
    var clientNotes by remember { mutableStateOf("") }

    val appointments by viewModel.getAppointmentsForClient(clientId)
        .collectAsState(initial = emptyList())
    val questionnaires by viewModel.getQuestionnaires(clientId)
        .collectAsState(initial = emptyList())
    val clinicName by viewModel.clinicName.collectAsState()
    val clinicPhone by viewModel.clinicPhone.collectAsState()
    val clinicAddress by viewModel.clinicAddress.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

        val currency = remember { NumberFormat.getNumberInstance(Locale("fa")) }

    LaunchedEffect(clientId) {
        viewModel.getClient(clientId)?.let {
            clientName = it.name
            clientPhone = it.phone
            nationalId = it.nationalId
            birthDateMillis = it.birthDateMillis
            address = it.address
            clientNotes = it.notes
        }
    }

    val timeline = remember(appointments, questionnaires) {
        buildList {
            appointments.forEach { add(TimelineItem.Session(it)) }
            questionnaires.forEach { add(TimelineItem.Questionnaire(it)) }
        }.sortedByDescending { it.time }
    }

    val patterns = remember(appointments) { detectPatterns(appointments) }
    val trendSeries = remember(questionnaires) {
        val colors = listOf(
            Color(0xFF0284C7), // sky
            Color(0xFF059669), // emerald
            Color(0xFF7C3AED), // violet
            Color(0xFFEA580C), // orange
            Color(0xFFDB2777), // pink
            Color(0xFF0D9488), // teal
            Color(0xFFCA8A04), // amber
            Color(0xFF4F46E5)  // indigo
        )
        questionnaires
            .groupBy { it.type }
            .entries
            .mapIndexed { index, (type, list) ->
                val sorted = list.sortedBy { it.createdAt }.takeLast(12)
                TrendSeries(
                    type = type,
                    label = AssessmentCatalog.byId(type)?.titleFa ?: type,
                    scores = sorted.map { it.score },
                    times = sorted.map { it.createdAt },
                    color = colors[index % colors.size]
                )
            }
            .filter { it.scores.isNotEmpty() }
            .sortedByDescending { it.scores.size }
    }

    val totalPaid = appointments.filter { it.isPaid }.sumOf { it.fee }
    val totalUnpaid = appointments.filter { !it.isPaid && it.status != AppointmentStatus.CANCELLED }.sumOf { it.fee }
    val completedCount = appointments.count { it.status == AppointmentStatus.COMPLETED }
    val cancelledCount = appointments.count { it.status == AppointmentStatus.CANCELLED }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddAppointment,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("نوبت جدید", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                GradientTopBar(
                    title = clientName.ifEmpty { "مراجع" },
                    subtitle = clientPhone.ifBlank { null },
                    onBack = onBack,
                    actions = {
                        IconButton(onClick = {
                            scope.launch {
                                val client = viewModel.getClient(clientId) ?: return@launch
                                val appts = appointments
                                val qs = questionnaires
                                val workshops = viewModel.getClientWorkshopLinks(clientId)
                                val file = File(context.cacheDir, "client_file_${clientId}_${System.currentTimeMillis()}.pdf")
                                ClientFileExporter.exportPdf(
                                    file = file,
                                    clinicName = clinicName,
                                    clinicPhone = clinicPhone,
                                    clinicAddress = clinicAddress,
                                    client = client,
                                    appointments = appts,
                                    questionnaires = qs,
                                    workshopLinks = workshops
                                )
                                val uri = FileProvider.getUriForFile(
                                    context, "${context.packageName}.fileprovider", file
                                )
                                context.startActivity(
                                    Intent.createChooser(
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "application/pdf"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            putExtra(Intent.EXTRA_SUBJECT, "پرونده ${client.name}")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        },
                                        "چاپ / اشتراک پرونده کامل"
                                    )
                                )
                            }
                        }) {
                            Icon(Icons.Default.Print, null, tint = androidx.compose.ui.graphics.Color.White)
                        }
                        IconButton(onClick = onPhq9) {
                            Icon(Icons.Default.Assignment, null, tint = androidx.compose.ui.graphics.Color.White)
                        }
                        IconButton(onClick = onDass21) {
                            Icon(Icons.Default.Psychology, null, tint = androidx.compose.ui.graphics.Color.White)
                        }
                        IconButton(onClick = onDocuments) {
                            Icon(Icons.Default.Description, null, tint = androidx.compose.ui.graphics.Color.White)
                        }
                        IconButton(onClick = onEditClient) {
                            Icon(Icons.Default.Edit, null, tint = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                )
            }
            // Hero profile card
            item {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(56.dp).clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    clientName.take(1).ifEmpty { "؟" },
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(clientName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                if (nationalId.isNotBlank()) Text("کد ملی: $nationalId", style = MaterialTheme.typography.bodyMedium)
                                if (birthDateMillis > 0) Text("تولد: ${JalaliDate.formatDate(birthDateMillis)}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        if (address.isNotBlank()) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.LocationOn, null, Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(6.dp))
                                Text(address, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        if (clientNotes.isNotBlank()) {
                            Text(clientNotes, style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Stats row
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatMini(Modifier.weight(1f), "$completedCount", "انجام‌شده", MaterialTheme.colorScheme.secondary)
                    StatMini(Modifier.weight(1f), "$cancelledCount", "لغو", MaterialTheme.colorScheme.error)
                    StatMini(Modifier.weight(1f), currency.format(totalPaid), "دریافت", MaterialTheme.colorScheme.primary)
                    StatMini(Modifier.weight(1f), currency.format(totalUnpaid), "بدهی", Color(0xFFE65100))
                }
            }

            // Pattern alerts
            if (patterns.isNotEmpty()) {
                item {
                    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        patterns.forEach { pattern ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(Modifier.width(10.dp))
                                    Text(pattern, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // PHQ-9 trend chart
            if (trendSeries.isNotEmpty()) {
                item {
                    Card(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ShowChart, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text("روند آزمون‌ها", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.weight(1f))
                                Text("${trendSeries.size} آزمون", style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(12.dp))
                            MultiTrendChart(series = trendSeries, modifier = Modifier.fillMaxWidth().height(180.dp))
                            Spacer(Modifier.height(8.dp))
                            trendSeries.take(6).forEach { s ->
                                val last = s.scores.last()
                                val delta = if (s.scores.size >= 2) s.scores.last() - s.scores[s.scores.size - 2] else 0
                                val deltaText = when {
                                    s.scores.size < 2 -> "—"
                                    delta < 0 -> "↓ ${-delta}"
                                    delta > 0 -> "↑ $delta"
                                    else -> "="
                                }
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(10.dp).clip(CircleShape).background(s.color))
                                    Spacer(Modifier.width(8.dp))
                                    Text(s.label, Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, maxLines = 1)
                                    Text("آخرین: $last  $deltaText", style = MaterialTheme.typography.labelMedium, color = s.color)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            } else {
                item {
                    OutlinedCard(
                        onClick = onPhq9,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Assignment, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("ثبت اولین PHQ-9", fontWeight = FontWeight.SemiBold)
                                Text("نمودار روند بعد از ثبت نمرات نمایش داده می‌شود",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.ChevronLeft, null)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            // Timeline header
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("خط‌زمانی", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text("${timeline.size}", Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            if (timeline.isEmpty()) {
                item {
                    Card(
                        Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            Modifier.padding(32.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Timeline, null, Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Text("هنوز رویدادی ثبت نشده", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            items(timeline, key = {
                when (it) {
                    is TimelineItem.Session -> "s-${it.appt.id}"
                    is TimelineItem.Questionnaire -> "q-${it.q.id}"
                }
            }) { item ->
                when (item) {
                    is TimelineItem.Session -> SessionTimelineCard(
                        appt = item.appt,
                        currency = currency,
                        onClick = { onEditAppointment(item.appt.id) }
                    )
                    is TimelineItem.Questionnaire -> QuestionnaireTimelineCard(item.q)
                }
            }
        }
    }
}

@Composable
private fun StatMini(modifier: Modifier, value: String, label: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(Modifier.padding(10.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.9f))
        }
    }
}

@Composable
private fun SessionTimelineCard(appt: Appointment, currency: NumberFormat, onClick: () -> Unit) {
    val statusColor = when (appt.status) {
        AppointmentStatus.SCHEDULED -> MaterialTheme.colorScheme.primary
        AppointmentStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
        AppointmentStatus.CANCELLED -> MaterialTheme.colorScheme.error
        AppointmentStatus.RESCHEDULED -> Color(0xFF6A1B9A)
    }
    val statusLabel = when (appt.status) {
        AppointmentStatus.SCHEDULED -> "زمان‌بندی"
        AppointmentStatus.COMPLETED -> "انجام‌شده"
        AppointmentStatus.CANCELLED -> "لغو"
        AppointmentStatus.RESCHEDULED -> "جابجا"
    }

    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        // Timeline rail
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
            Box(Modifier.size(12.dp).clip(CircleShape).background(statusColor))
            Box(Modifier.width(2.dp).height(8.dp).background(MaterialTheme.colorScheme.outlineVariant))
        }
        Spacer(Modifier.width(10.dp))
        Card(
            onClick = onClick,
            modifier = Modifier.weight(1f).animateContentSize(),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(JalaliDate.formatDateTime(appt.dateTimeMillis), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.15f)) {
                        Text(statusLabel, Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.Bold)
                    }
                }
                if (appt.fee > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (appt.isPaid) Icons.Default.CheckCircle else Icons.Default.Schedule,
                            null, Modifier.size(16.dp),
                            tint = if (appt.isPaid) MaterialTheme.colorScheme.secondary else Color(0xFFE65100)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${currency.format(appt.fee)} تومان — ${if (appt.isPaid) "پرداخت‌شده" else "بدهکار"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                if (appt.noteTags.isNotBlank()) {
                    Text("🏷 ${appt.noteTags}", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary)
                }
                val soapBits = listOfNotNull(
                    appt.soapSubjective.takeIf { it.isNotBlank() }?.let { "S: ${it.take(80)}" },
                    appt.soapAssessment.takeIf { it.isNotBlank() }?.let { "A: ${it.take(80)}" },
                    appt.soapPlan.takeIf { it.isNotBlank() }?.let { "P: ${it.take(60)}" },
                    appt.sessionNotes.takeIf { it.isNotBlank() }?.take(100)
                )
                if (soapBits.isNotEmpty()) {
                    Text(soapBits.joinToString("\n"), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 4, overflow = TextOverflow.Ellipsis)
                }
                if (appt.cancelReason.isNotBlank()) {
                    Text("دلیل: ${appt.cancelReason}", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun QuestionnaireTimelineCard(q: QuestionnaireResult) {
    val label = when (q.type) {
        "PHQ9" -> "PHQ-9"
        "DASS21" -> "DASS-21"
        "CONSENT" -> "رضایت‌نامه"
        else -> q.type
    }
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
            Box(Modifier.size(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary))
            Box(Modifier.width(2.dp).height(8.dp).background(MaterialTheme.colorScheme.outlineVariant))
        }
        Spacer(Modifier.width(10.dp))
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f))
        ) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Assignment, null, tint = MaterialTheme.colorScheme.tertiary)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(label, fontWeight = FontWeight.Bold)
                    Text(JalaliDate.formatDateTime(q.createdAt), style = MaterialTheme.typography.labelMedium)
                }
                if (q.type == "PHQ9" || q.type == "DASS21") {
                    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.tertiary) {
                        Text("${q.score}", Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onTertiary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private data class TrendSeries(
    val type: String,
    val label: String,
    val scores: List<Int>,
    val times: List<Long>,
    val color: Color
)

@Composable
private fun MultiTrendChart(series: List<TrendSeries>, modifier: Modifier = Modifier) {
    val grid = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier = modifier) {
        if (series.isEmpty()) return@Canvas
        val maxScore = series.maxOf { s -> s.scores.maxOrNull() ?: 0 }.coerceAtLeast(10).toFloat()
        val maxPoints = series.maxOf { it.scores.size }.coerceAtLeast(1)
        val padL = 28f
        val padR = 12f
        val padT = 12f
        val padB = 24f
        val w = size.width - padL - padR
        val h = size.height - padT - padB

        val ticks = 5
        for (i in 0..ticks) {
            val v = maxScore * i / ticks
            val y = padT + h * (1 - v / maxScore)
            drawLine(grid.copy(alpha = 0.45f), Offset(padL, y), Offset(padL + w, y), strokeWidth = 1f)
        }

        series.forEach { s ->
            if (s.scores.isEmpty()) return@forEach
            val pts = s.scores.mapIndexed { i, sc ->
                val x = if (maxPoints == 1) padL + w / 2
                else padL + (i.toFloat() / (maxPoints - 1).coerceAtLeast(1)) * w
                val y = padT + h * (1 - sc / maxScore)
                Offset(x, y)
            }
            if (pts.size == 1) {
                drawCircle(s.color, radius = 7f, center = pts[0])
            } else {
                val path = Path().apply {
                    moveTo(pts.first().x, pts.first().y)
                    for (i in 1 until pts.size) {
                        val prev = pts[i - 1]
                        val cur = pts[i]
                        val cx = (prev.x + cur.x) / 2
                        cubicTo(cx, prev.y, cx, cur.y, cur.x, cur.y)
                    }
                }
                drawPath(path, s.color, style = Stroke(width = 3.5f, cap = StrokeCap.Round))
                pts.forEach { drawCircle(s.color, radius = 5f, center = it) }
                pts.forEach { drawCircle(Color.White, radius = 2.2f, center = it) }
            }
        }
    }
}

private fun detectPatterns(appointments: List<Appointment>): List<String> {
    val result = mutableListOf<String>()
    val sorted = appointments.sortedByDescending { it.dateTimeMillis }

    // 3 consecutive cancellations
    var streak = 0
    for (a in sorted) {
        if (a.status == AppointmentStatus.CANCELLED) {
            streak++
            if (streak >= 3) {
                result.add("۳ جلسه لغو پشت‌سرهم — نیاز به پیگیری")
                break
            }
        } else break
    }

    // unpaid older than ~2 months
    val twoMonthsAgo = System.currentTimeMillis() - 60L * 24 * 60 * 60 * 1000
    val oldDebt = sorted.filter {
        !it.isPaid && it.status != AppointmentStatus.CANCELLED && it.dateTimeMillis < twoMonthsAgo && it.fee > 0
    }
    if (oldDebt.isNotEmpty()) {
        val sum = oldDebt.sumOf { it.fee }
        val fmt = NumberFormat.getNumberInstance(Locale("fa"))
        result.add("بدهی بیش از ۲ ماه: ${fmt.format(sum)} تومان (${oldDebt.size} جلسه)")
    }

    // high cancel rate
    if (appointments.size >= 5) {
        val rate = appointments.count { it.status == AppointmentStatus.CANCELLED }.toFloat() / appointments.size
        if (rate >= 0.4f) {
            result.add("نرخ لغو بالا (${(rate * 100).toInt()}٪) — بررسی برنامه زمانی")
        }
    }
    return result
}
