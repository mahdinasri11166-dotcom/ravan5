package com.therapist.assistant.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.therapist.assistant.ui.components.GradientTopBar
import com.therapist.assistant.ui.components.SoftCard
import com.therapist.assistant.util.DocumentFormPdf
import com.therapist.assistant.util.DocumentTemplates
import com.therapist.assistant.util.BackupHelper
import com.therapist.assistant.viewmodel.AppointmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun DocumentsScreen(
    viewModel: AppointmentViewModel,
    clientId: Long?,
    onBack: () -> Unit
) {
    val clients by viewModel.clients.collectAsState()
    val clinicName by viewModel.clinicName.collectAsState()
    val clinicPhone by viewModel.clinicPhone.collectAsState()
    val clinicAddress by viewModel.clinicAddress.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedId by remember { mutableStateOf(clientId) }
    val client = clients.find { it.id == selectedId }
    val appts by viewModel.allAppointments.collectAsState()
    val sessionCount = appts.count { it.clientId == selectedId }
    var busyId by remember { mutableStateOf<String?>(null) }
    var filter by remember { mutableStateOf<String?>(null) }

    val categories = remember {
        DocumentTemplates.catalog.map { it.category }.distinct()
    }
    val templates = remember(filter) {
        if (filter == null) DocumentTemplates.catalog
        else DocumentTemplates.catalog.filter { it.category == filter }
    }

    fun exportPdf(docId: String, title: String) {
        if (!viewModel.hasProAccess()) {
            Toast.makeText(context, "PDF اسناد در نسخه حرفه‌ای / آزمایشی ۱۴روزه فعال است.", Toast.LENGTH_LONG).show()
            return
        }
        val c = client ?: return
        busyId = docId
        scope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    val dir = File(context.cacheDir, "docs").apply { mkdirs() }
                    val out = File(dir, "${docId}_${c.id}_${System.currentTimeMillis()}.pdf")
                    val spec = DocumentTemplates.buildForm(
                        id = docId,
                        client = c,
                        clinicName = clinicName,
                        clinicPhone = clinicPhone,
                        clinicAddress = clinicAddress,
                        sessionCount = sessionCount
                    )
                    DocumentFormPdf.write(out, spec)
                    out
                }
                val uri = BackupHelper.fileProviderUri(context, file)
                context.startActivity(
                    Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_SUBJECT, title)
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "فرم رسمی A5 — $title — ${c.name}"
                            )
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        },
                        "اشتراک PDF فرم"
                    )
                )
            } catch (e: Exception) {
                Toast.makeText(context, "خطا: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                busyId = null
            }
        }
    }

    fun shareText(docId: String, title: String) {
        val c = client ?: return
        val spec = DocumentTemplates.buildForm(
            id = docId,
            client = c,
            clinicName = clinicName,
            clinicPhone = clinicPhone,
            clinicAddress = clinicAddress,
            sessionCount = sessionCount
        )
        val text = DocumentTemplates.plainTextSummary(spec)
        context.startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, title)
                    putExtra(Intent.EXTRA_TEXT, text)
                },
                "اشتراک متن"
            )
        )
    }

    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            GradientTopBar(
                title = "اسناد و فرم‌های رسمی",
                subtitle = client?.name ?: "مراجع را انتخاب کنید",
                onBack = onBack
            )
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    SoftCard {
                        Text("انتخاب مراجع", fontWeight = FontWeight.Bold)
                        Text(
                            "فرم‌ها روی کاغذ A5 با جدول‌بندی حرفه‌ای ساخته می‌شوند.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        clients.filter { !it.isArchived }.take(40).forEach { c ->
                            FilterChip(
                                selected = selectedId == c.id,
                                onClick = { selectedId = c.id },
                                label = { Text(c.name) },
                                modifier = Modifier.padding(end = 6.dp, bottom = 4.dp)
                            )
                        }
                    }
                }
                item {
                    SoftCard {
                        Text("دسته", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = filter == null,
                                onClick = { filter = null },
                                label = { Text("همه") }
                            )
                        }
                        categories.forEach { cat ->
                            FilterChip(
                                selected = filter == cat,
                                onClick = { filter = cat },
                                label = { Text(cat) },
                                modifier = Modifier.padding(end = 6.dp, bottom = 4.dp)
                            )
                        }
                    }
                }
                items(templates, key = { it.id }) { def ->
                    SoftCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(def.title, fontWeight = FontWeight.Bold)
                                Text(
                                    def.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    def.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { exportPdf(def.id, def.title) },
                                enabled = client != null && busyId == null,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (busyId == def.id) {
                                    CircularProgressIndicator(
                                        Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Icon(Icons.Default.PictureAsPdf, null, Modifier.size(18.dp))
                                }
                                Spacer(Modifier.width(6.dp))
                                Text("PDF فرم A5")
                            }
                            OutlinedButton(
                                onClick = { shareText(def.id, def.title) },
                                enabled = client != null,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("متن")
                            }
                        }
                    }
                }
            }
        }
    }
}
