package com.eric.wandroid.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.eric.wandroid.R
import com.eric.wandroid.common.auth.createHomeIntent
import com.eric.wandroid.common.ui.EdgeToEdgeHelper
import com.eric.wandroid.ui.shell.MainTab
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {
    private val viewModel: AuthViewModel by viewModels { AuthViewModelFactory() }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var usernameInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var repeatPasswordInputLayout: TextInputLayout
    private lateinit var usernameEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var repeatPasswordEditText: TextInputEditText
    private lateinit var submitButton: Button
    private lateinit var switchModeButton: Button
    private lateinit var formScrollView: ScrollView
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    private lateinit var errorText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        bindViews()
        EdgeToEdgeHelper.apply(this, toolbar, formScrollView)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        submitButton.setOnClickListener {
            viewModel.submit(
                username = usernameEditText.text.toString(),
                password = passwordEditText.text.toString(),
                repeatPassword = repeatPasswordEditText.text.toString()
            )
        }
        switchModeButton.setOnClickListener { viewModel.switchMode() }

        observeUi()
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar)
        formScrollView = findViewById(R.id.authScrollView)
        usernameInputLayout = findViewById(R.id.usernameInputLayout)
        passwordInputLayout = findViewById(R.id.passwordInputLayout)
        repeatPasswordInputLayout = findViewById(R.id.repeatPasswordInputLayout)
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        repeatPasswordEditText = findViewById(R.id.repeatPasswordEditText)
        submitButton = findViewById(R.id.submitAuthButton)
        switchModeButton = findViewById(R.id.switchAuthModeButton)
        progressBar = findViewById(R.id.authProgress)
        statusText = findViewById(R.id.authStatus)
        errorText = findViewById(R.id.authError)
    }

    private fun observeUi() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch {
                    viewModel.messages.collect { message ->
                        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
                    }
                }
                launch {
                    viewModel.authSuccess.collect {
                        startActivity(createHomeIntent(initialTab = MainTab.My))
                        finish()
                    }
                }
            }
        }
    }

    private fun render(state: AuthUiState) {
        toolbar.subtitle = if (state.isLoggedIn) {
            getString(R.string.auth_logged_in_subtitle)
        } else {
            getString(R.string.auth_guest_subtitle)
        }

        progressBar.isVisible = state.isLoading
        val showForm = !state.isLoggedIn
        usernameEditText.isEnabled = !state.isLoading && showForm
        passwordEditText.isEnabled = !state.isLoading && showForm
        repeatPasswordEditText.isEnabled = !state.isLoading && showForm
        submitButton.isEnabled = !state.isLoading && showForm
        switchModeButton.isEnabled = !state.isLoading && showForm

        usernameInputLayout.isVisible = showForm
        passwordInputLayout.isVisible = showForm
        submitButton.isVisible = showForm
        switchModeButton.isVisible = showForm
        repeatPasswordInputLayout.isVisible = state.mode == AuthMode.Register && showForm
        submitButton.text = if (state.mode == AuthMode.Login) {
            getString(R.string.auth_login)
        } else {
            getString(R.string.auth_register)
        }
        switchModeButton.text = if (state.mode == AuthMode.Login) {
            getString(R.string.auth_go_register)
        } else {
            getString(R.string.auth_go_login)
        }

        statusText.text = if (state.isLoggedIn) {
            getString(R.string.auth_logged_in_as, state.displayName.ifBlank { state.username })
        } else {
            getString(R.string.auth_not_logged_in)
        }

        errorText.isVisible = state.errorMessage != null
        errorText.text = state.errorMessage.orEmpty()
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, AuthActivity::class.java)
        }
    }
}
