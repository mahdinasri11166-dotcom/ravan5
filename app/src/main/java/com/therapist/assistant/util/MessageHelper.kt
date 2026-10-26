package com.therapist.assistant.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object MessageHelper {
    fun buildReminderText(template: String, name: String, dateTimeMillis: Long): String {
        return template
            .replace("{name}", name)
            .replace("{date}", JalaliDate.formatDate(dateTimeMillis))
            .replace("{time}", JalaliDate.formatTime(dateTimeMillis))
    }

    fun openSms(context: Context, phone: String, text: String) {
        val p = phone.filter { it.isDigit() }
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$p")
            putExtra("sms_body", text)
        }
        context.startActivity(Intent.createChooser(intent, "ارسال پیامک"))
    }

    fun openWhatsApp(context: Context, phone: String, text: String) {
        var p = phone.filter { it.isDigit() }
        if (p.startsWith("0")) p = "98" + p.drop(1)
        val url = "https://wa.me/$p?text=${Uri.encode(text)}"
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    fun openEitaa(context: Context, phone: String, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "ارسال با ایتا / سایر"))
    }
}
