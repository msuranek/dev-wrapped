package com.devwrapped.render

import com.devwrapped.core.DeveloperProfile
import com.devwrapped.core.PersonalityResult
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.GradientPaint
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import java.text.NumberFormat
import java.util.Locale
import javax.imageio.ImageIO
import java.nio.file.Path
import kotlin.math.min

object CardRenderer {
    const val WIDTH = 1200
    const val HEIGHT = 1500

    private val background = Color(14, 8, 30)
    private val foreground = Color(250, 248, 255)
    private val muted = Color(190, 180, 211)
    private val purple = Color(142, 72, 255)
    private val pink = Color(255, 66, 164)
    private val cyan = Color(46, 226, 214)

    fun render(profile: DeveloperProfile, personality: PersonalityResult): BufferedImage {
        val image = BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            paintBackground(g)
            paintContent(g, profile, personality)
        } finally { g.dispose() }
        return image
    }

    fun writePng(image: BufferedImage, path: Path) {
        require(ImageIO.write(image, "png", path.toFile())) { "No PNG image writer is available." }
    }

    private fun paintBackground(g: Graphics2D) {
        g.paint = GradientPaint(0f, 0f, Color(30, 13, 58), WIDTH.toFloat(), HEIGHT.toFloat(), background)
        g.fillRect(0, 0, WIDTH, HEIGHT)
        g.color = Color(purple.red, purple.green, purple.blue, 42)
        g.fill(Ellipse2D.Double(720.0, -230.0, 720.0, 720.0))
        g.color = Color(pink.red, pink.green, pink.blue, 28)
        g.fill(Ellipse2D.Double(-260.0, 980.0, 680.0, 680.0))
        val wave = Path2D.Double().apply {
            moveTo(0.0, 450.0); curveTo(330.0, 330.0, 700.0, 620.0, 1200.0, 420.0)
            lineTo(1200.0, 470.0); curveTo(700.0, 670.0, 330.0, 380.0, 0.0, 500.0); closePath()
        }
        g.color = Color(cyan.red, cyan.green, cyan.blue, 20)
        g.fill(wave)
    }

    private fun paintContent(g: Graphics2D, profile: DeveloperProfile, personality: PersonalityResult) {
        val left = 92
        g.font = Font("SansSerif", Font.BOLD, 30)
        g.color = cyan
        g.drawString("DEV WRAPPED", left, 102)
        g.font = Font("SansSerif", Font.PLAIN, 22)
        g.color = muted
        drawRight(g, profile.periodLabel.uppercase(), WIDTH - left, 102)

        g.font = Font("SansSerif", Font.BOLD, 25)
        g.color = pink
        g.drawString("YOUR DEVELOPER DNA", left, 235)

        val title = personality.archetype?.title?.uppercase() ?: "NOT ENOUGH SIGNAL"
        var y = 330
        g.color = foreground
        g.font = fittingFont(g, title, Font.BOLD, 92, WIDTH - left * 2)
        y = drawWrapped(g, title, left, y, WIDTH - left * 2, (g.font.size * 1.02).toInt(), 2) + 32
        g.font = Font("SansSerif", Font.ITALIC, 31)
        g.color = muted
        val subtitle = personality.archetype?.subtitle ?: personality.explanation
        y = drawWrapped(g, "“$subtitle”", left, y, WIDTH - left * 2, 42, 2) + 58

        g.color = Color(255, 255, 255, 24)
        g.fillRoundRect(left, y, WIDTH - left * 2, 2, 2, 2)
        y += 70

        val number = NumberFormat.getIntegerInstance(Locale.US).format(profile.totalCommits)
        drawMetric(g, left, y, number, "COMMITS", purple)
        drawMetric(g, 620, y, "${profile.lateNightPercentage}%", "LATE-NIGHT COMMITS", pink)
        y += 210
        drawMetric(g, left, y, profile.mostActiveDay?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "—", "MOST ACTIVE DAY", cyan)
        drawMetric(g, 620, y, profile.primaryLanguage ?: "—", "TOP LANGUAGE", purple)
        y += 210
        drawSmallMetric(g, left, y, profile.activeDays.toString(), "active days")
        drawSmallMetric(g, 390, y, profile.longestStreakDays.toString(), "day streak")
        drawSmallMetric(g, 690, y, profile.fixCommits.toString(), "fix commits")
        drawSmallMetric(g, 960, y, profile.mostActiveHour?.let { String.format("%02d:00", it) } ?: "—", "peak hour")

        g.color = Color(255, 255, 255, 28)
        g.fillRoundRect(left, HEIGHT - 166, WIDTH - left * 2, 2, 2, 2)
        g.font = Font("SansSerif", Font.BOLD, 24)
        g.color = foreground
        g.drawString("devwrapped", left, HEIGHT - 94)
        g.font = Font("SansSerif", Font.PLAIN, 22)
        g.color = muted
        drawRight(g, "Discover Your Developer DNA", WIDTH - left, HEIGHT - 94)
    }

    private fun drawMetric(g: Graphics2D, x: Int, y: Int, value: String, label: String, accent: Color) {
        g.color = accent
        g.fillRoundRect(x, y - 58, 12, 115, 12, 12)
        g.font = fittingFont(g, value, Font.BOLD, 62, 470)
        g.color = foreground
        g.drawString(value, x + 34, y)
        g.font = Font("SansSerif", Font.BOLD, 18)
        g.color = muted
        g.drawString(label, x + 36, y + 42)
    }

    private fun drawSmallMetric(g: Graphics2D, x: Int, y: Int, value: String, label: String) {
        g.font = fittingFont(g, value, Font.BOLD, 38, 190)
        g.color = foreground
        g.drawString(value, x, y)
        g.font = Font("SansSerif", Font.PLAIN, 18)
        g.color = muted
        g.drawString(label, x, y + 34)
    }

    private fun drawRight(g: Graphics2D, text: String, right: Int, baseline: Int) = g.drawString(text, right - g.fontMetrics.stringWidth(text), baseline)

    private fun fittingFont(g: Graphics2D, text: String, style: Int, maxSize: Int, maxWidth: Int): Font {
        var size = maxSize
        while (size > 18) {
            val candidate = Font("SansSerif", style, size)
            if (g.getFontMetrics(candidate).stringWidth(text) <= maxWidth) return candidate
            size -= 2
        }
        return Font("SansSerif", style, size)
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
}
