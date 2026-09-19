package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AccessCode
import com.example.data.model.Recipe
import com.example.data.model.Subscription
import com.example.data.repository.MounirRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MounirUiState(
    val isUnlocked: Boolean = false,
    val isAdminLoggedIn: Boolean = false,
    val selectedCategory: String = "all", // "all", "cleaners", "cars"
    val searchQuery: String = "",
    val activeRecipeForDetails: Recipe? = null,
    val isEditingRecipe: Boolean = false,
    val recipeBeingEdited: Recipe? = null,
    val showAdminLoginDialog: Boolean = false,
    val showAdminDashboard: Boolean = false,
    val userCodeInput: String = "",
    val accessCodeError: String? = null,
    val adminPasswordInput: String = "",
    val adminPasswordError: String? = null,
    val bannerMessage: String? = null,
    val batchWeightKg: Double = 1.0 // 1 kg default batch calculator
)

class MounirViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = MounirRepository(database, application)

    private val _uiState = MutableStateFlow(
        MounirUiState(isUnlocked = repository.isAccessUnlocked())
    )
    val uiState: StateFlow<MounirUiState> = _uiState

    val recipes: StateFlow<List<Recipe>> = repository.allRecipes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accessCodes: StateFlow<List<AccessCode>> = repository.allCodes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subscriptions: StateFlow<List<Subscription>> = repository.allSubscriptions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    fun onUserCodeInputChange(input: String) {
        val filtered = input.take(8).uppercase()
        _uiState.update { it.copy(userCodeInput = filtered, accessCodeError = null) }
    }

    fun checkUserCode() {
        val code = _uiState.value.userCodeInput.trim().uppercase()
        if (code.isBlank()) {
            _uiState.update { it.copy(accessCodeError = "الرجاء إدخال رمز الوصول المكون من 8 خانات") }
            return
        }
        viewModelScope.launch {
            val valid = repository.validateAccessCode(code)
            if (valid) {
                repository.setAccessUnlocked(true)
                _uiState.update {
                    it.copy(
                        isUnlocked = true,
                        accessCodeError = null,
                        bannerMessage = "تم التحقق بنجاح! أهلاً بك في Mounir Formule"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(accessCodeError = "رمز غير صحيح. يرجى التأكد من الرمز والمحاولة مجدداً")
                }
            }
        }
    }

    fun onAdminPasswordChange(password: String) {
        _uiState.update { it.copy(adminPasswordInput = password, adminPasswordError = null) }
    }

    fun openAdminLoginDialog() {
        _uiState.update { it.copy(showAdminLoginDialog = true, adminPasswordInput = "", adminPasswordError = null) }
    }

    fun closeAdminLoginDialog() {
        _uiState.update { it.copy(showAdminLoginDialog = false, adminPasswordInput = "", adminPasswordError = null) }
    }

    fun loginAdmin() {
        val pass = _uiState.value.adminPasswordInput
        val isCorrect = repository.verifyAdminPassword(pass)
        if (isCorrect) {
            _uiState.update {
                it.copy(
                    isAdminLoggedIn = true,
                    showAdminLoginDialog = false,
                    showAdminDashboard = true,
                    adminPasswordInput = "",
                    adminPasswordError = null,
                    isUnlocked = true, // Admin automatically has access to recipes
                    bannerMessage = "تم تسجيل الدخول كمسؤول بنجاح"
                )
            }
        } else {
            _uiState.update { it.copy(adminPasswordError = "كلمة مرور الأدمن خاطئة") }
        }
    }

    fun logoutAdmin() {
        _uiState.update {
            it.copy(
                isAdminLoggedIn = false,
                showAdminDashboard = false,
                bannerMessage = "تم تسجيل الخروج من لوحة الأدمن"
            )
        }
    }

    fun lockUserAccess() {
        repository.setAccessUnlocked(false)
        _uiState.update {
            it.copy(
                isUnlocked = false,
                isAdminLoggedIn = false,
                showAdminDashboard = false,
                userCodeInput = "",
                accessCodeError = null
            )
        }
    }

    fun openAdminDashboard() {
        if (_uiState.value.isAdminLoggedIn) {
            _uiState.update { it.copy(showAdminDashboard = true) }
        } else {
            openAdminLoginDialog()
        }
    }

    fun closeAdminDashboard() {
        _uiState.update { it.copy(showAdminDashboard = false) }
    }

    fun setCategoryFilter(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun openRecipeDetails(recipe: Recipe) {
        _uiState.update { it.copy(activeRecipeForDetails = recipe, batchWeightKg = 1.0) }
    }

    fun closeRecipeDetails() {
        _uiState.update { it.copy(activeRecipeForDetails = null) }
    }

    fun setBatchWeight(kg: Double) {
        _uiState.update { it.copy(batchWeightKg = kg.coerceAtLeast(0.1)) }
    }

    fun openRecipeEditor(recipe: Recipe? = null) {
        _uiState.update {
            it.copy(
                isEditingRecipe = true,
                recipeBeingEdited = recipe
            )
        }
    }

    fun closeRecipeEditor() {
        _uiState.update {
            it.copy(
                isEditingRecipe = false,
                recipeBeingEdited = null
            )
        }
    }

    fun saveRecipe(
        id: String?,
        category: String,
        title: String,
        percentages: String,
        steps: String,
        warnings: String,
        videoUrl: String
    ) {
        viewModelScope.launch {
            val recipe = Recipe(
                id = id ?: java.util.UUID.randomUUID().toString(),
                category = category,
                title = title.trim(),
                percentages = percentages.trim(),
                steps = steps.trim(),
                warnings = warnings.trim(),
                videoUrl = videoUrl.trim(),
                createdAt = System.currentTimeMillis()
            )
            repository.insertRecipe(recipe)
            closeRecipeEditor()
            _uiState.update { it.copy(bannerMessage = "تم حفظ الوصفة بنجاح") }
        }
    }

    fun deleteRecipe(id: String) {
        viewModelScope.launch {
            repository.deleteRecipeById(id)
            if (_uiState.value.activeRecipeForDetails?.id == id) {
                closeRecipeDetails()
            }
            _uiState.update { it.copy(bannerMessage = "تم حذف الوصفة") }
        }
    }

    fun generateNewCode() {
        viewModelScope.launch {
            val code = repository.generateNewCode(note = "تم التوليد بواسطة الأدمن")
            _uiState.update { it.copy(bannerMessage = "تم توليد الكود: $code بنجاح") }
        }
    }

    fun deleteAccessCode(code: String) {
        viewModelScope.launch {
            repository.deleteAccessCode(code)
            _uiState.update { it.copy(bannerMessage = "تم حذف الكود $code") }
        }
    }

    fun saveSubscriptions(linesText: String) {
        viewModelScope.launch {
            val list = linesText.lines().map { it.trim() }.filter { it.isNotEmpty() }
            repository.saveSubscriptionsBulk(list)
            _uiState.update { it.copy(bannerMessage = "تم حفظ ${list.size} اشتراك بنجاح") }
        }
    }

    fun deleteSubscription(id: Long) {
        viewModelScope.launch {
            repository.deleteSubscription(id)
            _uiState.update { it.copy(bannerMessage = "تم حذف الاشتراك") }
        }
    }

    fun dismissBanner() {
        _uiState.update { it.copy(bannerMessage = null) }
    }
}
