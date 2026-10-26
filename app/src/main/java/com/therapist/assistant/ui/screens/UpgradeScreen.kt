package com.therapist.assistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.therapist.assistant.ui.components.GradientTopBar
import com.therapist.assistant.ui.components.SoftCard
import com.therapist.assistant.viewmodel.AppointmentViewModel

@Composable
fun UpgradeScreen(viewModel: AppointmentViewModel, onBack: () -> Unit) {
    val isPro by viewModel.isPro.collectAsState()
    val trialStart by viewModel.trialStartMillis.collectAsState()
    val trialActive = viewModel.isTrialActive()
    val daysLeft = viewModel.trialDaysRemaining()
    var code by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf<String?>(null) }
    val freeTests = AppointmentViewModel.FREE_ASSESSMENT_IDS.size
    val freeClients = AppointmentViewModel.FREE_CLIENT_LIMIT
    val trialDays = AppointmentViewModel.TRIAL_DAYS

    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            GradientTopBar(
                title = "نسخه‌ها و ارتقا",
                subtitle = when {
                    isPro -> "نسخه حرفه‌ای فعال"
                    trialActive -> "آزمایشی — $daysLeft روز مانده"
                    else -> "نسخه رایگان"
                },
                onBack = onBack
            )
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                if (isPro) {
                    SoftCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("اشتراک حرفه‌ای فعال است", fontWeight = FontWeight.Bold)
                                Text("همه قابلیت‌ها در دسترس شماست.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // ——— مقایسه سه لایه ———
                SoftCard {
                    Text("مقایسه نسخه‌ها", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    TierBlock(
                        title = "رایگان",
                        badge = "شروع",
                        lines = listOf(
                            "تا $freeClients مراجع",
                            "$freeTests آزمون غربالگری پایه",
                            "نوبت و پرونده",
                            "بکاپ محلی روی گوشی",
                            "یادآور نوبت",
                            "قفل پین"
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    TierBlock(
                        title = "آزمایشی $trialDays روزه",
                        badge = "همه Pro",
                        highlight = true,
                        lines = listOf(
                            "مراجع نامحدود",
                            "همه آزمون‌های بانک",
                            "PDF و نمودار روند",
                            "کارگاه + اکسل رسمی",
                            "اسناد و فرم‌های A5",
                            "بکاپ + اشتراک فایل"
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    TierBlock(
                        title = "حرفه‌ای (دائمی با کد)",
                        badge = "کامل",
                        lines = listOf(
                            "همان امکانات آزمایشی بدون محدودیت زمان",
                            "مناسب مطب و مرکز",
                            "فعال‌سازی با کد پس از خرید"
                        )
                    )
                }

                if (!isPro) {
                    if (!trialActive && trialStart == 0L) {
                        SoftCard {
                            Text("$trialDays روز نسخه حرفه‌ای رایگان", fontWeight = FontWeight.Bold)
                            Text(
                                "همه قابلیت‌ها را دو هفته کامل تست کنید. پس از آن به نسخه رایگان برمی‌گردید مگر با کد فعال‌سازی.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.startTrial() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("شروع دوره آزمایشی $trialDays روزه")
                            }
                        }
                    } else if (trialActive) {
                        SoftCard {
                            Text("دوره آزمایشی فعال", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("$daysLeft روز تا پایان دسترسی حرفه‌ای")
                            LinearProgressIndicator(
                                progress = { (daysLeft.toFloat() / trialDays).coerceIn(0.05f, 1f) },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            )
                        }
                    } else if (trialStart > 0L) {
                        SoftCard {
                            Text("دوره آزمایشی تمام شده", fontWeight = FontWeight.SemiBold)
                            Text("برای ادامه امکانات کامل، کد فعال‌سازی را وارد کنید.")
                        }
                    }

                    SoftCard {
                        Text("فعال‌سازی با کد", fontWeight = FontWeight.Bold)
                        Text("پس از خرید، کد را وارد کنید. کد در هیچ جای اپ نمایش داده نمی‌شود.", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it; msg = null },
                            label = { Text("کد فعال‌سازی") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                msg = if (viewModel.unlockProWithCode(code)) "با موفقیت فعال شد ✓"
                                else "کد نامعتبر است"
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) { Text("فعال‌سازی نسخه حرفه‌ای") }
                        msg?.let {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                it,
                                color = if (it.contains("✓")) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                SoftCard {
                    Text("یادآوری مهم", fontWeight = FontWeight.Bold)
                    Text(
                        "آزمون‌ها ابزار غربالگری‌اند و جایگزین تشخیص بالینی نیستند. اطلاعات مراجع محرمانه است و بکاپ فقط روی دستگاه/حساب خودتان ذخیره می‌شود.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TierBlock(
    title: String,
    badge: String,
    lines: List<String>,
    highlight: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (highlight) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                AssistChip(onClick = {}, label = { Text(badge) }, enabled = false)
            }
            Spacer(Modifier.height(6.dp))
            lines.forEach { line ->
                Row(Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.dp))
                    Text(line, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
