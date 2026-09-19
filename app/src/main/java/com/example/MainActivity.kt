package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MounirViewModel
import com.example.ui.components.AccessCodeGateDialog
import com.example.ui.components.AdminDashboardDialog
import com.example.ui.components.AdminLoginDialog
import com.example.ui.components.RecipeDetailSheet
import com.example.ui.components.RecipeEditorDialog
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MounirViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Wrap in Right-to-Left (RTL) for standard Arabic layout mirroring
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        val recipes by viewModel.recipes.collectAsStateWithLifecycle()
                        val accessCodes by viewModel.accessCodes.collectAsStateWithLifecycle()
                        val subscriptions by viewModel.subscriptions.collectAsStateWithLifecycle()

                        Box(modifier = Modifier.fillMaxSize()) {
                            // Primary Main Screen
                            MainScreen(
                                state = uiState,
                                recipes = recipes,
                                onCategorySelect = { viewModel.setCategoryFilter(it) },
                                onSearchChange = { viewModel.setSearchQuery(it) },
                                onRecipeClick = { viewModel.openRecipeDetails(it) },
                                onOpenAdmin = { viewModel.openAdminDashboard() },
                                onLockApp = { viewModel.lockUserAccess() },
                                onDismissBanner = { viewModel.dismissBanner() },
                                onOpenRecipeEditor = { viewModel.openRecipeEditor(null) }
                            )

                            // Access Code Gate (shown when locked)
                            if (!uiState.isUnlocked) {
                                AccessCodeGateDialog(
                                    codeInput = uiState.userCodeInput,
                                    errorMessage = uiState.accessCodeError,
                                    onCodeChange = { viewModel.onUserCodeInputChange(it) },
                                    onSubmit = { viewModel.checkUserCode() },
                                    onOpenAdminLogin = { viewModel.openAdminLoginDialog() }
                                )
                            }

                            // Admin Login Dialog
                            if (uiState.showAdminLoginDialog) {
                                AdminLoginDialog(
                                    passwordInput = uiState.adminPasswordInput,
                                    errorMessage = uiState.adminPasswordError,
                                    onPasswordChange = { viewModel.onAdminPasswordChange(it) },
                                    onLogin = { viewModel.loginAdmin() },
                                    onDismiss = { viewModel.closeAdminLoginDialog() }
                                )
                            }

                            // Admin Dashboard Dialog
                            if (uiState.showAdminDashboard) {
                                AdminDashboardDialog(
                                    recipes = recipes,
                                    accessCodes = accessCodes,
                                    subscriptions = subscriptions,
                                    onClose = { viewModel.closeAdminDashboard() },
                                    onLogout = { viewModel.logoutAdmin() },
                                    onGenerateCode = { viewModel.generateNewCode() },
                                    onDeleteCode = { viewModel.deleteAccessCode(it) },
                                    onSaveSubscriptions = { viewModel.saveSubscriptions(it) },
                                    onDeleteSubscription = { viewModel.deleteSubscription(it) },
                                    onOpenRecipeEditor = { viewModel.openRecipeEditor(it) },
                                    onDeleteRecipe = { viewModel.deleteRecipe(it) }
                                )
                            }

                            // Recipe Details Modal Sheet
                            uiState.activeRecipeForDetails?.let { recipe ->
                                RecipeDetailSheet(
                                    recipe = recipe,
                                    batchWeightKg = uiState.batchWeightKg,
                                    onBatchWeightChange = { viewModel.setBatchWeight(it) },
                                    onDismiss = { viewModel.closeRecipeDetails() }
                                )
                            }

                            // Recipe Editor Dialog (Add/Edit)
                            if (uiState.isEditingRecipe) {
                                RecipeEditorDialog(
                                    recipeToEdit = uiState.recipeBeingEdited,
                                    onSave = { id, category, title, percentages, steps, warnings, videoUrl ->
                                        viewModel.saveRecipe(id, category, title, percentages, steps, warnings, videoUrl)
                                    },
                                    onDismiss = { viewModel.closeRecipeEditor() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
