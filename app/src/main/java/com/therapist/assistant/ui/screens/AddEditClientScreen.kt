package com.therapist.assistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.therapist.assistant.ui.components.JalaliDatePickerDialog
import com.therapist.assistant.util.JalaliDate
import com.therapist.assistant.viewmodel.AppointmentViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditClientScreen(
    viewModel: AppointmentViewModel,
    clientId: Long?,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }
    var birthDateMillis by remember { mutableLongStateOf(0L) }
    var education by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var nationalIdError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val isEdit = clientId != null
    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale("fa"))

    LaunchedEffect(clientId) {
        if (clientId != null) {
            viewModel.getClient(clientId)?.let {
                name = it.name
                phone = it.phone
                nationalId = it.nationalId
                birthDateMillis = it.birthDateMillis
                address = it.address
                education = it.education
                notes = it.notes
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "ویرایش مراجع" else "افزودن مراجع") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = null },
                label = { Text("نام مراجع *") },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    if (it.all { c -> c.isDigit() } && it.length <= 11) phone = it
                },
                label = { Text("شماره تلفن (اختیاری)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("۰۹۱۲...") }
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = nationalId,
                onValueChange = {
                    if (it.all { c -> c.isDigit() } && it.length <= 10) {
                        nationalId = it
                        nationalIdError = null
                    }
                },
                label = { Text("کد ملی (اختیاری)") },
                isError = nationalIdError != null,
                supportingText = nationalIdError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("۱۰ رقم") }
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = if (birthDateMillis > 0) JalaliDate.formatDate(birthDateMillis) else "",
                onValueChange = {},
                readOnly = true,
                label = { Text("تاریخ تولد (اختیاری)") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Row {
                        if (birthDateMillis > 0) {
                            TextButton(onClick = { birthDateMillis = 0L }) { Text("پاک") }
                        }
                        TextButton(onClick = { showDatePicker = true }) { Text("انتخاب") }
                    }
                },
                placeholder = { Text("انتخاب تاریخ") }
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("آدرس محل زندگی (اختیاری)") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                maxLines = 3
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = education,
                onValueChange = { education = it },
                label = { Text("مدرک تحصیلی (اختیاری)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("دیپلم، کارشناسی، ...") }
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("یادداشت") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                maxLines = 4
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (name.trim().isEmpty()) {
                        nameError = "نام الزامی است"
                        return@Button
                    }
                    if (phone.isNotEmpty() && !phone.matches(Regex("^0\\d{10}$"))) {
                        nameError = "شماره تلفن باید ۱۱ رقم و با ۰ شروع شود"
                        return@Button
                    }
                    if (nationalId.isNotEmpty() && nationalId.length != 10) {
                        nationalIdError = "کد ملی باید ۱۰ رقم باشد"
                        return@Button
                    }
                    viewModel.saveClient(
                        id = clientId,
                        name = name,
                        phone = phone,
                        nationalId = nationalId,
                        birthDateMillis = birthDateMillis,
                        address = address,
                        education = education,
                        notes = notes,
                        onSaved = { onBack() },
                        onLimitReached = { nameError = "سقف نسخه رایگان: حداکثر ۵۰ مراجع" }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEdit) "ذخیره تغییرات" else "ثبت مراجع")
            }
        }
    }

    if (showDatePicker) {
        JalaliDatePickerDialog(
            initialMillis = if (birthDateMillis > 0) birthDateMillis else System.currentTimeMillis(),
            onDismiss = { showDatePicker = false },
            onConfirm = {
                birthDateMillis = it
                showDatePicker = false
            }
        )
    }
}
