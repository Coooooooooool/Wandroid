package com.eric.wandroid.ui.moyu.news

import android.text.Spanned
import androidx.core.text.HtmlCompat

sealed interface NewsContentBlock {
    data class Paragraph(val text: Spanned) : NewsContentBlock
    data class Image(val url: String) : NewsContentBlock
}

object NewsContentParser {
    private val imageRegex = Regex(
        "<img[^>]*src=[\"']([^\"']+)[\"'][^>]*>",
        RegexOption.IGNORE_CASE
    )

    fun parse(html: String): List<NewsContentBlock> {
        val source = html.trim()
        if (source.isBlank()) return emptyList()

        val blocks = mutableListOf<NewsContentBlock>()
        var lastIndex = 0
        imageRegex.findAll(source).forEach { match ->
            val before = source.substring(lastIndex, match.range.first)
            parseTextBlock(before)?.let(blocks::add)
            val url = match.groupValues.getOrNull(1).orEmpty().trim()
            if (url.isNotBlank()) {
                blocks += NewsContentBlock.Image(url)
            }
            lastIndex = match.range.last + 1
        }
        parseTextBlock(source.substring(lastIndex))?.let(blocks::add)
        return blocks
    }

    private fun parseTextBlock(raw: String): NewsContentBlock.Paragraph? {
        val cleaned = raw
            .replace("<br\\s*/?>".toRegex(RegexOption.IGNORE_CASE), "<br/>")
            .replace("</p>", "</p>\n", ignoreCase = true)
            .trim()
        if (cleaned.isBlank()) return null
        val spanned = HtmlCompat.fromHtml(cleaned, HtmlCompat.FROM_HTML_MODE_LEGACY)
        return if (spanned.isBlank()) null else NewsContentBlock.Paragraph(spanned)
    }
}
