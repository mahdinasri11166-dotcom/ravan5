package com.therapist.assistant.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.therapist.assistant.ui.components.*
import com.therapist.assistant.ui.theme.Amber500
import com.therapist.assistant.ui.theme.Emerald500
import com.therapist.assistant.util.JalaliDate
import com.therapist.assistant.viewmodel.AppointmentViewModel
import java.text.NumberFormat
import java.util.*

@Composable
fun ReportsScreen(viewModel: AppointmentViewModel, onBack: () -> Unit) {
    val unpaid by viewModel.unpaidAppointments.collectAsState()
    val allAppts by viewModel.allAppointments.collectAsState()
    val clinicName by viewModel.clinicName.collectAsState()
    val context = LocalContext.current
    val currency = remember { NumberFormat.getNumberInstance(Locale("fa")) }
    val cal = remember { Calendar.getInstance() }
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH)
    var monthlyIncome by remember { mutableDoubleStateOf(0.0) }
    val totalUnpaid = remember(unpaid) { unpaid.sumOf { it.fee } }
    val monthName = JalaliDate.formatMonthYear(cal.timeInMillis)

    LaunchedEffect(Unit) { monthlyIncome = viewModel.getMonthlyIncome(year, month) }

    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            GradientTopBar(
                title = "گزارش‌ها",
                subtitle = monthName,
                onBack = onBack,
                actions = {
                    IconButton(onClick = {
                        val sb = StringBuilder()
                        sb.appendLine("گزارش مالی — $clinicName")
                        sb.appendLine("تاریخ: ${JalaliDate.formatDateLong(System.currentTimeMillis())}")
                        sb.appendLine("درآمد $monthName: ${currency.format(monthlyIncome)} تومان")
                        sb.appendLine("مجموع بدهکاری: ${currency.format(totalUnpaid)} تومان")
                        sb.appendLine()
                        allAppts.sortedByDescending { it.dateTimeMillis }.take(50).forEach {
                            sb.appendLine("${JalaliDate.formatDateTime(it.dateTimeMillis)} | ${it.clientName} | ${it.fee.toLong()} | ${if (it.isPaid) "پرداخت" else "بدهکار"}")
                        }
                        context.startActivity(
                            Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "گزارش مالی")
                                    putExtra(Intent.EXTRA_TEXT, sb.toString())
                                },
                                "اشتراک گزارش"
                            )
                        )
                    }) { Icon(Icons.Default.Share, null, tint = Color.White) }
                }
            )

            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SoftCard {
                    Text("درآمد این ماه", style = MaterialTheme.typography.labelLarge, color = Emerald500)
                    Text("${currency.format(monthlyIncome)} تومان", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Emerald500)
                }
                SoftCard {
                    Text("مجموع بدهی‌ها", style = MaterialTheme.typography.labelLarge, color = Amber500)
                    Text("${currency.format(totalUnpaid)} تومان", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Amber500)
                    Text("${JalaliDate.toFa(unpaid.size)} جلسه تسویه‌نشده", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                SoftCard {
                    Text("بدهکاران", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    if (unpaid.isEmpty()) {
                        Text("بدهکاری ثبت نشده", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        unpaid.groupBy { it.clientName }.forEach { (name, list) ->
                            val sum = list.sumOf { it.fee }
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(name, fontWeight = FontWeight.Medium)
                                Text("${currency.format(sum)} ت", color = Amber500, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                SoftCard {
                    Text("خلاصه جلسات", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("کل جلسات: ${JalaliDate.toFa(allAppts.size)}")
                    Text("انجام‌شده: ${JalaliDate.toFa(allAppts.count { it.status.name == "COMPLETED" })}")
                    Text("لغو‌شده: ${JalaliDate.toFa(allAppts.count { it.status.name == "CANCELLED" })}")
                }
                SoftCard {
                    Text("نتایج بالینی (Outcome)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("روند PHQ-9 و DASS-21 را در پرونده هر مراجع (خط‌زمانی) ببینید. برای گزارش تجمیعی، از اشتراک گزارش مالی استفاده کنید.")
                }
            }
        }
    }
}
