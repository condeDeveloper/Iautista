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
    /** Idempotente — só insere se o banco estiver vazio; também aplica patches de imagens. */
    suspend fun seedIfEmpty() {
        if (categoryDao.count() == 0) seedCategories()
        if (routineDao.count() == 0) seedRoutine()
        patchMissingImageUris()
        patchImageUriSize()
    }

    /**
     * Corrige itens que foram seedados sem ARASAAC ID mas agora têm um.
     * Idempotente: só atualiza registros onde imageUri ainda é null.
     */
    private suspend fun patchMissingImageUris() {
        // Sentimentos (cat 2)
        itemDao.updateImageUriIfNull("Bravo", 2L, arasaac(35533))
    }

    /**
     * Migra URLs de _500.png → _300.png em todos os itens default.
     * Idempotente: REPLACE em string que não contém _500.png é no-op.
     */
    private suspend fun patchImageUriSize() {
        itemDao.replaceImageUriSuffix("_500.png", "_300.png")
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
            add(item(1, "Água",    "💧", 0, arasaac(32464))); add(item(1, "Comida",  "🍽️", 1, arasaac(4611)))
            add(item(1, "Leite",   "🥛", 2, arasaac(2445)));  add(item(1, "Suco",    "🧃", 3))
            add(item(1, "Pão",     "🍞", 4, arasaac(2494)));  add(item(1, "Fruta",   "🍎", 5, arasaac(28339)))
            add(item(1, "Biscoito","🍪", 6, arasaac(8295)));  add(item(1, "Iogurte", "🥣", 7, arasaac(2618)))

            // 2 · Sentimentos
            add(item(2, "Feliz",     "😊", 0, arasaac(9907)));  add(item(2, "Triste",   "😢", 1, arasaac(35545)))
            add(item(2, "Bravo",     "😠", 2, arasaac(35533))); add(item(2, "Assustado","😨", 3, arasaac(35535)))
            add(item(2, "Cansado",   "😴", 4, arasaac(35537))); add(item(2, "Ansioso",  "😟", 5, arasaac(30484)))
            add(item(2, "Calmo",     "😌", 6, arasaac(31310))); add(item(2, "Carinho",  "🥰", 7, arasaac(8020)))

            // 3 · Dor e desconforto
            add(item(3, "Dói",          "🤕", 0, arasaac(2367)));  add(item(3, "Cabeça",       "🧠", 1, arasaac(2673)))
            add(item(3, "Barriga",      "🤢", 2, arasaac(2786)));  add(item(3, "Muito barulho","🔊", 3, arasaac(38945)))
            add(item(3, "Muita luz",    "💡", 4, arasaac(8619)));  add(item(3, "Calor",         "🌡️",5, arasaac(35561)))
            add(item(3, "Frio",         "🥶", 6, arasaac(4652)))

            // 4 · Pessoas
            add(item(4, "Mamãe",      "👩",   0, arasaac(2458))); add(item(4, "Papai",     "👨",   1, arasaac(2497)))
            add(item(4, "Vovó",       "👵",   2, arasaac(23718)));add(item(4, "Vovô",      "👴",   3))
            add(item(4, "Irmão",      "🧒",   4, arasaac(2423))); add(item(4, "Amigo",     "🧑",   5, arasaac(25790)))
            add(item(4, "Professora", "👩‍🏫",  6, arasaac(2456))); add(item(4, "Terapeuta", "🧑‍⚕️", 7, arasaac(2454)))

            // 5 · Lugares
            add(item(5, "Casa",     "🏠", 0, arasaac(6964)));  add(item(5, "Escola",  "🏫", 1, arasaac(32446)))
            add(item(5, "Banheiro", "🚽", 2));                  add(item(5, "Quarto",  "🛏️", 3, arasaac(5988)))
            add(item(5, "Cozinha",  "🍳", 4, arasaac(25966))); add(item(5, "Parque",  "🌳", 5, arasaac(39572)))
            add(item(5, "Hospital", "🏥", 6, arasaac(36210)))

            // 6 · Atividades
            add(item(6, "Brincar",     "🎮", 0, arasaac(23392))); add(item(6, "Desenhar",    "✏️", 1, arasaac(8088)))
            add(item(6, "Assistir TV", "📺", 2, arasaac(25498))); add(item(6, "Música",      "🎵", 3, arasaac(24791)))
            add(item(6, "Ajuda",       "🆘", 4, arasaac(12252))); add(item(6, "Abraço",      "🤗", 5, arasaac(4550)))
            add(item(6, "Passear",     "🚶", 6, arasaac(29951)))

            // 7 · Higiene
            add(item(7, "Banho",     "🛁", 0, arasaac(38587))); add(item(7, "Dente",    "🦷", 1, arasaac(10267)))
            add(item(7, "Banheiro",  "🚽", 2));                  add(item(7, "Lavar mão","🙌", 3, arasaac(34826)))

            // 8 · Escola
            add(item(8, "Escola",  "🎒", 0, arasaac(32446))); add(item(8, "Lição",  "📝", 1))
            add(item(8, "Recreio", "⚽", 2, arasaac(33064))); add(item(8, "Lanche", "🥪", 3, arasaac(4695)))

            // 9 · Brincadeiras
            add(item(9, "Bola",      "⚽", 0, arasaac(3241)));  add(item(9, "Boneca",    "🪆", 1, arasaac(26238)))
            add(item(9, "Puzzle",    "🧩", 2, arasaac(2540)));  add(item(9, "Parquinho", "🛝", 3))
            add(item(9, "Tinta",     "🎨", 4, arasaac(4870)))

            // 10 · Descanso
            add(item(10, "Dormir",    "😴", 0, arasaac(6479)));  add(item(10, "Descansar","🛋️", 1, arasaac(16643)))
            add(item(10, "Silêncio",  "🤫", 2, arasaac(5936)))
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

    private fun arasaac(id: Int): String =
        "https://static.arasaac.org/pictograms/$id/${id}_300.png"

    private fun item(categoryId: Long, text: String, emoji: String, order: Int, imageUri: String? = null) =
        CommunicationItemEntity(
            id = 0, categoryId = categoryId, text = text, emoji = emoji,
            imageRes = null, imageUri = imageUri, audioUri = null,
            isFavorite = false, isDefault = true, order = order, usageCount = 0,
        )

    private fun rot(text: String, emoji: String, status: String, order: Int, hour: Int) =
        RoutineItemEntity(id = 0, text = text, emoji = emoji,
            status = status, order = order, suggestedHour = hour)
}
