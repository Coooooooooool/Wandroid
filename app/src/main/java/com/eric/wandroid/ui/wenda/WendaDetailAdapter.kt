package com.eric.wandroid.ui.wenda

import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.text.buildSpannedString
import androidx.core.text.bold
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.eric.wandroid.R
import com.eric.wandroid.common.ui.WendaContentRenderer
import com.eric.wandroid.domain.model.Article
import com.eric.wandroid.domain.model.WendaComment

class WendaDetailAdapter(
    private val onOpenOriginalClick: (Article) -> Unit,
    private val onCollectClick: (Article) -> Unit,
    private val onRetryClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<WendaDetailListItem>()

    fun submitState(state: WendaDetailUiState) {
        items.clear()
        items += WendaDetailListItem.Summary(
            article = state.article,
            isCollecting = state.isCollecting
        )
        items += WendaDetailListItem.Header(state.comments.size)

        when {
            state.isInitialLoading && state.comments.isEmpty() -> {
                items += WendaDetailListItem.LoadingRow
            }

            state.comments.isNotEmpty() -> {
                items += state.comments.map { WendaDetailListItem.CommentRow(it) }
            }

            state.commentsErrorMessage != null -> {
                items += WendaDetailListItem.StateRow(
                    message = state.commentsErrorMessage,
                    actionLabel = null
                )
            }

            else -> {
                items += WendaDetailListItem.StateRow(
                    message = ""
                )
            }
        }

        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is WendaDetailListItem.Summary -> VIEW_TYPE_SUMMARY
        is WendaDetailListItem.Header -> VIEW_TYPE_HEADER
        is WendaDetailListItem.CommentRow -> VIEW_TYPE_COMMENT
        is WendaDetailListItem.StateRow -> VIEW_TYPE_STATE
        WendaDetailListItem.LoadingRow -> VIEW_TYPE_LOADING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SUMMARY -> SummaryViewHolder(
                inflater.inflate(R.layout.item_wenda_article_summary, parent, false),
                onOpenOriginalClick,
                onCollectClick
            )

            VIEW_TYPE_HEADER -> HeaderViewHolder(
                inflater.inflate(R.layout.item_system_article_header, parent, false)
            )

            VIEW_TYPE_COMMENT -> CommentViewHolder(
                inflater.inflate(R.layout.item_wenda_comment, parent, false)
            )

            VIEW_TYPE_STATE -> StateViewHolder(
                inflater.inflate(R.layout.item_wenda_comment_state, parent, false),
                onRetryClick
            )

            VIEW_TYPE_LOADING -> LoadingViewHolder(
                inflater.inflate(R.layout.item_wenda_comment_loading, parent, false)
            )

            else -> error("Unsupported view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is WendaDetailListItem.Summary -> (holder as SummaryViewHolder).bind(item.article, item.isCollecting)
            is WendaDetailListItem.Header -> (holder as HeaderViewHolder).bind(item.count)
            is WendaDetailListItem.CommentRow -> (holder as CommentViewHolder).bind(item.comment)
            is WendaDetailListItem.StateRow -> (holder as StateViewHolder).bind(item)
            WendaDetailListItem.LoadingRow -> Unit
        }
    }

    private class SummaryViewHolder(
        itemView: View,
        private val onOpenOriginalClick: (Article) -> Unit,
        private val onCollectClick: (Article) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.articleTitle)
        private val descView: TextView = itemView.findViewById(R.id.articleDescription)
        private val metaView: TextView = itemView.findViewById(R.id.articleMeta)
        private val chapterView: TextView = itemView.findViewById(R.id.articleChapter)
        private val openOriginalButton: Button = itemView.findViewById(R.id.openOriginalButton)
        private val collectButton: Button = itemView.findViewById(R.id.collectButton)

        init {
            descView.movementMethod = LinkMovementMethod.getInstance()
        }

        fun bind(article: Article, isCollecting: Boolean) {
            titleView.text = renderHtml(article.title)
            if (article.desc.isNotBlank()) {
                WendaContentRenderer.render(descView, article.desc)
            } else {
                descView.text = article.link
            }
            metaView.text = itemView.context.getString(
                R.string.article_meta,
                article.author,
                article.niceDate.ifBlank { itemView.context.getString(R.string.unknown_time) }
            )
            chapterView.text = listOf(article.superChapterName, article.chapterName)
                .filter { it.isNotBlank() }
                .joinToString(" / ")
                .ifBlank { itemView.context.getString(R.string.uncategorized) }
            openOriginalButton.isEnabled = article.link.isNotBlank()
            openOriginalButton.setOnClickListener { onOpenOriginalClick(article) }
            collectButton.isEnabled = !isCollecting
            collectButton.text = when {
                isCollecting -> itemView.context.getString(R.string.collect_action_loading)
                article.isCollected -> itemView.context.getString(R.string.collect_action_remove)
                else -> itemView.context.getString(R.string.collect_action_add)
            }
            collectButton.setOnClickListener { onCollectClick(article) }
        }
    }

    private class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.systemArticleTitle)
        private val subtitleView: TextView = itemView.findViewById(R.id.systemArticleSubtitle)

        fun bind(count: Int) {
            titleView.setText(R.string.wenda_comment_list_title)
            subtitleView.text = itemView.context.getString(R.string.article_section_count, count)
        }
    }

    private class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarView: TextView = itemView.findViewById(R.id.commentAvatar)
        private val titleView: TextView = itemView.findViewById(R.id.commentUser)
        private val badgeView: TextView = itemView.findViewById(R.id.commentBadge)
        private val metaView: TextView = itemView.findViewById(R.id.commentMeta)
        private val contentView: TextView = itemView.findViewById(R.id.commentContent)
        private val replyContainer: LinearLayout = itemView.findViewById(R.id.replyContainer)

        init {
            contentView.movementMethod = LinkMovementMethod.getInstance()
        }

        fun bind(comment: WendaComment) {
            val displayName = displayUserName(comment, itemView)
            avatarView.text = displayName.firstOrNull()?.uppercaseChar()?.toString()
                ?: itemView.context.getString(R.string.wenda_comment_avatar_fallback)
            titleView.text = displayName
            badgeView.isVisible = comment.isAnonymous
            badgeView.text = itemView.context.getString(R.string.wenda_comment_anonymous_tag)
            metaView.text = buildMeta(comment, itemView)
            WendaContentRenderer.render(
                contentView,
                comment.content,
                comment.contentMd
            )
            replyContainer.removeAllViews()
            replyContainer.isVisible = comment.replies.isNotEmpty()
            if (comment.replies.isEmpty()) return

            val inflater = LayoutInflater.from(itemView.context)
            comment.replies.forEach { reply ->
                val replyView = inflater.inflate(R.layout.item_wenda_reply, replyContainer, false)
                val replyMeta: TextView = replyView.findViewById(R.id.replyMeta)
                val replyContent: TextView = replyView.findViewById(R.id.replyContent)
                replyContent.movementMethod = LinkMovementMethod.getInstance()
                replyMeta.text = buildReplyMeta(reply, itemView)
                WendaContentRenderer.render(
                    replyContent,
                    reply.content,
                    reply.contentMd
                )
                replyContainer.addView(replyView)
            }
        }
    }

    private class StateViewHolder(
        itemView: View,
        private val onRetryClick: () -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val messageView: TextView = itemView.findViewById(R.id.stateMessage)
        private val actionButton: Button = itemView.findViewById(R.id.stateAction)

        fun bind(item: WendaDetailListItem.StateRow) {
            val fallbackMessage = if (item.actionLabel == null) {
                itemView.context.getString(R.string.wenda_comment_empty)
            } else {
                item.message
            }
            messageView.text = fallbackMessage
            actionButton.isVisible = item.actionLabel != null
            actionButton.text = item.actionLabel ?: itemView.context.getString(R.string.label_retry)
            actionButton.setOnClickListener { onRetryClick() }
        }
    }

    private class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val progressBar: ProgressBar = itemView.findViewById(R.id.loadingProgress)
        private val loadingText: TextView = itemView.findViewById(R.id.loadingText)

        init {
            progressBar.isIndeterminate = true
            loadingText.setText(R.string.wenda_comment_loading)
        }
    }

    companion object {
        private const val VIEW_TYPE_SUMMARY = 1
        private const val VIEW_TYPE_HEADER = 2
        private const val VIEW_TYPE_COMMENT = 3
        private const val VIEW_TYPE_STATE = 4
        private const val VIEW_TYPE_LOADING = 5

        private fun renderHtml(raw: String): Spanned {
            return HtmlCompat.fromHtml(raw, HtmlCompat.FROM_HTML_MODE_LEGACY)
        }

        private fun displayUserName(comment: WendaComment, itemView: View): String {
            return if (comment.isAnonymous) {
                itemView.context.getString(R.string.wenda_comment_anonymous_user)
            } else {
                comment.userName.ifBlank {
                    itemView.context.getString(R.string.wenda_comment_anonymous_user)
                }
            }
        }

        private fun buildMeta(comment: WendaComment, itemView: View): CharSequence {
            return buildSpannedString {
                append(comment.niceDate.ifBlank { itemView.context.getString(R.string.unknown_time) })
                if (comment.likeCount > 0) {
                    append(" · ")
                    bold {
                        append(itemView.context.getString(R.string.wenda_comment_like_count, comment.likeCount))
                    }
                }
            }
        }

        private fun buildReplyMeta(reply: WendaComment, itemView: View): String {
            val fromUser = displayUserName(reply, itemView)
            val targetUser = reply.toUserName.ifBlank { itemView.context.getString(R.string.wenda_comment_default_target) }
            return itemView.context.getString(
                R.string.wenda_comment_reply_meta,
                fromUser,
                targetUser,
                reply.niceDate.ifBlank { itemView.context.getString(R.string.unknown_time) }
            )
        }
    }
}
