package com.therapist.assistant.util

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

/**
 * خروجی PDF راست‌چین با جداول خط‌دار، نمودار روند فارسی و پاورقی.
 */
object PdfExporter {

    private const val PAGE_W = 595
    private const val PAGE_H = 842
    private const val MARGIN = 36f
    private val RIGHT get() = PAGE_W - MARGIN

    data class TableColumn(val title: String, val weight: Float)
    data class Brand(
        val title: String,
        val subtitle: String = "",
        val phone: String = "",
        val address: String = ""
    )

    data class ChartSeries(
        val label: String,
        val scores: List<Int>,
        val color: Int
    )

    /** جدول نام‌دار برای بخش‌های میانی سند */
    data class NamedTable(
        val title: String,
        val columns: List<TableColumn>,
        val rows: List<List<String>>
    )

    fun writeSimplePdf(file: File, title: String, lines: List<String>) {
        writeStyledPdf(
            file = file,
            brand = Brand(title = title),
            introLines = emptyList(),
            tableColumns = listOf(TableColumn("شرح", 1f)),
            tableRows = lines.filter { it.isNotBlank() }.map { listOf(it) },
            footer = "تولیدشده توسط درمانگرنو"
        )
    }

    fun writeStyledPdf(
        file: File,
        brand: Brand,
        introLines: List<String> = emptyList(),
        tableColumns: List<TableColumn> = emptyList(),
        tableRows: List<List<String>> = emptyList(),
        extraTables: List<NamedTable> = emptyList(),
        sections: List<Pair<String, List<String>>> = emptyList(),
        chartSeries: List<ChartSeries> = emptyList(),
        chartTitle: String = "نمودار روند نمرات آزمون‌ها",
        footer: String = "درمانگرنو — گزارش محرمانه بالینی"
    ) {
        val doc = PdfDocument()
        var pageIndex = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageIndex).create())
        var canvas = page.canvas
        var y = MARGIN

        fun newPage() {
            doc.finishPage(page)
            pageIndex++
            page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageIndex).create())
            canvas = page.canvas
            y = MARGIN
            drawFooter(canvas, footer, pageIndex)
        }

        fun need(space: Float) {
            if (y + space > PAGE_H - 52f) newPage()
        }

        drawFooter(canvas, footer, pageIndex)
        y = drawHeader(canvas, brand, y)
        y += 8f

        introLines.forEach { line ->
            need(16f)
            y = drawTextLine(canvas, line, y, bodyPaint())
        }
        if (introLines.isNotEmpty()) y += 10f

        if (tableColumns.isNotEmpty()) {
            need(32f)
            y = drawTable(canvas, tableColumns, tableRows, y) {
                newPage()
            }
            y += 14f
        }

        // نمودار رنگی با برچسب فارسی
        if (chartSeries.isNotEmpty()) {
            need(220f)
            y = drawSectionTitle(canvas, chartTitle, y)
            y = drawTrendChart(canvas, chartSeries, y) { newPage() }
            y += 12f
        }

        // جداول اضافی با عنوان و خطوط کامل
        extraTables.forEach { nt ->
            if (nt.rows.isEmpty() && nt.columns.isEmpty()) return@forEach
            need(40f)
            y = drawSectionTitle(canvas, nt.title, y)
            y = drawTable(canvas, nt.columns, nt.rows, y) { newPage() }
            y += 12f
        }

        // بخش‌های متنی — اگر خطوط شبیه جدول (| یا : جفت‌ستونی) باشند خودکار جدول می‌شوند
        sections.forEach { (title, lines) ->
            need(28f)
            y = drawSectionTitle(canvas, title, y)
            val auto = tryAutoTable(lines)
            if (auto != null) {
                y = drawTable(canvas, auto.first, auto.second, y) { newPage() }
            } else {
                lines.forEach { line ->
                    need(15f)
                    y = drawTextLine(canvas, line, y, bodyPaint())
                }
            }
            y += 8f
        }

        doc.finishPage(page)
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
    }

    /**
     * تشخیص خودکار جدول از خطوط با جداکننده « | » یا جفت‌های «کلید: مقدار».
     */
    private fun tryAutoTable(lines: List<String>): Pair<List<TableColumn>, List<List<String>>>? {
        if (lines.isEmpty()) return null
        val pipeLines = lines.filter { it.contains(" | ") }
        if (pipeLines.size >= 2) {
            val cols = pipeLines.first().split(" | ").map { it.trim() }
            if (cols.size < 2) return null
            val weights = List(cols.size) { 1f / cols.size }
            val columns = cols.mapIndexed { i, t -> TableColumn(t, weights[i]) }
            val rows = pipeLines.drop(1).map { line ->
                val parts = line.split(" | ").map { it.trim() }
                // pad/truncate to col count
                List(cols.size) { i -> parts.getOrNull(i).orEmpty() }
            }
            return columns to rows
        }
        // جفت‌های «عنوان: مقدار»
        val pairs = lines.mapNotNull { line ->
            val idx = line.indexOf(':')
            if (idx > 0 && idx < line.length - 1) {
                listOf(line.substring(0, idx).trim(), line.substring(idx + 1).trim())
            } else null
        }
        if (pairs.size >= 2 && pairs.size == lines.size) {
            return listOf(TableColumn("عنوان", 0.4f), TableColumn("مقدار", 0.6f)) to pairs
        }
        return null
    }

    private fun drawHeader(canvas: Canvas, brand: Brand, startY: Float): Float {
        var y = startY
        canvas.drawRect(
            RectF(0f, 0f, PAGE_W.toFloat(), 72f),
            Paint().apply {
                color = android.graphics.Color.parseColor("#0284C7")
                style = Paint.Style.FILL
            }
        )
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#E0F2FE")
            textSize = 10f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(brand.title, RIGHT, 32f, titlePaint)
        if (brand.subtitle.isNotBlank()) {
            canvas.drawText(brand.subtitle, RIGHT, 48f, subPaint)
        }
        y = 88f
        val metaPaint = bodyPaint().apply {
            color = android.graphics.Color.parseColor("#334155")
        }
        if (brand.phone.isNotBlank()) {
            canvas.drawText("تلفن: ${brand.phone}", RIGHT, y, metaPaint)
            y += 14f
        }
        if (brand.address.isNotBlank()) {
            canvas.drawText("آدرس: ${brand.address}", RIGHT, y, metaPaint)
            y += 14f
        }
        canvas.drawLine(
            MARGIN, y + 4f, RIGHT, y + 4f,
            Paint().apply {
                color = android.graphics.Color.parseColor("#BAE6FD")
                strokeWidth = 1.5f
            }
        )
        return y + 16f
    }

    private fun drawFooter(canvas: Canvas, footer: String, pageNo: Int) {
        val rightP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#94A3B8")
            textSize = 9f
            textAlign = Paint.Align.RIGHT
        }
        val leftP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#94A3B8")
            textSize = 9f
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText(footer, RIGHT, PAGE_H - 18f, rightP)
        canvas.drawText("صفحه $pageNo", MARGIN, PAGE_H - 18f, leftP)
    }

    private fun drawSectionTitle(canvas: Canvas, title: String, y: Float): Float {
        val bg = RectF(MARGIN, y, RIGHT, y + 22f)
        canvas.drawRoundRect(bg, 4f, 4f, Paint().apply {
            color = android.graphics.Color.parseColor("#E0F2FE")
            style = Paint.Style.FILL
        })
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#0C4A6E")
            textSize = 11.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(title, RIGHT - 8f, y + 15f, paint)
        return y + 30f
    }

    private fun bodyPaint() = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#1E293B")
        textSize = 10.5f
        textAlign = Paint.Align.RIGHT
    }

    private fun drawTextLine(canvas: Canvas, text: String, y: Float, paint: Paint): Float {
        val maxWidth = PAGE_W - 2 * MARGIN
        val words = text.split(" ")
        var line = ""
        var cy = y
        val p = Paint(paint).apply { textAlign = Paint.Align.RIGHT }
        for (w in words) {
            val trial = if (line.isEmpty()) w else "$line $w"
            if (p.measureText(trial) > maxWidth && line.isNotEmpty()) {
                canvas.drawText(line, RIGHT, cy, p)
                cy += 14f
                line = w
            } else {
                line = trial
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line, RIGHT, cy, p)
            cy += 14f
        }
        return cy
    }

    private fun drawTable(
        canvas: Canvas,
        columns: List<TableColumn>,
        rows: List<List<String>>,
        startY: Float,
        onNewPage: () -> Unit
    ): Float {
        if (columns.isEmpty()) return startY
        var y = startY
        val totalWeight = columns.sumOf { it.weight.toDouble() }.toFloat().coerceAtLeast(0.01f)
        val tableWidth = PAGE_W - 2 * MARGIN
        val colWidths = columns.map { tableWidth * (it.weight / totalWeight) }
        val borderPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0284C7")
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
        }
        val vLinePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#CBD5E1")
            strokeWidth = 0.9f
        }
        val hLinePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#CBD5E1")
            strokeWidth = 0.9f
        }
        val altBg = Paint().apply {
            color = android.graphics.Color.parseColor("#F1F5F9")
            style = Paint.Style.FILL
        }
        val headerBg = Paint().apply {
            color = android.graphics.Color.parseColor("#0284C7")
            style = Paint.Style.FILL
        }

        fun drawHeaderRow(at: Float): Float {
            val h = 22f
            canvas.drawRect(MARGIN, at, RIGHT, at + h, headerBg)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
            }
            var xRight = RIGHT
            columns.forEachIndexed { i, col ->
                canvas.drawText(col.title, xRight - 5f, at + 15f, paint)
                xRight -= colWidths[i]
                if (i < columns.lastIndex) {
                    canvas.drawLine(xRight, at, xRight, at + h, Paint().apply {
                        color = android.graphics.Color.parseColor("#7DD3FC")
                        strokeWidth = 0.8f
                    })
                }
            }
            return at + h
        }

        val tableTop = y
        y = drawHeaderRow(y)
        val cellPaint = bodyPaint()

        rows.forEachIndexed { rowIndex, row ->
            val cellH = 18f
            if (y + cellH > PAGE_H - 52f) {
                // بستن کادر صفحه قبل
                canvas.drawRect(MARGIN, tableTop, RIGHT, y, borderPaint)
                onNewPage()
                y = MARGIN + 8f
                y = drawHeaderRow(y)
            }
            if (rowIndex % 2 == 1) {
                canvas.drawRect(MARGIN, y, RIGHT, y + cellH, altBg)
            }
            var xRight = RIGHT
            columns.indices.forEach { i ->
                val value = row.getOrNull(i).orEmpty()
                val maxW = colWidths[i] - 10f
                val clipped = if (cellPaint.measureText(value) > maxW) {
                    var s = value
                    while (s.isNotEmpty() && cellPaint.measureText("$s…") > maxW) s = s.dropLast(1)
                    "$s…"
                } else value
                canvas.drawText(clipped, xRight - 5f, y + 13f, cellPaint)
                xRight -= colWidths[i]
                if (i < columns.lastIndex) {
                    canvas.drawLine(xRight, y, xRight, y + cellH, vLinePaint)
                }
            }
            canvas.drawLine(MARGIN, y + cellH, RIGHT, y + cellH, hLinePaint)
            y += cellH
        }
        canvas.drawRect(MARGIN, tableTop, RIGHT, y, borderPaint)
        // خطوط عمودی بیرونی از قبل در border هستند
        return y
    }

    /**
     * نمودار خطی رنگی با برچسب فارسی، خطوط ضخیم و واضح.
     */
    private fun drawTrendChart(
        canvas: Canvas,
        series: List<ChartSeries>,
        startY: Float,
        onNewPage: () -> Unit
    ): Float {
        val chartH = 168f
        val legendRows = (series.size + 1) / 2
        val legendH = 24f * legendRows + 14f
        val totalH = chartH + legendH + 28f
        if (startY + totalH > PAGE_H - 52f) {
            onNewPage()
        }
        var y = if (startY + totalH > PAGE_H - 52f) MARGIN + 8f else startY

        val boxTop = y
        val left = MARGIN + 32f
        val right = RIGHT - 10f
        val top = y + 10f
        val bottom = top + chartH
        val w = right - left
        val h = bottom - top

        // قاب
        canvas.drawRoundRect(
            RectF(MARGIN, boxTop, RIGHT, bottom + legendH + 10f),
            6f, 6f,
            Paint().apply {
                color = android.graphics.Color.parseColor("#F8FAFC")
                style = Paint.Style.FILL
            }
        )
        canvas.drawRoundRect(
            RectF(MARGIN, boxTop, RIGHT, bottom + legendH + 10f),
            6f, 6f,
            Paint().apply {
                color = android.graphics.Color.parseColor("#94A3B8")
                style = Paint.Style.STROKE
                strokeWidth = 1.2f
            }
        )

        val maxScore = series.maxOf { s -> s.scores.maxOrNull() ?: 0 }.coerceAtLeast(10).toFloat()
        val maxPoints = series.maxOf { it.scores.size }.coerceAtLeast(1)

        val gridPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#CBD5E1")
            strokeWidth = 1f
        }
        val axisPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#64748B")
            strokeWidth = 1.5f
        }
        val yLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#475569")
            textSize = 9f
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val xLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#475569")
            textSize = 8.5f
            textAlign = Paint.Align.CENTER
        }

        // شبکه و برچسب محور Y (نمره) — فارسی
        val ticks = 5
        for (i in 0..ticks) {
            val v = maxScore * i / ticks
            val gy = bottom - h * (v / maxScore)
            canvas.drawLine(left, gy, right, gy, gridPaint)
            // اعداد فارسی
            canvas.drawText(toFaDigits(v.toInt().toString()), left - 6f, gy + 3.5f, yLabelPaint)
        }
        // محورها
        canvas.drawLine(left, top, left, bottom, axisPaint)
        canvas.drawLine(left, bottom, right, bottom, axisPaint)

        // برچسب محور
        val axisTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#0C4A6E")
            textSize = 9f
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("نمره", left - 6f, top - 2f, axisTitlePaint)
        canvas.drawText("ترتیب اجرا ←", right, bottom + 12f, xLabelPaint.apply {
            textAlign = Paint.Align.RIGHT
        })

        // شماره نقاط روی محور X
        if (maxPoints <= 12) {
            for (i in 0 until maxPoints) {
                val px = if (maxPoints == 1) left + w / 2
                else left + (i.toFloat() / (maxPoints - 1).coerceAtLeast(1)) * w
                canvas.drawText(toFaDigits((i + 1).toString()), px, bottom + 11f, xLabelPaint)
            }
        }

        // خطوط سری — ضخیم و واضح
        series.forEach { s ->
            if (s.scores.isEmpty()) return@forEach
            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = s.color
                style = Paint.Style.STROKE
                strokeWidth = 3.8f
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }
            // سایه کم‌رنگ زیر خط برای وضوح بیشتر
            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.argb(40, 0, 0, 0)
                style = Paint.Style.STROKE
                strokeWidth = 5.5f
                strokeCap = Paint.Cap.ROUND
            }
            val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = s.color
                style = Paint.Style.FILL
            }
            val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                style = Paint.Style.FILL
            }
            val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = s.color
                textSize = 8f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val pts = s.scores.mapIndexed { i, sc ->
                val px = if (maxPoints == 1) left + w / 2
                else left + (i.toFloat() / (maxPoints - 1).coerceAtLeast(1)) * w
                val py = bottom - h * (sc / maxScore)
                Triple(px, py, sc)
            }

            if (pts.size == 1) {
                canvas.drawCircle(pts[0].first, pts[0].second, 6.5f, pointPaint)
                canvas.drawCircle(pts[0].first, pts[0].second, 3f, ringPaint)
                canvas.drawText(toFaDigits(pts[0].third.toString()), pts[0].first, pts[0].second - 9f, scorePaint)
            } else {
                val path = Path()
                path.moveTo(pts[0].first, pts[0].second)
                for (i in 1 until pts.size) {
                    val prev = pts[i - 1]
                    val cur = pts[i]
                    val midX = (prev.first + cur.first) / 2f
                    path.cubicTo(midX, prev.second, midX, cur.second, cur.first, cur.second)
                }
                canvas.drawPath(path, shadowPaint)
                canvas.drawPath(path, linePaint)
                pts.forEach { (px, py, sc) ->
                    canvas.drawCircle(px, py, 6f, pointPaint)
                    canvas.drawCircle(px, py, 2.8f, ringPaint)
                    // نمره فارسی بالای نقطه
                    canvas.drawText(toFaDigits(sc.toString()), px, py - 9f, scorePaint)
                }
            }
        }

        // راهنما — برچسب فارسی کامل
        var ly = bottom + 20f
        val legPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9.5f
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subLegPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f
            textAlign = Paint.Align.RIGHT
            color = android.graphics.Color.parseColor("#64748B")
        }
        val colW = (RIGHT - MARGIN) / 2f
        series.forEachIndexed { idx, s ->
            val col = idx % 2
            val row = idx / 2
            val lx = RIGHT - col * colW - 6f
            val ty = ly + row * 24f
            val swatch = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = s.color
                style = Paint.Style.FILL
            }
            // مربع رنگی واضح
            canvas.drawRoundRect(RectF(lx - 14f, ty - 9f, lx - 2f, ty + 1f), 2f, 2f, swatch)
            legPaint.color = android.graphics.Color.parseColor("#0F172A")
            val shortLabel = if (s.label.length > 26) s.label.take(25) + "…" else s.label
            val last = s.scores.lastOrNull()
            val first = s.scores.firstOrNull()
            val delta = if (last != null && first != null && s.scores.size >= 2) last - first else null
            val deltaFa = when {
                delta == null -> ""
                delta > 0 -> " — افزایش ${toFaDigits(delta.toString())}"
                delta < 0 -> " — کاهش ${toFaDigits((-delta).toString())}"
                else -> " — بدون تغییر"
            }
            canvas.drawText(shortLabel, lx - 18f, ty, legPaint)
            if (last != null) {
                canvas.drawText(
                    "آخرین نمره: ${toFaDigits(last.toString())}$deltaFa",
                    lx - 18f,
                    ty + 10f,
                    subLegPaint
                )
            }
        }
        // فضای legend دوخطی برای هر آیتم
        val actualLegendH = legendRows * 24f + 8f
        return bottom + 20f + actualLegendH + 8f
    }

    private fun toFaDigits(s: String): String = buildString {
        for (c in s) {
            append(if (c in '0'..'9') '۰' + (c - '0') else c)
        }
    }
}
