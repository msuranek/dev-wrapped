package com.devwrapped.render

import com.devwrapped.core.Archetype
import com.devwrapped.core.DeveloperProfile
import com.devwrapped.core.PersonalityResult
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.GradientPaint
import java.awt.Graphics2D
import java.awt.LinearGradientPaint
import java.awt.RadialGradientPaint
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.geom.Path2D
import java.awt.geom.Point2D
import java.awt.geom.RoundRectangle2D
import java.awt.geom.Area
import java.awt.image.BufferedImage
import java.nio.file.Path
import java.text.NumberFormat
import java.util.Locale
import javax.imageio.ImageIO

object CardRenderer {
    const val WIDTH = 1200
    const val HEIGHT = 1500

    private val foreground = Color(252, 250, 255)
    private val muted = Color(195, 188, 216)
    private val softMuted = Color(148, 139, 172)

    fun render(profile: DeveloperProfile, personality: PersonalityResult): BufferedImage {
        val image = BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            val palette = Palette.forArchetype(personality.archetype)
            paintBackground(g, palette, personality.archetype)
            paintContent(g, profile, personality, palette)
        } finally {
            g.dispose()
        }
        return image
    }

    fun writePng(image: BufferedImage, path: Path) {
        require(ImageIO.write(image, "png", path.toFile())) { "No PNG image writer is available." }
    }

    private fun paintBackground(g: Graphics2D, palette: Palette, archetype: Archetype?) {
        g.paint = LinearGradientPaint(
            0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(),
            floatArrayOf(0f, 0.52f, 1f),
            arrayOf(palette.backgroundTop, palette.backgroundMiddle, Color(8, 6, 20)),
        )
        g.fillRect(0, 0, WIDTH, HEIGHT)

        g.paint = RadialGradientPaint(
            Point2D.Float(1010f, 210f), 520f,
            floatArrayOf(0f, 0.58f, 1f),
            arrayOf(withAlpha(palette.primary, 92), withAlpha(palette.primary, 22), withAlpha(palette.primary, 0)),
        )
        g.fillRect(490, 0, 710, 730)

        g.paint = RadialGradientPaint(
            Point2D.Float(50f, 1370f), 470f,
            floatArrayOf(0f, 1f),
            arrayOf(withAlpha(palette.secondary, 54), withAlpha(palette.secondary, 0)),
        )
        g.fillRect(0, 900, 590, 600)

        paintMotif(g, archetype, palette)
        paintGrain(g, palette)
    }

    private fun paintContent(g: Graphics2D, profile: DeveloperProfile, personality: PersonalityResult, palette: Palette) {
        val margin = 78
        paintHeader(g, profile.periodLabel, margin, palette)

        g.font = Font("SansSerif", Font.BOLD, 20)
        g.color = palette.secondary
        g.drawString("YOUR DEVELOPER DNA", margin, 214)

        val title = personality.archetype?.title?.uppercase() ?: "NOT ENOUGH SIGNAL"
        val titleWidth = WIDTH - margin * 2 - 190
        g.font = fittingFont(g, title, Font.BOLD, 82, titleWidth)
        val titleLines = wrap(title, g.fontMetrics, titleWidth, 2)
        val titleLineHeight = (g.font.size * 0.98).toInt()
        g.paint = GradientPaint(margin.toFloat(), 260f, foreground, (WIDTH - margin).toFloat(), 430f, palette.highlight)
        titleLines.forEachIndexed { index, line -> g.drawString(line, margin, 302 + index * titleLineHeight) }

        val titleBottom = 302 + (titleLines.size - 1) * titleLineHeight
        val subtitle = personality.archetype?.subtitle ?: personality.explanation
        g.font = Font("SansSerif", Font.PLAIN, 27)
        g.color = muted
        val subtitleBottom = drawWrapped(g, subtitle, margin, titleBottom + 58, 780, 38, 2)

        paintDNAStamp(g, WIDTH - margin - 156, 244, personality.archetype, palette)
        val dividerY = maxOf(482, subtitleBottom + 62)
        g.paint = LinearGradientPaint(
            margin.toFloat(), dividerY.toFloat(), (WIDTH - margin).toFloat(), dividerY.toFloat(),
            floatArrayOf(0f, 0.5f, 1f),
            arrayOf(withAlpha(palette.primary, 180), withAlpha(palette.secondary, 100), withAlpha(palette.secondary, 0)),
        )
        g.fillRoundRect(margin, dividerY, WIDTH - margin * 2, 3, 3, 3)

        val gridTop = dividerY + 48
        val gap = 24
        val cardWidth = (WIDTH - margin * 2 - gap) / 2
        val cardHeight = 178
        val right = margin + cardWidth + gap
        val secondRow = gridTop + cardHeight + gap

        paintMetricCard(g, margin, gridTop, cardWidth, cardHeight, NumberFormat.getIntegerInstance(Locale.US).format(profile.totalCommits), "COMMITS", palette.primary)
        paintMetricCard(g, right, gridTop, cardWidth, cardHeight, "${profile.lateNightPercentage}%", "LATE-NIGHT COMMITS", palette.secondary)
        paintMetricCard(g, margin, secondRow, cardWidth, cardHeight, profile.mostActiveDay?.displayName() ?: "—", "MOST ACTIVE DAY", palette.tertiary)
        paintMetricCard(g, right, secondRow, cardWidth, cardHeight, profile.primaryLanguage ?: "—", "TOP LANGUAGE", palette.primary)

        val detailsY = secondRow + cardHeight + 38
        paintDetailStrip(g, profile, margin, detailsY, WIDTH - margin * 2, palette)
        paintAchievement(g, profile, personality.archetype, margin, detailsY + 218, palette)
        paintFooter(g, margin, palette)
    }

    private fun paintHeader(g: Graphics2D, periodLabel: String, margin: Int, palette: Palette) {
        g.color = palette.tertiary
        g.fillRoundRect(margin, 72, 12, 34, 12, 12)
        g.font = Font("SansSerif", Font.BOLD, 28)
        g.color = foreground
        g.drawString("DEV WRAPPED", margin + 28, 101)
        g.font = Font("SansSerif", Font.PLAIN, 16)
        g.color = softMuted
        g.drawString("LOCAL GIT RECAP", margin + 29, 127)

        val period = periodLabel.uppercase()
        g.font = Font("SansSerif", Font.BOLD, 16)
        val chipWidth = g.fontMetrics.stringWidth(period) + 42
        val chipX = WIDTH - margin - chipWidth
        g.color = Color(255, 255, 255, 18)
        g.fillRoundRect(chipX, 74, chipWidth, 44, 22, 22)
        g.color = Color(255, 255, 255, 42)
        g.stroke = BasicStroke(1f)
        g.drawRoundRect(chipX, 74, chipWidth, 44, 22, 22)
        g.color = foreground
        g.drawString(period, chipX + 21, 102)
    }

    private fun paintMetricCard(g: Graphics2D, x: Int, y: Int, width: Int, height: Int, value: String, label: String, accent: Color) {
        g.color = Color(255, 255, 255, 14)
        g.fill(RoundRectangle2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(), 34.0, 34.0))
        g.color = Color(255, 255, 255, 28)
        g.stroke = BasicStroke(1f)
        g.draw(RoundRectangle2D.Double(x + 0.5, y + 0.5, width - 1.0, height - 1.0, 34.0, 34.0))
        g.color = accent
        g.fillRoundRect(x + 24, y + 25, 46, 7, 7, 7)
        g.font = Font("SansSerif", Font.BOLD, 16)
        g.color = softMuted
        g.drawString(label, x + 24, y + 61)
        val fitted = fittingText(g, value, Font.BOLD, 62, 22, width - 48)
        g.font = fitted.font
        g.color = foreground
        g.drawString(fitted.text, x + 24, y + 137)
    }

    private fun paintDetailStrip(g: Graphics2D, profile: DeveloperProfile, x: Int, y: Int, width: Int, palette: Palette) {
        val height = 168
        g.color = Color(8, 5, 22, 118)
        g.fillRoundRect(x, y, width, height, 32, 32)
        g.color = Color(255, 255, 255, 25)
        g.drawRoundRect(x, y, width, height, 32, 32)

        val values = listOf(
            profile.activeDays.toString() to "ACTIVE DAYS",
            profile.longestStreakDays.toString() to "LONGEST STREAK",
            profile.fixCommits.toString() to "BUG-FIX COMMITS",
            (profile.mostActiveHour?.let { String.format("%02d:00", it) } ?: "—") to "PEAK COMMIT HOUR",
        )
        val columnWidth = width / values.size
        values.forEachIndexed { index, (value, label) ->
            val columnX = x + index * columnWidth
            if (index > 0) {
                g.color = Color(255, 255, 255, 24)
                g.drawLine(columnX, y + 35, columnX, y + height - 35)
            }
            val fitted = fittingText(g, value, Font.BOLD, 35, 16, columnWidth - 36)
            g.font = fitted.font
            g.color = if (index == 2) palette.secondary else foreground
            g.drawString(fitted.text, columnX + 22, y + 77)
            g.font = fittingFont(g, label, Font.BOLD, 13, columnWidth - 38)
            g.color = softMuted
            g.drawString(label, columnX + 22, y + 115)
        }
    }

    private fun paintAchievement(g: Graphics2D, profile: DeveloperProfile, archetype: Archetype?, x: Int, y: Int, palette: Palette) {
        val achievement = CardAchievements.forProfile(profile, archetype)
        g.color = withAlpha(palette.secondary, 210)
        g.fillRoundRect(x, y, 58, 5, 5, 5)
        g.font = Font("SansSerif", Font.BOLD, 15)
        g.color = softMuted
        g.drawString("YOUR CODING SUPERPOWER", x, y + 31)

        val textWidth = 800
        val title = fittingText(g, achievement.title, Font.BOLD, 34, 23, textWidth)
        g.font = title.font
        g.color = foreground
        g.drawString(title.text, x, y + 79)
        g.font = Font("SansSerif", Font.PLAIN, 21)
        g.color = muted
        drawWrapped(g, achievement.description, x, y + 113, textWidth, 28, 2)

        paintArchetypeGlyph(g, archetype, WIDTH - x - 64, y + 70, 94, palette, 220)
    }

    private fun paintFooter(g: Graphics2D, margin: Int, palette: Palette) {
        val lineY = HEIGHT - 138
        g.paint = LinearGradientPaint(
            margin.toFloat(), lineY.toFloat(), (WIDTH - margin).toFloat(), lineY.toFloat(),
            floatArrayOf(0f, 1f), arrayOf(withAlpha(palette.primary, 80), Color(255, 255, 255, 20)),
        )
        g.fillRoundRect(margin, lineY, WIDTH - margin * 2, 2, 2, 2)
        g.font = Font("SansSerif", Font.BOLD, 21)
        g.color = foreground
        g.drawString("devwrapped", margin, HEIGHT - 78)
        g.font = Font("SansSerif", Font.PLAIN, 17)
        g.color = softMuted
        g.drawString("PRIVATE BY DESIGN", margin + 152, HEIGHT - 78)
        g.font = Font("SansSerif", Font.PLAIN, 19)
        g.color = muted
        drawRight(g, "Discover Your Developer DNA", WIDTH - margin, HEIGHT - 78)
    }

    private fun paintDNAStamp(g: Graphics2D, x: Int, y: Int, archetype: Archetype?, palette: Palette) {
        g.color = Color(255, 255, 255, 12)
        g.fillOval(x, y, 156, 156)
        g.color = withAlpha(palette.primary, 80)
        g.stroke = BasicStroke(2f)
        g.drawOval(x + 1, y + 1, 154, 154)
        g.font = Font("SansSerif", Font.BOLD, 13)
        g.color = softMuted
        drawCentered(g, "DNA", x + 78, y + 31)
        paintArchetypeGlyph(g, archetype, x + 78, y + 94, 67, palette, 210)
    }

    private fun paintArchetypeGlyph(
        graphics: Graphics2D,
        archetype: Archetype?,
        centerX: Int,
        centerY: Int,
        size: Int,
        palette: Palette,
        alpha: Int,
    ) {
        val g = graphics.create() as Graphics2D
        try {
            val left = centerX - size / 2
            val top = centerY - size / 2
            val unit = size / 10.0
            g.stroke = BasicStroke((size / 24f).coerceAtLeast(2f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            when (archetype) {
                Archetype.MIDNIGHT_ARCHITECT -> {
                    val moon = Area(Ellipse2D.Double(left + unit, top + unit, unit * 7, unit * 7))
                    moon.subtract(Area(Ellipse2D.Double(left + unit * 3.2, top.toDouble(), unit * 6.2, unit * 6.2)))
                    g.color = withAlpha(palette.highlight, alpha)
                    g.fill(moon)
                    g.color = withAlpha(palette.tertiary, alpha)
                    g.fill(Ellipse2D.Double(left + unit * 7.7, top + unit * 2.1, unit, unit))
                    g.fill(Ellipse2D.Double(left + unit * 6.5, top + unit * 5.2, unit * .65, unit * .65))
                }
                Archetype.WEEKEND_WARRIOR -> {
                    g.color = withAlpha(palette.highlight, alpha)
                    g.draw(RoundRectangle2D.Double(left + unit, top + unit * 1.8, unit * 8, unit * 7, unit, unit))
                    g.drawLine((left + unit).toInt(), (top + unit * 4).toInt(), (left + unit * 9).toInt(), (top + unit * 4).toInt())
                    g.color = withAlpha(palette.primary, alpha)
                    g.fillRoundRect((left + unit * 2.2).toInt(), (top + unit * 5.2).toInt(), (unit * 2).toInt(), (unit * 2).toInt(), unit.toInt(), unit.toInt())
                    g.fillRoundRect((left + unit * 5.6).toInt(), (top + unit * 5.2).toInt(), (unit * 2).toInt(), (unit * 2).toInt(), unit.toInt(), unit.toInt())
                }
                Archetype.BUG_WHISPERER -> {
                    g.color = withAlpha(palette.tertiary, alpha)
                    g.drawOval((left + unit * 2.5).toInt(), (top + unit * 2.5).toInt(), (unit * 5).toInt(), (unit * 6).toInt())
                    g.drawLine(centerX, (top + unit * 2.6).toInt(), centerX, (top + unit * 8.2).toInt())
                    repeat(3) { index ->
                        val yy = (top + unit * (4 + index * 1.5)).toInt()
                        g.drawLine((left + unit * 2.5).toInt(), yy, (left + unit * .8).toInt(), yy - unit.toInt())
                        g.drawLine((left + unit * 7.5).toInt(), yy, (left + unit * 9.2).toInt(), yy - unit.toInt())
                    }
                }
                Archetype.COMMIT_MACHINE -> {
                    val points = listOf(left + unit * 1.5 to top + unit * 7.2, left + unit * 5 to top + unit * 3, left + unit * 8.5 to top + unit * 5.3)
                    g.color = withAlpha(palette.highlight, alpha)
                    points.zipWithNext().forEach { (a, b) -> g.drawLine(a.first.toInt(), a.second.toInt(), b.first.toInt(), b.second.toInt()) }
                    g.color = withAlpha(palette.primary, alpha)
                    points.forEach { point -> g.fill(Ellipse2D.Double(point.first - unit, point.second - unit, unit * 2, unit * 2)) }
                    g.color = withAlpha(palette.secondary, alpha / 2)
                    repeat(3) { i -> g.drawLine((left + unit * (1 + i)).toInt(), (top + unit * (1 + i * .7)).toInt(), (left + unit * (4 + i)).toInt(), (top + unit * (1 + i * .7)).toInt()) }
                }
                Archetype.POLYGLOT -> {
                    listOf(palette.primary, palette.secondary, palette.tertiary).forEachIndexed { index, color ->
                        g.color = withAlpha(color, alpha / 2)
                        g.fill(Ellipse2D.Double(left + unit * (1 + index * 2.2), top + unit * (2 + (index % 2) * 2.2), unit * 5, unit * 5))
                    }
                    g.color = withAlpha(palette.highlight, alpha)
                    g.drawLine((left + unit * 2).toInt(), (top + unit * 8).toInt(), (left + unit * 8).toInt(), (top + unit * 2).toInt())
                }
                Archetype.STEADY_BUILDER -> {
                    listOf(3.0, 5.2, 7.5).forEachIndexed { index, height ->
                        g.color = withAlpha(if (index == 1) palette.tertiary else palette.primary, alpha)
                        g.fillRoundRect(
                            (left + unit * (1 + index * 3)).toInt(),
                            (top + unit * (9 - height)).toInt(),
                            (unit * 2).toInt(),
                            (unit * height).toInt(),
                            unit.toInt(), unit.toInt(),
                        )
                    }
                }
                null -> {
                    g.color = withAlpha(palette.highlight, alpha)
                    repeat(3) { index -> g.fillOval((left + unit * (2 + index * 3)).toInt(), (top + unit * 4.3).toInt(), (unit * 1.4).toInt(), (unit * 1.4).toInt()) }
                }
            }
        } finally {
            g.dispose()
        }
    }

    private fun paintMotif(g: Graphics2D, archetype: Archetype?, palette: Palette) {
        g.stroke = BasicStroke(2f)
        when (archetype) {
            Archetype.MIDNIGHT_ARCHITECT, null -> {
                val stars = listOf(850 to 160, 970 to 245, 1080 to 135, 1120 to 330, 900 to 365, 1030 to 420)
                g.color = withAlpha(palette.highlight, 85)
                stars.zipWithNext().forEach { (a, b) -> g.drawLine(a.first, a.second, b.first, b.second) }
                stars.forEachIndexed { index, point ->
                    g.color = withAlpha(if (index % 2 == 0) palette.tertiary else palette.highlight, 150)
                    val radius = if (index % 3 == 0) 7 else 4
                    g.fill(Ellipse2D.Double((point.first - radius).toDouble(), (point.second - radius).toDouble(), (radius * 2).toDouble(), (radius * 2).toDouble()))
                }
            }
            Archetype.WEEKEND_WARRIOR -> {
                g.color = withAlpha(palette.primary, 55)
                repeat(4) { i -> g.drawArc(790 - i * 35, 45 - i * 35, 420 + i * 70, 420 + i * 70, 190, 145) }
            }
            Archetype.BUG_WHISPERER -> {
                g.color = withAlpha(palette.tertiary, 70)
                repeat(5) { i ->
                    val yy = 115 + i * 72
                    g.drawLine(820, yy, 1080 - i * 18, yy)
                    g.fillOval(810, yy - 5, 10, 10)
                    g.fillOval(1075 - i * 18, yy - 5, 10, 10)
                }
            }
            Archetype.COMMIT_MACHINE -> {
                g.color = withAlpha(palette.primary, 60)
                repeat(5) { i ->
                    val yy = 90 + i * 66
                    val path = Path2D.Double().apply { moveTo(830.0, yy.toDouble()); lineTo(950.0, (yy + 40).toDouble()); lineTo(1110.0, yy.toDouble()) }
                    g.draw(path)
                }
            }
            Archetype.POLYGLOT -> {
                listOf(palette.primary, palette.secondary, palette.tertiary).forEachIndexed { index, color ->
                    g.color = withAlpha(color, 72)
                    g.stroke = BasicStroke((8 - index * 2).toFloat(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
                    g.drawArc(790 + index * 38, 65 + index * 35, 380 - index * 30, 380 - index * 30, 205, 125)
                }
            }
            Archetype.STEADY_BUILDER -> {
                repeat(6) { i ->
                    g.color = withAlpha(if (i % 2 == 0) palette.primary else palette.tertiary, 54)
                    g.fillRoundRect(820, 105 + i * 58, 300 - i * 24, 10, 10, 10)
                }
            }
        }
    }

    private fun paintGrain(g: Graphics2D, palette: Palette) {
        var seed = 0x5EEDL
        repeat(95) {
            seed = (seed * 1103515245 + 12345) and 0x7fffffff
            val x = (seed % WIDTH).toInt()
            seed = (seed * 1103515245 + 12345) and 0x7fffffff
            val y = (seed % HEIGHT).toInt()
            g.color = withAlpha(if (it % 3 == 0) palette.tertiary else Color.WHITE, if (it % 4 == 0) 18 else 9)
            g.fillOval(x, y, if (it % 5 == 0) 3 else 2, if (it % 5 == 0) 3 else 2)
        }
    }

    private fun java.time.DayOfWeek.displayName(): String = name.lowercase().replaceFirstChar { it.uppercase() }

    private fun drawRight(g: Graphics2D, text: String, right: Int, baseline: Int) = g.drawString(text, right - g.fontMetrics.stringWidth(text), baseline)

    private fun drawCentered(g: Graphics2D, text: String, centerX: Int, baseline: Int) = g.drawString(text, centerX - g.fontMetrics.stringWidth(text) / 2, baseline)

    private fun fittingFont(g: Graphics2D, text: String, style: Int, maxSize: Int, maxWidth: Int): Font {
        var size = maxSize
        while (size > 12) {
            val candidate = Font("SansSerif", style, size)
            if (g.getFontMetrics(candidate).stringWidth(text) <= maxWidth) return candidate
            size -= 2
        }
        return Font("SansSerif", style, size)
    }

    private data class FittedText(val text: String, val font: Font)

    private fun fittingText(g: Graphics2D, text: String, style: Int, maxSize: Int, minSize: Int, maxWidth: Int): FittedText {
        var size = maxSize
        while (size >= minSize) {
            val font = Font("SansSerif", style, size)
            if (g.getFontMetrics(font).stringWidth(text) <= maxWidth) return FittedText(text, font)
            size -= 2
        }
        val font = Font("SansSerif", style, minSize)
        return FittedText(ellipsize(text, g.getFontMetrics(font), maxWidth), font)
    }

    internal fun ellipsize(text: String, metrics: FontMetrics, width: Int): String {
        if (metrics.stringWidth(text) <= width) return text
        var result = text
        while (result.isNotEmpty() && metrics.stringWidth("$result…") > width) result = result.dropLast(1)
        return "$result…"
    }

    private fun drawWrapped(g: Graphics2D, text: String, x: Int, firstBaseline: Int, width: Int, lineHeight: Int, maxLines: Int): Int {
        val lines = wrap(text, g.fontMetrics, width, maxLines)
        lines.forEachIndexed { index, line -> g.drawString(line, x, firstBaseline + index * lineHeight) }
        return firstBaseline + (lines.size - 1) * lineHeight
    }

    internal fun wrap(text: String, metrics: FontMetrics, width: Int, maxLines: Int): List<String> {
        val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return listOf("")
        val lines = mutableListOf<String>()
        var current = ""
        words.forEach { word ->
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (metrics.stringWidth(candidate) <= width || current.isEmpty()) current = candidate
            else { lines += current; current = word }
        }
        if (current.isNotEmpty()) lines += current
        if (lines.size <= maxLines) return lines
        val kept = lines.take(maxLines).toMutableList()
        var last = kept.last()
        while (metrics.stringWidth("$last…") > width && last.isNotEmpty()) last = last.dropLast(1)
        kept[maxLines - 1] = "$last…"
        return kept
    }

    private fun withAlpha(color: Color, alpha: Int) = Color(color.red, color.green, color.blue, alpha.coerceIn(0, 255))

    private data class Palette(
        val backgroundTop: Color,
        val backgroundMiddle: Color,
        val primary: Color,
        val secondary: Color,
        val tertiary: Color,
        val highlight: Color,
    ) {
        companion object {
            fun forArchetype(archetype: Archetype?): Palette = when (archetype) {
                Archetype.MIDNIGHT_ARCHITECT, null -> Palette(Color(20, 10, 52), Color(13, 8, 34), Color(141, 73, 255), Color(255, 62, 161), Color(43, 226, 215), Color(222, 205, 255))
                Archetype.WEEKEND_WARRIOR -> Palette(Color(53, 16, 39), Color(30, 10, 35), Color(255, 129, 52), Color(255, 62, 154), Color(255, 213, 79), Color(255, 229, 185))
                Archetype.BUG_WHISPERER -> Palette(Color(8, 42, 42), Color(7, 23, 31), Color(45, 220, 151), Color(64, 156, 255), Color(96, 245, 222), Color(205, 255, 239))
                Archetype.COMMIT_MACHINE -> Palette(Color(48, 20, 17), Color(25, 10, 24), Color(255, 183, 55), Color(255, 78, 105), Color(255, 223, 118), Color(255, 238, 198))
                Archetype.POLYGLOT -> Palette(Color(13, 28, 58), Color(15, 9, 39), Color(50, 214, 255), Color(255, 76, 183), Color(157, 99, 255), Color(210, 246, 255))
                Archetype.STEADY_BUILDER -> Palette(Color(8, 37, 52), Color(8, 18, 35), Color(42, 201, 181), Color(87, 125, 255), Color(119, 232, 211), Color(206, 255, 247))
            }
        }
    }
}
