package com.therapist.assistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.therapist.assistant.ui.components.GradientTopBar
import com.therapist.assistant.ui.components.SoftCard
import com.therapist.assistant.viewmodel.AppointmentViewModel

@Composable
fun PrivacyScreen(viewModel: AppointmentViewModel, onBack: () -> Unit, requireAccept: Boolean = false) {
    val accepted by viewModel.privacyAccepted.collectAsState()

    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            GradientTopBar(title = "حریم خصوصی", subtitle = "درمانگرنو", onBack = if (requireAccept && !accepted) null else onBack)
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SoftCard {
                    Text("تعهد محرمانگی", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        """
داده‌های مراجعین (نام، تلفن، یادداشت بالینی، آزمون‌ها و اطلاعات مالی) فقط روی همین دستگاه ذخیره می‌شود.

• هیچ سروری برای همگام‌سازی پیش‌فرض وجود ندارد.
• پایگاه‌داده با رمزنگاری محلی محافظت می‌شود.
• پشتیبان‌گیری فقط با اقدام شما و از طریق اشتراک‌گذاری فایل انجام می‌شود.
• یادآوری‌ها به‌صورت اعلان محلی یا لینک پیامک/واتساپ (بدون سرور ما) ارسال می‌شوند.

شما مسئول نگهداری امن گوشی، پین/بیومتریک، و فایل‌های پشتیبان هستید.
                        """.trimIndent()
                    )
                }
                SoftCard {
                    Text("رضایت استفاده", fontWeight = FontWeight.Bold)
                    Text("با ادامه استفاده تأیید می‌کنید که از این اپ برای مدیریت حرفه‌ای نوبت و پرونده سبک استفاده می‌کنید و مسئولیت رعایت اخلاق حرفه‌ای و قوانین مرتبط با خودتان است.")
                }
                if (!accepted) {
                    Button(
                        onClick = {
                            viewModel.setPrivacyAccepted(true)
                            onBack()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("می‌پذیرم و ادامه می‌دهم") }
                } else {
                    Text("شما این سیاست را پذیرفته‌اید.", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
