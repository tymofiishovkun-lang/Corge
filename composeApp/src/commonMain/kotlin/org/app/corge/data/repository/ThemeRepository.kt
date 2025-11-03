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
                q.insertTheme(AppTheme.LIGHT.id,    AppTheme.LIGHT.title,    is_paid = false, purchased = true)
                q.insertTheme(AppTheme.WABI.id,     AppTheme.WABI.title,     is_paid = true,  purchased = false)
                q.insertTheme(AppTheme.KINTSUGI.id, AppTheme.KINTSUGI.title, is_paid = true,  purchased = false)
            }

            val cur = q.getCurrentThemeId().executeAsOneOrNull()
            if (cur == null) {
                q.upsertCurrentTheme(AppTheme.LIGHT.id)
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
                purchased = row.purchased == true
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
