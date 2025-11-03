package org.app.corge.data.mappers

import org.app.corge.data.model.Category
import org.app.corge.data.model.Favorite
import org.app.corge.data.model.Message
import org.app.corge.data.model.MessageType
import org.app.corge.data.model.Session
import org.app.corge.data.model.Theme
import org.app.corge.data.Message as MessageDb
import org.app.corge.data.Session as SessionDb
import org.app.corge.data.Category as CategoryDb
import org.app.corge.data.Theme as ThemeDb
import org.app.corge.data.Favorite as FavoriteDb

fun MessageDb.toModel() = Message(
    id = id,
    orderIndex = order_index.toInt(),
    type = MessageType.entries.find { it.name.equals(type, ignoreCase = true) } ?: MessageType.PHRASE,
    category = category,
    textEn = text_en,
    illustrationName = illustration_name,
    ritual = ritual,
    whyItMatters = why_it_matters,
    recommendedTime = recommended_time,
    durationSeconds = duration_seconds?.toInt() ?: 0,
    breathingRelated = breathing_related == true
)

fun SessionDb.toModel() = Session(
    id = id,
    messageId = message_id,
    date = date,
    startedAt = started_at,
    durationSeconds = duration_seconds?.toInt() ?: 0,
    done = done == true,
    note = note
)

fun CategoryDb.toModel() = Category(
    id = id,
    title = title,
    description = description
)

fun ThemeDb.toModel() = Theme(
    id = id,
    name = name,
    isPaid = is_paid == true,
    purchased = purchased == true
)

fun FavoriteDb.toModel() = Favorite(
    messageId = message_id,
    addedAt = added_at
)