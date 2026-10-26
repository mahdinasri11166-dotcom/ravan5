package com.therapist.assistant.util

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

/**
 * فرم‌های رسمی A5 (۴۲۰×۵۹۵) با جدول‌بندی زیبا و راست‌چین.
 */
object DocumentFormPdf {

    // A5 portrait in points
    private const val PAGE_W = 420
    private const val PAGE_H = 595
    private const val M = 22f
    private val R get() = PAGE_W - M

    data class Field(val label: String, val value: String = "", val lines: Int = 1)
    data class Section(
        val title: String,
        val fields: List<Field> = emptyList(),
        val paragraphs: List<String> = emptyList(),
        val checklist: List<String> = emptyList()
    )
    data class FormSpec(
        val docTitle: String,
        val docCode: String = "",
        val clinicName: String,
        val clinicPhone: String = "",
        val clinicAddress: String = "",
        val dateText: String,
        val intro: String = "",
        val sections: List<Section>,
        val closing: List<String> = emptyList(),
        val signatureLabels: List<String> = listOf("امضای مراجع", "امضای درمانگر")
    )

    fun write(file: File, spec: FormSpec) {
        val doc = PdfDocument()
        var pageNo = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNo).create())
        var canvas = page.canvas
        var y = M

        fun newPage() {
            doc.finishPage(page)
            pageNo++
            page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNo).create())
            canvas = page.canvas
            y = M
            drawPageChrome(canvas, spec, pageNo)
            y = 56f
        }

        fun need(h: Float) {
            if (y + h > PAGE_H - 36f) newPage()
        }

        drawPageChrome(canvas, spec, pageNo)
        y = 56f

        // عنوان سند
        y = drawDocTitle(canvas, spec.docTitle, spec.docCode, y)
        y += 4f

        // متا: مرکز / تاریخ
        y = drawMetaTable(canvas, spec, y)
        y += 8f

        if (spec.intro.isNotBlank()) {
            need(28f)
            y = drawParagraph(canvas, spec.intro, y)
            y += 6f
        }

        spec.sections.forEach { sec ->
            need(24f)
            y = drawSectionHeader(canvas, sec.title, y)
            if (sec.paragraphs.isNotEmpty()) {
                sec.paragraphs.forEach { p ->
                    need(16f)
                    y = drawParagraph(canvas, p, y)
                }
                y += 3f
            }
            if (sec.fields.isNotEmpty()) {
                y = drawFieldsTable(canvas, sec.fields, y) { newPage() }
                y += 6f
            }
            if (sec.checklist.isNotEmpty()) {
                y = drawChecklist(canvas, sec.checklist, y) { newPage() }
                y += 6f
            }
        }

        if (spec.closing.isNotEmpty()) {
            y += 4f
            spec.closing.forEach { c ->
                need(16f)
                y = drawParagraph(canvas, c, y)
            }
        }

        // امضا
        need(70f)
        y += 8f
        y = drawSignatures(canvas, spec.signatureLabels, y)

        doc.finishPage(page)
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
    }

    private fun drawPageChrome(canvas: Canvas, spec: FormSpec, pageNo: Int) {
        // نوار بالا
        canvas.drawRect(
            0f, 0f, PAGE_W.toFloat(), 40f,
            Paint().apply {
                color = android.graphics.Color.parseColor("#0C4A6E")
                style = Paint.Style.FILL
            }
        )
        val t = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(spec.clinicName.ifBlank { "درمانگرنو" }, R, 18f, t)
        val s = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#BAE6FD")
            textSize = 8f
            textAlign = Paint.Align.RIGHT
        }
        val sub = listOfNotNull(
            spec.clinicPhone.takeIf { it.isNotBlank() }?.let { "تلفن: $it" },
            "فرم رسمی A5"
        ).joinToString("  |  ")
        canvas.drawText(sub, R, 32f, s)

        // پاورقی
        val f = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#94A3B8")
            textSize = 7.5f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("محرمانه — درمانگرنو — صفحه ${fa(pageNo)}", R, PAGE_H - 12f, f)
        // خط دور صفحه
        canvas.drawRect(
            8f, 44f, PAGE_W - 8f, PAGE_H - 22f,
            Paint().apply {
                color = android.graphics.Color.parseColor("#CBD5E1")
                style = Paint.Style.STROKE
                strokeWidth = 0.8f
            }
        )
    }

    private fun drawDocTitle(canvas: Canvas, title: String, code: String, y: Float): Float {
        val bg = RectF(M, y, R, y + 26f)
        canvas.drawRoundRect(bg, 4f, 4f, Paint().apply {
            color = android.graphics.Color.parseColor("#E0F2FE")
            style = Paint.Style.FILL
        })
        canvas.drawRoundRect(bg, 4f, 4f, Paint().apply {
            color = android.graphics.Color.parseColor("#0284C7")
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
        })
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#0C4A6E")
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(title, PAGE_W / 2f, y + 17f, p)
        if (code.isNotBlank()) {
            val c = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.parseColor("#64748B")
                textSize = 7.5f
                textAlign = Paint.Align.LEFT
            }
            canvas.drawText(code, M + 6f, y + 17f, c)
        }
        return y + 30f
    }

    private fun drawMetaTable(canvas: Canvas, spec: FormSpec, y0: Float): Float {
        val rows = listOf(
            listOf("تاریخ تنظیم", spec.dateText, "مرکز / کلینیک", spec.clinicName),
            listOf(
                "تلفن مرکز",
                spec.clinicPhone.ifBlank { "—" },
                "آدرس",
                spec.clinicAddress.ifBlank { "—" }
            )
        )
        val colW = floatArrayOf(0.18f, 0.32f, 0.18f, 0.32f).map { it * (R - M) }
        var y = y0
        val cellH = 16f
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#0F172A")
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val valPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#334155")
            textSize = 8f
            textAlign = Paint.Align.RIGHT
        }
        val line = Paint().apply {
            color = android.graphics.Color.parseColor("#94A3B8")
            strokeWidth = 0.7f
        }
        val top = y
        rows.forEach { row ->
            var xR = R
            row.forEachIndexed { i, cell ->
                val w = colW[i]
                val paint = if (i % 2 == 0) headerPaint else valPaint
                val text = clip(cell, paint, w - 6f)
                canvas.drawText(text, xR - 3f, y + 11f, paint)
                xR -= w
                if (i < row.lastIndex) canvas.drawLine(xR, y, xR, y + cellH, line)
            }
            y += cellH
            canvas.drawLine(M, y, R, y, line)
        }
        canvas.drawRect(M, top, R, y, Paint().apply {
            color = android.graphics.Color.parseColor("#0284C7")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        })
        return y + 4f
    }

    private fun drawSectionHeader(canvas: Canvas, title: String, y: Float): Float {
        canvas.drawRect(
            M, y, R, y + 16f,
            Paint().apply {
                color = android.graphics.Color.parseColor("#0284C7")
                style = Paint.Style.FILL
            }
        )
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(title, R - 6f, y + 11.5f, p)
        return y + 18f
    }

    private fun drawFieldsTable(
        canvas: Canvas,
        fields: List<Field>,
        y0: Float,
        onNewPage: () -> Unit
    ): Float {
        var y = y0
        val labelW = (R - M) * 0.32f
        val valueW = (R - M) * 0.68f
        val linePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#CBD5E1")
            strokeWidth = 0.8f
        }
        val border = Paint().apply {
            color = android.graphics.Color.parseColor("#64748B")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#0F172A")
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#334155")
            textSize = 8.5f
            textAlign = Paint.Align.RIGHT
        }
        val alt = Paint().apply {
            color = android.graphics.Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }
        val top = y
        fields.forEachIndexed { idx, f ->
            val h = 14f * f.lines.coerceIn(1, 6) + 6f
            if (y + h > PAGE_H - 36f) {
                canvas.drawRect(M, top, R, y, border)
                onNewPage()
                y = 56f
            }
            if (idx % 2 == 1) {
                canvas.drawRect(M, y, R, y + h, alt)
            }
            // label
            canvas.drawText(clip(f.label, labelPaint, labelW - 6f), R - 4f, y + 12f, labelPaint)
            // vertical divider
            canvas.drawLine(R - labelW, y, R - labelW, y + h, linePaint)
            // value or blank lines for writing
            if (f.value.isNotBlank()) {
                val lines = wrap(f.value, valuePaint, valueW - 8f)
                lines.take(f.lines).forEachIndexed { li, ln ->
                    canvas.drawText(ln, R - labelW - 4f, y + 12f + li * 12f, valuePaint)
                }
            } else {
                // dotted write lines
                val dot = Paint().apply {
                    color = android.graphics.Color.parseColor("#E2E8F0")
                    strokeWidth = 0.6f
                }
                for (i in 0 until f.lines) {
                    val ly = y + 14f + i * 12f
                    canvas.drawLine(M + 4f, ly, R - labelW - 4f, ly, dot)
                }
            }
            canvas.drawLine(M, y + h, R, y + h, linePaint)
            y += h
        }
        canvas.drawRect(M, top, R, y, border)
        return y
    }

    private fun drawChecklist(
        canvas: Canvas,
        items: List<String>,
        y0: Float,
        onNewPage: () -> Unit
    ): Float {
        var y = y0
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#1E293B")
            textSize = 8.5f
            textAlign = Paint.Align.RIGHT
        }
        val box = Paint().apply {
            color = android.graphics.Color.parseColor("#0284C7")
            style = Paint.Style.STROKE
            strokeWidth = 1.1f
        }
        items.forEach { item ->
            if (y + 16f > PAGE_H - 36f) onNewPage().also { y = 56f }
            // checkbox on the right
            canvas.drawRect(R - 12f, y + 1f, R - 2f, y + 11f, box)
            canvas.drawText(item, R - 16f, y + 10f, p)
            y += 15f
        }
        return y
    }

    private fun drawParagraph(canvas: Canvas, text: String, y0: Float): Float {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#1E293B")
            textSize = 8.5f
            textAlign = Paint.Align.RIGHT
        }
        var y = y0
        wrap(text, p, R - M - 4f).forEach { line ->
            canvas.drawText(line, R, y + 10f, p)
            y += 12f
        }
        return y + 2f
    }

    private fun drawSignatures(canvas: Canvas, labels: List<String>, y0: Float): Float {
        val n = labels.size.coerceAtLeast(1)
        val gap = 8f
        val boxW = ((R - M) - gap * (n - 1)) / n
        var y = y0
        canvas.drawText(
            "تأیید و امضا",
            R,
            y + 10f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.parseColor("#0C4A6E")
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
            }
        )
        y += 14f
        val border = Paint().apply {
            color = android.graphics.Color.parseColor("#94A3B8")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val lp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#475569")
            textSize = 8f
            textAlign = Paint.Align.CENTER
        }
        labels.forEachIndexed { i, label ->
            val left = M + i * (boxW + gap)
            val right = left + boxW
            canvas.drawRoundRect(RectF(left, y, right, y + 48f), 3f, 3f, border)
            canvas.drawText(label, (left + right) / 2f, y + 42f, lp)
            // خط امضا
            canvas.drawLine(left + 10f, y + 28f, right - 10f, y + 28f, Paint().apply {
                color = android.graphics.Color.parseColor("#CBD5E1")
                strokeWidth = 0.8f
            })
        }
        return y + 52f
    }

    private fun wrap(text: String, paint: Paint, maxW: Float): List<String> {
        val words = text.split(" ")
        val out = mutableListOf<String>()
        var line = ""
        for (w in words) {
            val t = if (line.isEmpty()) w else "$line $w"
            if (paint.measureText(t) > maxW && line.isNotEmpty()) {
                out += line
                line = w
            } else line = t
        }
        if (line.isNotEmpty()) out += line
        return out.ifEmpty { listOf("") }
    }

    private fun clip(s: String, paint: Paint, maxW: Float): String {
        if (paint.measureText(s) <= maxW) return s
        var t = s
        while (t.isNotEmpty() && paint.measureText("$t…") > maxW) t = t.dropLast(1)
        return "$t…"
    }

    private fun fa(n: Int): String = n.toString().map {
        if (it in '0'..'9') '۰' + (it - '0') else it
    }.joinToString("")
}
