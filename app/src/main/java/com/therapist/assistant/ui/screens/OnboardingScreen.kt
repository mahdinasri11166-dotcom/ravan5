package com.therapist.assistant.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.therapist.assistant.viewmodel.AppointmentViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: AppointmentViewModel,
    onFinished: () -> Unit
) {
    val pages = listOf(
        Triple(Icons.Default.Favorite, "به درمانگرنو خوش آمدید", "ابزار حرفه‌ای مدیریت نوبت و مراجع برای روانشناسان"),
        Triple(Icons.Default.EventNote, "نوبت و یادآوری", "زمان‌بندی دقیق، نوبت تکراری و یادآوری پیامکی/واتساپ"),
        Triple(Icons.Default.AccountBalanceWallet, "دفتر مالی", "ثبت مبلغ، بدهکاری، صورتحساب و گزارش درآمد"),
        Triple(Icons.Default.Folder, "پرونده سبک", "یادداشت SOAP، پرسشنامه‌ها و خط‌زمانی مراجع"),
        Triple(Icons.Default.PrivacyTip, "حریم خصوصی", "داده‌ها فقط روی گوشی شماست. با شروع، سیاست حریم خصوصی را می‌پذیرید.")
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            val (icon, title, desc) = pages[page]
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(icon, null, Modifier.size(96.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(24.dp))
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(desc, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            repeat(pages.size) { i ->
                val selected = pagerState.currentPage == i
                Surface(
                    modifier = Modifier.padding(4.dp).size(if (selected) 10.dp else 8.dp),
                    shape = MaterialTheme.shapes.small,
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant
                ) {}
            }
        }
        Spacer(Modifier.height(24.dp))
        if (pagerState.currentPage < pages.lastIndex) {
            Button(
                onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                modifier = Modifier.fillMaxWidth()
            ) { Text("بعدی") }
        } else {
            Button(
                onClick = {
                    viewModel.seedSampleData()
                    viewModel.setPrivacyAccepted(true)
                    viewModel.setOnboardingDone(true)
                    onFinished()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("شروع با داده نمونه") }
            TextButton(
                onClick = {
                    viewModel.setPrivacyAccepted(true)
                    viewModel.setOnboardingDone(true)
                    onFinished()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("شروع بدون داده نمونه") }
        }
    }
}
