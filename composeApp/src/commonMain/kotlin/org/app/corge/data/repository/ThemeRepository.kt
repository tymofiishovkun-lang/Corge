package org.app.corge.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.app.corge.data.Corge
import org.app.corge.data.model.Theme
import org.app.corge.screens.settings.AppTheme

interface ThemeRepository {
    suspend fun initializeThemes()
    suspend fun getAllThemes(): List<Theme>
    suspend fun setCurrentTheme(themeId: String)
    suspend fun getCurrentThemeId(): String?
    suspend fun markThemePurchased(themeId: String)
    val currentThemeId: StateFlow<String?>
}

class ThemeRepositoryImpl(
    private val db: Corge
) : ThemeRepository {

    private val q = db.corgeQueries
    private val _currentThemeId = MutableStateFlow<String?>(null)
    override val currentThemeId: StateFlow<String?> = _currentThemeId

    override suspend fun initializeThemes() = withContext(Dispatchers.Default) {
        q.transaction {
            val count = q.countThemes().executeAsOne()
            if (count == 0L) {
                q.insertTheme(
                    id = "light",
                    name = "Light Minimal",
                    is_paid = false,
                    purchased = true,
                    preview_res = "bg_light",
                    primary_color = 0xFFFFFFFF,
                    splash_text = "Track easy!",
                    price = null
                )

                q.insertTheme(
                    id = "wabi",
                    name = "Wabi-Sabi Ink Theme",
                    is_paid = true,
                    purchased = false,
                    preview_res = "bg_wabi",
                    primary_color = 0xFF000000,
                    splash_text = "Calm imperfection.",
                    price = 1.99
                )

                q.insertTheme(
                    id = "kintsugi",
                    name = "Kintsugi Night Theme",
                    is_paid = true,
                    purchased = false,
                    preview_res = "bg_kintsugi",
                    primary_color = 0xFFFFD700,
                    splash_text = "Beauty in repair.",
                    price = 1.99
                )
            }

            val cur = q.getCurrentThemeId().executeAsOneOrNull()
            if (cur == null) {
                q.upsertCurrentTheme("light")
            }
        }

        _currentThemeId.value = q.getCurrentThemeId().executeAsOneOrNull()
    }

    override suspend fun getAllThemes(): List<Theme> = withContext(Dispatchers.Default) {
        q.selectAllTheme().executeAsList().map { row ->
            Theme(
                id = row.id,
                name = row.name,
                isPaid = row.is_paid == true,
                purchased = row.purchased == true,
                previewRes = row.preview_res ?: "",
                primaryColor = row.primary_color ?: 0L,
                splashText = row.splash_text ?: "",
                price = row.price
            )
        }
    }

    override suspend fun getCurrentThemeId(): String? = withContext(Dispatchers.Default) {
        val id = q.getCurrentThemeId().executeAsOneOrNull()
        _currentThemeId.value = id
        id
    }

    override suspend fun setCurrentTheme(themeId: String) = withContext(Dispatchers.Default) {
        q.upsertCurrentTheme(themeId)
        _currentThemeId.value = themeId
    }

    override suspend fun markThemePurchased(themeId: String) = withContext(Dispatchers.Default) {
        q.updatePurchased(themeId)
        q.upsertCurrentTheme(themeId)
        _currentThemeId.value = themeId
    }
}
