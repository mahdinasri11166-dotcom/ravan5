package com.therapist.assistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.therapist.assistant.ui.components.GradientTopBar
import com.therapist.assistant.ui.components.SoftCard

private data class Protocol(val title: String, val steps: List<String>)

private val protocols = listOf(
    Protocol("اضطراب", listOf(
        "بررسی علائم و محرک‌های هفته",
        "تمرین تنفس یا آرام‌سازی ۵ دقیقه",
        "شناسایی فکر خودکار",
        "تکلیف مواجهه کوچک برای هفته بعد"
    )),
    Protocol("افسردگی", listOf(
        "خلق و انرژی از ۱ تا ۱۰",
        "فعال‌سازی رفتاری: ۱ فعالیت لذت‌بخش",
        "بررسی خواب و دارو",
        "هدف کوچک و قابل اندازه‌گیری"
    )),
    Protocol("زوج‌درمانی", listOf(
        "موضوع جلسه از زبان هر دو",
        "قانون صحبت بدون قطع کردن",
        "نیاز اصلی پشت تعارض",
        "تکلیف ارتباط روزانه ۱۰ دقیقه‌ای"
    )),
    Protocol("جلسه اول", listOf(
        "معرفی حدود محرمانگی",
        "فرم اولیه / رضایت‌نامه",
        "شکایت اصلی و اهداف",
        "برنامه تعداد جلسات پیشنهادی"
    ))
)

@Composable
fun ProtocolsScreen(onBack: () -> Unit) {
    var selected by remember { mutableStateOf(protocols.first()) }
    val checked = remember(selected) { mutableStateListOf(*Array(selected.steps.size) { false }) }

    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            GradientTopBar(title = "پروتکل‌های آماده", subtitle = "چک‌لیست جلسه", onBack = onBack)
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        protocols.forEach { p ->
                            FilterChip(
                                selected = selected.title == p.title,
                                onClick = { selected = p },
                                label = { Text(p.title) }
                            )
                        }
                    }
                }
                item {
                    SoftCard {
                        Text(selected.title, fontWeight = FontWeight.Bold)
                        Text("موارد را حین جلسه تیک بزنید", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                items(selected.steps.size) { i ->
                    SoftCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = checked[i], onCheckedChange = { checked[i] = it })
                            Text(selected.steps[i], modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
