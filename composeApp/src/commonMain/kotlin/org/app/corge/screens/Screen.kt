package org.app.corge.screens

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("nav_home")
    data object Search : Screen("search")
    data object SearchResults : Screen("search_results")
    data class Details(val messageId: Long, val date: String? = null) :
        Screen("details/$messageId${if (date != null) "?date=$date" else ""}") {
        companion object {
            private const val PREFIX = "details/"

            fun route(id: Long, date: String? = null): String =
                if (date == null) "$PREFIX$id" else "$PREFIX$id?date=$date"

            fun isMatch(route: String) = route.startsWith(PREFIX)

            fun parse(route: String): Pair<Long, String?>? {
                val rest = route.removePrefix(PREFIX)
                val qIdx = rest.indexOf('?')
                val idStr = if (qIdx == -1) rest else rest.substring(0, qIdx)
                val id = idStr.toLongOrNull() ?: return null
                val date = if (qIdx == -1) null
                else rest.substring(qIdx + 1).removePrefix("date=").ifBlank { null }
                return id to date
            }
        }
    }

    data class Session(val messageId: Long) : Screen("session/$messageId") {
        companion object {
            private const val PREFIX = "session/"
            fun route(id: Long) = "$PREFIX$id"
            fun isMatch(route: String) = route.startsWith(PREFIX)
            fun idFrom(route: String)  = route.removePrefix(PREFIX).toLongOrNull()
        }
    }
    data object Favorites : Screen("favorites")
    data object Journal : Screen("journal")
    data object Stats : Screen("stats")
    data object Setting : Screen("setting")
    data object About : Screen("about")
    object Web : Screen("web") {

        fun route(url: String): String =
            "${this.route}:$url"

        fun parse(route: String): String? =
            route.removePrefix("${this.route}:")
    }
}