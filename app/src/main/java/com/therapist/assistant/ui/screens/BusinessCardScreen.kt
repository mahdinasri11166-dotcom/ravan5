package com.therapist.assistant.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.therapist.assistant.ui.components.GradientTopBar
import com.therapist.assistant.ui.components.SoftCard
import com.therapist.assistant.util.QrUtil
import com.therapist.assistant.viewmodel.AppointmentViewModel

private data class CardTheme(
    val id: String,
    val title: String,
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val onPrimary: Color,
    val qrModule: Int
)

private val cardThemes = listOf(
    CardTheme("sky", "آبی حرفه‌ای", Color(0xFF0284C7), Color(0xFF0C4A6E), Color(0xFFE0F2FE), Color.White, 0xFF0C4A6E.toInt()),
    CardTheme("rose", "رز کلینیک", Color(0xFFBE123C), Color(0xFF881337), Color(0xFFFFE4E6), Color.White, 0xFF881337.toInt()),
    CardTheme("emerald", "زمردی", Color(0xFF059669), Color(0xFF064E3B), Color(0xFFD1FAE5), Color.White, 0xFF064E3B.toInt()),
    CardTheme("violet", "بنفش آرام", Color(0xFF7C3AED), Color(0xFF4C1D95), Color(0xFFEDE9FE), Color.White, 0xFF4C1D95.toInt()),
    CardTheme("night", "مشکی لوکس", Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFFF1F5F9), Color.White, 0xFF0F172A.toInt())
)

@Composable
fun BusinessCardScreen(viewModel: AppointmentViewModel, onBack: () -> Unit) {
    val clinicName by viewModel.clinicName.collectAsState()
    val clinicPhone by viewModel.clinicPhone.collectAsState()
    val clinicAddress by viewModel.clinicAddress.collectAsState()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var displayName by remember(clinicName) { mutableStateOf(clinicName.ifBlank { "مرکز گل سرخ" }) }
    var role by remember { mutableStateOf("روان‌شناس / مشاور") }
    var phone by remember(clinicPhone) { mutableStateOf(clinicPhone) }
    var email by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var address by remember(clinicAddress) { mutableStateOf(clinicAddress) }
    var tagline by remember { mutableStateOf("همراه شما در مسیر سلامت روان") }
    var themeId by remember { mutableStateOf("sky") }
    val theme = cardThemes.first { it.id == themeId }

    val vcard = remember(displayName, role, phone, email, website, address, tagline) {
        QrUtil.vCard(
            fullName = displayName,
            organization = displayName,
            title = role,
            phone = phone,
            email = email,
            address = address,
            url = website,
            note = tagline
        )
    }
    val qrBitmap = remember(vcard, theme.qrModule) {
        QrUtil.encode(vcard, moduleColor = theme.qrModule)
    }

    val shareText = remember(displayName, role, phone, email, website, address, tagline) {
        buildString {
            appendLine("━━━━━━━━━━━━━━━━")
            appendLine(displayName)
            appendLine(role)
            if (tagline.isNotBlank()) appendLine(tagline)
            appendLine("━━━━━━━━━━━━━━━━")
            if (phone.isNotBlank()) appendLine("📞 $phone")
            if (email.isNotBlank()) appendLine("✉ $email")
            if (website.isNotBlank()) appendLine("🌐 $website")
            if (address.isNotBlank()) appendLine("📍 $address")
            appendLine()
            appendLine("نوبت‌دهی با هماهنگی قبلی")
            appendLine("— درمانگرنو —")
        }
    }

    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            GradientTopBar(title = "کارت ویزیت دیجیتال", subtitle = "قالب شیک + QR مخاطب", onBack = onBack)
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ——— پیش‌نمایش کارت ———
                Box(
                    Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    Column {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(theme.primary, theme.secondary)
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.2f))
                                            .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            displayName.take(1).ifBlank { "د" },
                                            color = theme.onPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp
                                        )
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            displayName,
                                            color = theme.onPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        )
                                        Text(role, color = theme.onPrimary.copy(alpha = 0.9f), fontSize = 13.sp)
                                    }
                                }
                                if (tagline.isNotBlank()) {
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        tagline,
                                        color = theme.onPrimary.copy(alpha = 0.85f),
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (phone.isNotBlank()) InfoLine(Icons.Outlined.Phone, phone, theme.primary)
                                if (email.isNotBlank()) InfoLine(Icons.Outlined.Email, email, theme.primary)
                                if (website.isNotBlank()) InfoLine(Icons.Outlined.Language, website, theme.primary)
                                if (address.isNotBlank()) InfoLine(Icons.Default.LocationOn, address, theme.primary)
                                if (listOf(phone, email, website, address).all { it.isBlank() }) {
                                    Text(
                                        "اطلاعات تماس را پایین صفحه کامل کنید",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    Modifier
                                        .shadow(4.dp, RoundedCornerShape(12.dp))
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(theme.accent)
                                        .padding(8.dp)
                                ) {
                                    Image(
                                        bitmap = qrBitmap.asImageBitmap(),
                                        contentDescription = "QR vCard",
                                        modifier = Modifier.size(108.dp)
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text("اسکن = ذخیره مخاطب", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                // ——— قالب رنگ ———
                SoftCard {
                    Text("قالب ظاهری", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        cardThemes.forEach { t ->
                            val selected = themeId == t.id
                            val borderC by animateColorAsState(
                                if (selected) t.primary else Color.Transparent,
                                label = "border"
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(t.primary, t.secondary)))
                                        .border(2.5.dp, borderC, CircleShape)
                                        .then(
                                            Modifier.clip(CircleShape)
                                        )
                                ) {
                                    Surface(
                                        onClick = { themeId = t.id },
                                        color = Color.Transparent,
                                        modifier = Modifier.fillMaxSize()
                                    ) {}
                                }
                                Text(t.title, fontSize = 9.sp, textAlign = TextAlign.Center, maxLines = 1)
                            }
                        }
                    }
                }

                // ——— ویرایش فیلدها ———
                SoftCard {
                    Text("اطلاعات کارت", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("نام مرکز / درمانگر") },
                        leadingIcon = { Icon(Icons.Outlined.Badge, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("عنوان شغلی") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tagline,
                        onValueChange = { tagline = it },
                        label = { Text("شعار / معرفی کوتاه") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("تلفن") },
                        leadingIcon = { Icon(Icons.Outlined.Phone, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("ایمیل (اختیاری)") },
                        leadingIcon = { Icon(Icons.Outlined.Email, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = website,
                        onValueChange = { website = it },
                        label = { Text("وب‌سایت / اینستاگرام (اختیاری)") },
                        leadingIcon = { Icon(Icons.Outlined.Language, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("آدرس") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // ——— اقدامات ———
                SoftCard {
                    Text("اقدام سریع", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                context.startActivity(
                                    Intent.createChooser(
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "کارت ویزیت — $displayName")
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                        },
                                        "اشتراک کارت ویزیت"
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Share, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("اشتراک")
                        }
                        OutlinedButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(shareText))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("کپی")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    if (phone.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                val digits = phone.filter { it.isDigit() || it == '+' }
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digits")))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Call, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("تماس آزمایشی با همین شماره")
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "QR از نوع vCard است؛ با اسکن در اکثر گوشی‌ها مستقیم به مخاطبین اضافه می‌شود.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoLine(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 12.sp, color = Color(0xFF334155), maxLines = 2)
    }
}
