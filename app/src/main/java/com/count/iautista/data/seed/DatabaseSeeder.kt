package com.count.iautista.data.seed

import com.count.iautista.data.local.dao.CommunicationCategoryDao
import com.count.iautista.data.local.dao.CommunicationItemDao
import com.count.iautista.data.local.dao.RoutineItemDao
import com.count.iautista.data.local.entity.CommunicationCategoryEntity
import com.count.iautista.data.local.entity.CommunicationItemEntity
import com.count.iautista.data.local.entity.RoutineItemEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val categoryDao: CommunicationCategoryDao,
    private val itemDao: CommunicationItemDao,
    private val routineDao: RoutineItemDao,
) {
    /** Idempotente — só insere se o banco estiver vazio */
    suspend fun seedIfEmpty() {
        if (categoryDao.count() == 0) seedCategories()
        if (routineDao.count() == 0) seedRoutine()
    }

    // ── Categorias + Itens ───────────────────────────────────────────────────

    private suspend fun seedCategories() {
        val categories = listOf(
            cat(1,  "Comida e bebida",   "🍎", 0xFFFFF3E0L, 0),
            cat(2,  "Sentimentos",       "❤️", 0xFFFCE4ECL, 1),
            cat(3,  "Dor e desconforto", "🤕", 0xFFF3E5F5L, 2),
            cat(4,  "Pessoas",           "👨‍👩‍👧", 0xFFE3F2FDL, 3),
            cat(5,  "Lugares",           "🏠", 0xFFE8F5E9L, 4),
            cat(6,  "Atividades",        "🎯", 0xFFE0F7FAL, 5),
            cat(7,  "Higiene",           "🚿", 0xFFE8EAF6L, 6),
            cat(8,  "Escola",            "🎒", 0xFFFFF9C4L, 7),
            cat(9,  "Brincadeiras",      "🧸", 0xFFF1F8E9L, 8),
            cat(10, "Descanso",          "😴", 0xFFEDE7F6L, 9),
        )
        categoryDao.insertAll(categories)

        val items = buildList {
            // 1 · Comida e bebida
            add(item(1, "Água",    "💧", 0)); add(item(1, "Comida",  "🍽️", 1))
            add(item(1, "Leite",   "🥛", 2)); add(item(1, "Suco",    "🧃", 3))
            add(item(1, "Pão",     "🍞", 4)); add(item(1, "Fruta",   "🍎", 5))
            add(item(1, "Biscoito","🍪", 6)); add(item(1, "Iogurte", "🥣", 7))

            // 2 · Sentimentos
            add(item(2, "Feliz",     "😊", 0)); add(item(2, "Triste",   "😢", 1))
            add(item(2, "Bravo",     "😠", 2)); add(item(2, "Assustado","😨", 3))
            add(item(2, "Cansado",   "😴", 4)); add(item(2, "Ansioso",  "😟", 5))
            add(item(2, "Calmo",     "😌", 6)); add(item(2, "Carinho",  "🥰", 7))

            // 3 · Dor e desconforto
            add(item(3, "Dói",         "🤕", 0)); add(item(3, "Cabeça",      "🧠", 1))
            add(item(3, "Barriga",     "🤢", 2)); add(item(3, "Muito barulho","🔊", 3))
            add(item(3, "Muita luz",   "💡", 4)); add(item(3, "Calor",        "🌡️",5))
            add(item(3, "Frio",        "🥶", 6))

            // 4 · Pessoas
            add(item(4, "Mamãe",       "👩",  0)); add(item(4, "Papai",      "👨",  1))
            add(item(4, "Vovó",        "👵",  2)); add(item(4, "Vovô",       "👴",  3))
            add(item(4, "Irmão",       "🧒",  4)); add(item(4, "Amigo",      "🧑",  5))
            add(item(4, "Professora",  "👩‍🏫", 6)); add(item(4, "Terapeuta",  "🧑‍⚕️",7))

            // 5 · Lugares
            add(item(5, "Casa",     "🏠", 0)); add(item(5, "Escola",  "🏫", 1))
            add(item(5, "Banheiro", "🚽", 2)); add(item(5, "Quarto",  "🛏️", 3))
            add(item(5, "Cozinha",  "🍳", 4)); add(item(5, "Parque",  "🌳", 5))
            add(item(5, "Hospital", "🏥", 6))

            // 6 · Atividades
            add(item(6, "Brincar",     "🎮", 0)); add(item(6, "Desenhar",    "✏️", 1))
            add(item(6, "Assistir TV", "📺", 2)); add(item(6, "Música",      "🎵", 3))
            add(item(6, "Ajuda",       "🆘", 4)); add(item(6, "Abraço",      "🤗", 5))
            add(item(6, "Passear",     "🚶", 6))

            // 7 · Higiene
            add(item(7, "Banho",    "🛁", 0)); add(item(7, "Dente",  "🦷", 1))
            add(item(7, "Banheiro", "🚽", 2)); add(item(7, "Lavar mão","🙌",3))

            // 8 · Escola
            add(item(8, "Escola",   "🎒", 0)); add(item(8, "Lição",  "📝", 1))
            add(item(8, "Recreio",  "⚽", 2)); add(item(8, "Lanche", "🥪", 3))

            // 9 · Brincadeiras
            add(item(9, "Bola",     "⚽", 0)); add(item(9, "Boneca", "🪆", 1))
            add(item(9, "Puzzle",   "🧩", 2)); add(item(9, "Parquinho","🛝",3))
            add(item(9, "Tinta",    "🎨", 4))

            // 10 · Descanso
            add(item(10, "Dormir",    "😴", 0)); add(item(10, "Descansar","🛋️",1))
            add(item(10, "Silêncio",  "🤫", 2))
        }
        itemDao.insertAll(items)
    }

    // ── Rotina ───────────────────────────────────────────────────────────────

    private suspend fun seedRoutine() {
        val routine = listOf(
            rot("Acordar",          "☀️", "DONE",  0, 7),
            rot("Escovar os dentes","🦷", "DONE",  1, 7),
            rot("Café da manhã",    "🥐", "NOW",   2, 8),
            rot("Escola",           "🎒", "NEXT",  3, 9),
            rot("Terapia",          "🌟", "LATER", 4, 11),
            rot("Almoço",           "🍽️","LATER", 5, 12),
            rot("Brincar",          "🧸", "LATER", 6, 14),
            rot("Banho",            "🛁", "LATER", 7, 17),
            rot("Jantar",           "🍽️","LATER", 8, 18),
            rot("Dormir",           "😴", "LATER", 9, 21),
        )
        routineDao.insertAll(routine)
    }

    // ── Construtores compactos ────────────────────────────────────────────────

    private fun cat(id: Long, name: String, emoji: String, bg: Long, order: Int) =
        CommunicationCategoryEntity(id = id, name = name, emoji = emoji,
            backgroundColor = bg, order = order, isDefault = true)

    private fun item(categoryId: Long, text: String, emoji: String, order: Int) =
        CommunicationItemEntity(
            id = 0, categoryId = categoryId, text = text, emoji = emoji,
            imageRes = null, imageUri = null, audioUri = null,
            isFavorite = false, isDefault = true, order = order, usageCount = 0,
        )

    private fun rot(text: String, emoji: String, status: String, order: Int, hour: Int) =
        RoutineItemEntity(id = 0, text = text, emoji = emoji,
            status = status, order = order, suggestedHour = hour)
}
