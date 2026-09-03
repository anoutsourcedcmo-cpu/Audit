package com.example.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

enum class AuditTab {
    DASHBOARD,
    PROFILE,
    CONSULTANT
}

class AuditViewModel(private val repository: AuditRepository) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private var bindingManager: DeviceBindingManager? = null

    private val _deviceBindingInfo = MutableStateFlow(DeviceBindingInfo(isBound = false))
    val deviceBindingInfo: StateFlow<DeviceBindingInfo> = _deviceBindingInfo.asStateFlow()

    private val _currentDeviceModel = MutableStateFlow("")
    val currentDeviceModel: StateFlow<String> = _currentDeviceModel.asStateFlow()

    private val _currentDeviceFingerprintDisplay = MutableStateFlow("")
    val currentDeviceFingerprintDisplay: StateFlow<String> = _currentDeviceFingerprintDisplay.asStateFlow()

    private val _loginResultState = MutableStateFlow<LoginResult?>(null)
    val loginResultState: StateFlow<LoginResult?> = _loginResultState.asStateFlow()

    private val _currentTab = MutableStateFlow(AuditTab.DASHBOARD)
    val currentTab: StateFlow<AuditTab> = _currentTab.asStateFlow()

    private val _profile = MutableStateFlow(ProfileEntity())
    val profile: StateFlow<ProfileEntity> = _profile.asStateFlow()

    private val _answers = MutableStateFlow<Map<String, String>>(emptyMap())
    val answers: StateFlow<Map<String, String>> = _answers.asStateFlow()

    private val _showValidationErrors = MutableStateFlow(false)
    val showValidationErrors: StateFlow<Boolean> = _showValidationErrors.asStateFlow()

    private val _exportEvents = MutableSharedFlow<ExportResult>()
    val exportEvents: SharedFlow<ExportResult> = _exportEvents.asSharedFlow()

    sealed interface ExportResult {
        data class Success(val filePath: String, val fileUri: Uri?) : ExportResult
        data class Failure(val message: String) : ExportResult
    }

    init {
        // Collect database flows
        viewModelScope.launch {
            repository.profileFlow.collectLatest { profileEntity ->
                if (profileEntity != null) {
                    _profile.value = profileEntity
                }
            }
        }

        viewModelScope.launch {
            repository.allAnswersFlow.collectLatest { list ->
                val map = list.associate { it.questionId to it.answerText }
                _answers.value = map
            }
        }
    }

    fun initDeviceBinding(context: Context) {
        val manager = bindingManager ?: DeviceBindingManager(context).also { bindingManager = it }
        val username = "MarketingPro"
        _deviceBindingInfo.value = manager.getBindingInfo(username)

        val currentFp = com.example.util.DeviceFingerprintUtils.getHardwareFingerprint(context)
        _currentDeviceModel.value = com.example.util.DeviceFingerprintUtils.getHardwareModelName()
        _currentDeviceFingerprintDisplay.value = com.example.util.DeviceFingerprintUtils.getFormattedFingerprint(currentFp)
    }

    fun login(usernameInput: String, passwordInput: String): Boolean {
        if (usernameInput.trim() == "MarketingPro" && passwordInput == "AuditPlatform@2026") {
            _isLoggedIn.value = true
            return true
        }
        return false
    }

    fun loginWithDeviceBinding(context: Context, usernameInput: String, passwordInput: String): LoginResult {
        if (usernameInput.trim() != "MarketingPro" || passwordInput != "AuditPlatform@2026") {
            val result = LoginResult.InvalidCredentials
            _loginResultState.value = result
            return result
        }

        val manager = bindingManager ?: DeviceBindingManager(context).also { bindingManager = it }
        val result = manager.verifyAndBindLogin(usernameInput.trim())

        _deviceBindingInfo.value = manager.getBindingInfo(usernameInput.trim())
        _loginResultState.value = result

        if (result is LoginResult.Success) {
            _isLoggedIn.value = true
        }

        return result
    }

    fun resetDeviceBinding(context: Context, username: String = "MarketingPro") {
        val manager = bindingManager ?: DeviceBindingManager(context).also { bindingManager = it }
        manager.resetBinding(username)
        _deviceBindingInfo.value = manager.getBindingInfo(username)
        _loginResultState.value = null
    }

    fun simulateDeviceMismatch(context: Context, username: String = "MarketingPro") {
        val manager = bindingManager ?: DeviceBindingManager(context).also { bindingManager = it }
        manager.simulateMismatchBinding(username)
        _deviceBindingInfo.value = manager.getBindingInfo(username)
        _loginResultState.value = null
    }

    fun clearLoginResultState() {
        _loginResultState.value = null
    }

    fun signOut() {
        _isLoggedIn.value = false
    }

    fun setTab(tab: AuditTab) {
        _currentTab.value = tab
    }

    fun updateProfile(updater: (ProfileEntity) -> ProfileEntity) {
        val updated = updater(_profile.value)
        _profile.value = updated
        viewModelScope.launch {
            repository.saveProfile(updated)
        }
    }

    fun updateAnswer(questionId: String, text: String) {
        val updatedMap = _answers.value.toMutableMap()
        updatedMap[questionId] = text
        _answers.value = updatedMap
        viewModelScope.launch {
            repository.saveAnswer(questionId, text)
        }
    }

    fun isPhoneValid(phone: String): Boolean {
        if (phone.isEmpty()) return false
        // Requires starts with '+' then digits, dashes, or spaces
        val regex = Regex("^\\+[1-9][0-9\\s\\-]{5,18}$")
        return phone.matches(regex)
    }

    // Validate overall Profile completeness and correctness
    fun validateProfile(): Boolean {
        val p = _profile.value
        return p.organizationName.isNotBlank() &&
                p.targetIndustry.isNotBlank() &&
                p.representativeName.isNotBlank() &&
                p.representativeDesignation.isNotBlank() &&
                p.representativeLocation.isNotBlank() &&
                p.representativePhone.isNotBlank() &&
                isPhoneValid(p.representativePhone)
    }

    fun performExport(context: Context) {
        _showValidationErrors.value = true

        val isValid = validateProfile()
        if (!isValid) {
            // Automatically switch to Profile tab
            _currentTab.value = AuditTab.PROFILE
            Toast.makeText(context, "Please fill in all compulsory profile fields correctly first!", Toast.LENGTH_LONG).show()
            return
        }

        // Export content
        exportToWordFile(context)
    }

    private fun exportToWordFile(context: Context) {
        viewModelScope.launch {
            try {
                val p = _profile.value
                val ans = _answers.value

                // HTML format which MS Word opens beautifully
                val docContent = buildString {
                    append("<!DOCTYPE html>\n<html>\n<head>\n")
                    append("<meta charset=\"utf-8\">\n")
                    append("<style>\n")
                    append("  body { font-family: 'Segoe UI', Calibri, Arial, sans-serif; line-height: 1.6; color: #333333; margin: 40px; }\n")
                    append("  h1 { color: #1e3a8a; font-size: 26pt; border-bottom: 2px solid #1e3a8a; padding-bottom: 10px; margin-bottom: 5px; }\n")
                    append("  .subtitle { font-size: 16pt; color: #475569; margin-top: 0; margin-bottom: 30px; font-weight: normal; }\n")
                    append("  .consultant-tag { font-size: 12pt; color: #0f766e; font-weight: bold; font-style: italic; margin-bottom: 40px; }\n")
                    append("  h2 { color: #0f766e; font-size: 18pt; margin-top: 30px; border-bottom: 1px solid #e2e8f0; padding-bottom: 5px; }\n")
                    append("  h3 { color: #1e293b; font-size: 13pt; margin-top: 20px; margin-bottom: 10px; }\n")
                    append("  .meta-table { width: 100%; border-collapse: collapse; margin-bottom: 40px; }\n")
                    append("  .meta-table th { background-color: #f1f5f9; text-align: left; padding: 10px; border: 1px solid #cbd5e1; font-weight: bold; width: 30%; }\n")
                    append("  .meta-table td { padding: 10px; border: 1px solid #cbd5e1; width: 70%; }\n")
                    append("  .question-container { background-color: #fafafa; border-left: 4px solid #0f766e; padding: 15px; margin-bottom: 20px; }\n")
                    append("  .question-text { font-weight: bold; color: #1e293b; margin-bottom: 8px; }\n")
                    append("  .answer-text { color: #334155; white-space: pre-wrap; font-style: normal; }\n")
                    append("  .empty-answer { color: #94a3b8; font-style: italic; }\n")
                    append("  .footer { margin-top: 50px; text-align: center; font-size: 10pt; color: #64748b; border-top: 1px solid #cbd5e1; padding-top: 10px; }\n")
                    append("</style>\n</head>\n<body>\n")

                    append("<h1>A Marketing Audit Platform</h1>\n")
                    append("<div class=\"subtitle\">Comprehensive, Systematic, Independent and Periodic</div>\n")
                    append("<div class=\"consultant-tag\">Anuj Khanna, Marketing Strategy Consultant</div>\n")

                    append("<h2>Organization & Client Profile</h2>\n")
                    append("<table class=\"meta-table\">\n")
                    append("  <tr><th>Client Organization Name</th><td>${p.organizationName}</td></tr>\n")
                    append("  <tr><th>Target Industry / Domain</th><td>${p.targetIndustry}</td></tr>\n")
                    append("  <tr><th>Client Representative Name</th><td>${p.representativeName}</td></tr>\n")
                    append("  <tr><th>Representative Designation</th><td>${p.representativeDesignation}</td></tr>\n")
                    append("  <tr><th>Representative Location</th><td>${p.representativeLocation}</td></tr>\n")
                    append("  <tr><th>Representative Phone Number</th><td>${p.representativePhone}</td></tr>\n")
                    append("</table>\n")

                    append("<h2>Marketing Environment</h2>\n")

                    // Macro sections
                    val macroSections = auditSections.filter { it.isMacro }
                    macroSections.forEach { section ->
                        append("<h3>${section.title}</h3>\n")
                        section.questions.forEach { q ->
                            val answer = ans[q.id]?.trim() ?: ""
                            append("<div class=\"question-container\">\n")
                            append("  <div class=\"question-text\">${q.questionNumber}. ${q.label}</div>\n")
                            if (answer.isNotEmpty()) {
                                append("  <div class=\"answer-text\">${answer.replace("\n", "<br>")}</div>\n")
                            } else {
                                append("  <div class=\"empty-answer\">[No response provided]</div>\n")
                            }
                            append("</div>\n")
                        }
                    }

                    // Task sections
                    val taskSections = auditSections.filter { !it.isMacro && !it.isStrategy && !it.isOrganization && !it.isSystems && !it.isProductivity && !it.isFunction }
                    taskSections.forEach { section ->
                        append("<h3>${section.title}</h3>\n")
                        section.questions.forEach { q ->
                            val answer = ans[q.id]?.trim() ?: ""
                            append("<div class=\"question-container\">\n")
                            append("  <div class=\"question-text\">${q.questionNumber}. ${q.label}</div>\n")
                            if (answer.isNotEmpty()) {
                                append("  <div class=\"answer-text\">${answer.replace("\n", "<br>")}</div>\n")
                            } else {
                                append("  <div class=\"empty-answer\">[No response provided]</div>\n")
                            }
                            append("</div>\n")
                        }
                    }

                    append("<h2>Marketing Strategy</h2>\n")

                    // Strategy sections
                    val strategySections = auditSections.filter { it.isStrategy }
                    strategySections.forEach { section ->
                        append("<h3>${section.title}</h3>\n")
                        section.questions.forEach { q ->
                            val answer = ans[q.id]?.trim() ?: ""
                            append("<div class=\"question-container\">\n")
                            append("  <div class=\"question-text\">${q.questionNumber}. ${q.label}</div>\n")
                            if (answer.isNotEmpty()) {
                                append("  <div class=\"answer-text\">${answer.replace("\n", "<br>")}</div>\n")
                            } else {
                                append("  <div class=\"empty-answer\">[No response provided]</div>\n")
                            }
                            append("</div>\n")
                        }
                    }

                    append("<h2>Marketing Organization</h2>\n")

                    // Organization sections
                    val orgSections = auditSections.filter { it.isOrganization }
                    orgSections.forEach { section ->
                        append("<h3>${section.title}</h3>\n")
                        section.questions.forEach { q ->
                            val answer = ans[q.id]?.trim() ?: ""
                            append("<div class=\"question-container\">\n")
                            append("  <div class=\"question-text\">${q.questionNumber}. ${q.label}</div>\n")
                            if (answer.isNotEmpty()) {
                                append("  <div class=\"answer-text\">${answer.replace("\n", "<br>")}</div>\n")
                            } else {
                                append("  <div class=\"empty-answer\">[No response provided]</div>\n")
                            }
                            append("</div>\n")
                        }
                    }

                    append("<h2>Marketing Systems</h2>\n")

                    // Systems sections
                    val sysSections = auditSections.filter { it.isSystems }
                    sysSections.forEach { section ->
                        append("<h3>${section.title}</h3>\n")
                        section.questions.forEach { q ->
                            val answer = ans[q.id]?.trim() ?: ""
                            append("<div class=\"question-container\">\n")
                            append("  <div class=\"question-text\">${q.questionNumber}. ${q.label}</div>\n")
                            if (answer.isNotEmpty()) {
                                append("  <div class=\"answer-text\">${answer.replace("\n", "<br>")}</div>\n")
                            } else {
                                append("  <div class=\"empty-answer\">[No response provided]</div>\n")
                            }
                            append("</div>\n")
                        }
                    }

                    append("<h2>Marketing Productivity</h2>\n")

                    // Productivity sections
                    val prodSections = auditSections.filter { it.isProductivity }
                    prodSections.forEach { section ->
                        append("<h3>${section.title}</h3>\n")
                        section.questions.forEach { q ->
                            val answer = ans[q.id]?.trim() ?: ""
                            append("<div class=\"question-container\">\n")
                            append("  <div class=\"question-text\">${q.questionNumber}. ${q.label}</div>\n")
                            if (answer.isNotEmpty()) {
                                append("  <div class=\"answer-text\">${answer.replace("\n", "<br>")}</div>\n")
                            } else {
                                append("  <div class=\"empty-answer\">[No response provided]</div>\n")
                            }
                            append("</div>\n")
                        }
                    }

                    append("<h2>Marketing Function</h2>\n")

                    // Function sections
                    val funSections = auditSections.filter { it.isFunction }
                    funSections.forEach { section ->
                        append("<h3>${section.title}</h3>\n")
                        section.questions.forEach { q ->
                            val answer = ans[q.id]?.trim() ?: ""
                            append("<div class=\"question-container\">\n")
                            append("  <div class=\"question-text\">${q.questionNumber}. ${q.label}</div>\n")
                            if (answer.isNotEmpty()) {
                                append("  <div class=\"answer-text\">${answer.replace("\n", "<br>")}</div>\n")
                            } else {
                                append("  <div class=\"empty-answer\">[No response provided]</div>\n")
                            }
                            append("</div>\n")
                        }
                    }

                    append("<div class=\"footer\">\n")
                    append("  Report Generated on ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}<br>\n")
                    append("  Formulated by <strong>Anuj Khanna, Marketing Strategy Consultant</strong><br>\n")
                    append("  <em>All credit goes to Dr. Philip Kotler of Northwestern University for teaching us this Methodology</em>\n")
                    append("</div>\n")

                    append("</body>\n</html>\n")
                }

                // File name
                val fileName = "Marketing_Audit_Report_${p.organizationName.replace(" ", "_")}.doc"

                // Save in general files folder to share
                val sharedDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                if (sharedDir != null && !sharedDir.exists()) {
                    sharedDir.mkdirs()
                }
                val outputFile = File(sharedDir, fileName)
                FileOutputStream(outputFile).use { fos ->
                    fos.write(docContent.toByteArray(Charsets.UTF_8))
                }

                // Also try to save a copy in public downloads or app files for AI Studio viewer accessibility
                val publicDir = context.getExternalFilesDir(null)
                if (publicDir != null) {
                    val publicCopy = File(publicDir, "Marketing_Audit_Report.doc")
                    FileOutputStream(publicCopy).use { fos ->
                        fos.write(docContent.toByteArray(Charsets.UTF_8))
                    }
                }

                // Let's create proper file sharing Uri
                val authority = "${context.packageName}.provider"
                val fileUri = try {
                    FileProvider.getUriForFile(context, authority, outputFile)
                } catch (e: Exception) {
                    Uri.fromFile(outputFile)
                }

                _exportEvents.emit(ExportResult.Success(outputFile.absolutePath, fileUri))
            } catch (e: Exception) {
                _exportEvents.emit(ExportResult.Failure(e.localizedMessage ?: "Failed to generate file"))
            }
        }
    }

    fun clearAllAnswers(context: Context) {
        viewModelScope.launch {
            repository.clearAll()
            _showValidationErrors.value = false
            Toast.makeText(context, "All answers cleared successfully!", Toast.LENGTH_SHORT).show()
        }
    }
}

class AuditViewModelFactory(private val repository: AuditRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuditViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuditViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
