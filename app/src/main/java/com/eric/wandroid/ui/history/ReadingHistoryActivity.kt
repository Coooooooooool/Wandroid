package com.eric.wandroid.ui.history

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eric.wandroid.R
import com.eric.wandroid.common.ui.EdgeToEdgeHelper
import com.eric.wandroid.common.ui.ScrollToTopHelper
import com.eric.wandroid.data.history.HistoryRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class ReadingHistoryActivity : AppCompatActivity() {
    private lateinit var toolbar: MaterialToolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: View
    private lateinit var emptyTitle: TextView
    private lateinit var emptyMessage: TextView
    private lateinit var scrollToTopButton: FloatingActionButton

    private val repository by lazy(LazyThreadSafetyMode.NONE) {
        HistoryRepository.getInstance(applicationContext)
    }

    private val adapter = ReadingHistoryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reading_history)
        bindViews()
        EdgeToEdgeHelper.applySurfaceToolbar(this, toolbar, recyclerView)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        ScrollToTopHelper.attach(recyclerView, scrollToTopButton) { recyclerView.visibility != View.VISIBLE }

        render()
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.readingHistoryRecyclerView)
        emptyState = findViewById(R.id.emptyState)
        emptyTitle = findViewById(R.id.emptyStateTitle)
        emptyMessage = findViewById(R.id.emptyStateMessage)
        scrollToTopButton = findViewById(R.id.scrollToTopButton)
    }

    private fun render() {
        val history = repository.getReadingHistory()
        adapter.submitList(history)
        recyclerView.visibility = if (history.isNotEmpty()) View.VISIBLE else View.GONE
        emptyState.visibility = if (history.isEmpty()) View.VISIBLE else View.GONE
        emptyTitle.setText(R.string.empty_title)
        emptyMessage.setText(R.string.reading_history_empty_message)
        ScrollToTopHelper.update(recyclerView, scrollToTopButton, history.isEmpty())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_reading_history, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_clear_history) {
            repository.clearReadingHistory()
            render()
            Snackbar.make(findViewById(android.R.id.content), R.string.reading_history_cleared, Snackbar.LENGTH_SHORT).show()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, ReadingHistoryActivity::class.java)
        }
    }
}
