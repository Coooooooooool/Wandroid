package com.eric.wandroid.common.ui

import android.content.Context
import android.graphics.Typeface
import android.text.TextUtils
import android.widget.TextView
import androidx.core.content.ContextCompat
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.ImagesPlugin
import java.util.WeakHashMap
import com.eric.wandroid.R

object WendaContentRenderer {
    private val markwonCache = WeakHashMap<Context, Markwon>()

    fun render(textView: TextView, html: String, markdown: String = "") {
        val source = contentSource(html = html, markdown = markdown)
        if (source.isBlank()) {
            textView.text = ""
            return
        }
        markwon(textView.context).setMarkdown(textView, source)
    }

    private fun contentSource(html: String, markdown: String): String {
        return when {
            markdown.isNotBlank() -> markdown
            html.isNotBlank() -> normalizeHtml(html)
            else -> ""
        }
    }

    private fun normalizeHtml(raw: String): String {
        return raw.replace("\r\n", "\n").trim()
    }

    private fun markwon(context: Context): Markwon {
        val appContext = context.applicationContext
        return markwonCache.getOrPut(appContext) {
            Markwon.builder(appContext)
                .usePlugin(ImagesPlugin.create())
                .usePlugin(HtmlPlugin.create())
                .usePlugin(object : AbstractMarkwonPlugin() {
                    override fun configureTheme(builder: MarkwonTheme.Builder) {
                        builder
                            .linkColor(ContextCompat.getColor(appContext, R.color.brand_secondary))
                            .codeTextColor(ContextCompat.getColor(appContext, R.color.code_inline_text))
                            .codeBackgroundColor(ContextCompat.getColor(appContext, R.color.code_inline_background))
                            .codeBlockTextColor(ContextCompat.getColor(appContext, R.color.code_block_text))
                            .codeBlockBackgroundColor(ContextCompat.getColor(appContext, R.color.code_block_background))
                            .codeTypeface(Typeface.MONOSPACE)
                            .codeBlockTypeface(Typeface.MONOSPACE)
                            .codeBlockMargin(dp(appContext, 12))
                            .blockMargin(dp(appContext, 20))
                    }
                })
                .build()
        }
    }

    private fun dp(context: Context, value: Int): Int {
        return (value * context.resources.displayMetrics.density).toInt()
    }
}
