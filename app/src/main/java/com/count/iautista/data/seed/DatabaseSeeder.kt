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
    /** Idempotente — só insere se o banco estiver vazio; aplica patches de imagens locais. */
    suspend fun seedIfEmpty() {
        if (categoryDao.count() == 0) seedCategories()
        if (routineDao.count() == 0) seedRoutine()
        // Migra installs existentes (ARASAAC URLs → drawables locais bundled no APK)
        patchToLocalDrawables()
    }

    /**
     * Substitui todas as imageUri dos itens default por android.resource:// locais.
     * Idempotente: forceUpdateImageUri atualiza sempre, mesmo que já seja local.
     * Novos installs recebem as URIs locais direto no seedCategories(),
     * mas este patch garante a migração para quem já tinha o app instalado.
     */
    private suspend fun patchToLocalDrawables() {
        // Cat 1 · Comida e bebida
        itemDao.forceUpdateImageUri("Água",     1L, local("agua"))
        itemDao.forceUpdateImageUri("Comida",   1L, local("comida"))
        itemDao.forceUpdateImageUri("Leite",    1L, local("leite"))
        itemDao.forceUpdateImageUri("Pão",      1L, local("pao"))
        itemDao.forceUpdateImageUri("Fruta",    1L, local("fruta"))
        itemDao.forceUpdateImageUri("Biscoito", 1L, local("biscoito"))
        itemDao.forceUpdateImageUri("Iogurte",  1L, local("iogurte"))
        // Cat 2 · Sentimentos
        itemDao.forceUpdateImageUri("Feliz",     2L, local("feliz"))
        itemDao.forceUpdateImageUri("Triste",    2L, local("triste"))
        itemDao.forceUpdateImageUri("Bravo",     2L, local("bravo"))
        itemDao.forceUpdateImageUri("Assustado", 2L, local("assustado"))
        itemDao.forceUpdateImageUri("Cansado",   2L, local("cansado"))
        itemDao.forceUpdateImageUri("Ansioso",   2L, local("ansioso"))
        itemDao.forceUpdateImageUri("Calmo",     2L, local("calmo"))
        itemDao.forceUpdateImageUri("Carinho",   2L, local("carinho"))
        // Cat 3 · Dor e desconforto
        itemDao.forceUpdateImageUri("Dói",          3L, local("doi"))
        itemDao.forceUpdateImageUri("Cabeça",        3L, local("cabeca"))
        itemDao.forceUpdateImageUri("Barriga",       3L, local("barriga"))
        itemDao.forceUpdateImageUri("Muito barulho", 3L, local("muito_barulho"))
        itemDao.forceUpdateImageUri("Muita luz",     3L, local("muita_luz"))
        itemDao.forceUpdateImageUri("Calor",         3L, local("calor"))
        itemDao.forceUpdateImageUri("Frio",          3L, local("frio"))
        // Cat 4 · Pessoas
        itemDao.forceUpdateImageUri("Mamãe",      4L, local("mamae"))
        itemDao.forceUpdateImageUri("Papai",      4L, local("papai"))
        itemDao.forceUpdateImageUri("Vovó",       4L, local("vovo"))
        itemDao.forceUpdateImageUri("Irmão",      4L, local("irmao"))
        itemDao.forceUpdateImageUri("Amigo",      4L, local("amigo"))
        itemDao.forceUpdateImageUri("Professora", 4L, local("professora"))
        itemDao.forceUpdateImageUri("Terapeuta",  4L, local("terapeuta"))
        // Cat 5 · Lugares
        itemDao.forceUpdateImageUri("Casa",     5L, local("casa"))
        itemDao.forceUpdateImageUri("Escola",   5L, local("escola"))
        itemDao.forceUpdateImageUri("Quarto",   5L, local("quarto"))
        itemDao.forceUpdateImageUri("Cozinha",  5L, local("cozinha"))
        itemDao.forceUpdateImageUri("Parque",   5L, local("parque"))
        itemDao.forceUpdateImageUri("Hospital", 5L, local("hospital"))
        // Cat 6 · Atividades
        itemDao.forceUpdateImageUri("Brincar",     6L, local("brincar"))
        itemDao.forceUpdateImageUri("Desenhar",    6L, local("desenhar"))
        itemDao.forceUpdateImageUri("Assistir TV", 6L, local("assistir_tv"))
        itemDao.forceUpdateImageUri("Música",      6L, local("musica"))
        itemDao.forceUpdateImageUri("Ajuda",       6L, local("ajuda"))
        itemDao.forceUpdateImageUri("Abraço",      6L, local("abraco"))
        itemDao.forceUpdateImageUri("Passear",     6L, local("passear"))
        // Cat 7 · Higiene
        itemDao.forceUpdateImageUri("Banho",     7L, local("banho"))
        itemDao.forceUpdateImageUri("Dente",     7L, local("dente"))
        itemDao.forceUpdateImageUri("Lavar mão", 7L, local("lavar_mao"))
        // Cat 8 · Escola
        itemDao.forceUpdateImageUri("Escola",  8L, local("escola"))
        itemDao.forceUpdateImageUri("Recreio", 8L, local("recreio"))
        itemDao.forceUpdateImageUri("Lanche",  8L, local("lanche"))
        // Cat 9 · Brincadeiras
        itemDao.forceUpdateImageUri("Bola",   9L, local("bola"))
        itemDao.forceUpdateImageUri("Boneca", 9L, local("boneca"))
        itemDao.forceUpdateImageUri("Puzzle", 9L, local("puzzle"))
        itemDao.forceUpdateImageUri("Tinta",  9L, local("tinta"))
        // Cat 10 · Descanso
        itemDao.forceUpdateImageUri("Dormir",    10L, local("dormir"))
        itemDao.forceUpdateImageUri("Descansar", 10L, local("descansar"))
        itemDao.forceUpdateImageUri("Silêncio",  10L, local("silencio"))
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
            add(item(1, "Água",    "💧", 0, local("agua")));    add(item(1, "Comida",  "🍽️", 1, local("comida")))
            add(item(1, "Leite",   "🥛", 2, local("leite")));   add(item(1, "Suco",    "🧃", 3))
            add(item(1, "Pão",     "🍞", 4, local("pao")));     add(item(1, "Fruta",   "🍎", 5, local("fruta")))
            add(item(1, "Biscoito","🍪", 6, local("biscoito"))); add(item(1, "Iogurte", "🥣", 7, local("iogurte")))

            // 2 · Sentimentos
            add(item(2, "Feliz",     "😊", 0, local("feliz")));     add(item(2, "Triste",   "😢", 1, local("triste")))
            add(item(2, "Bravo",     "😠", 2, local("bravo")));     add(item(2, "Assustado","😨", 3, local("assustado")))
            add(item(2, "Cansado",   "😴", 4, local("cansado")));   add(item(2, "Ansioso",  "😟", 5, local("ansioso")))
            add(item(2, "Calmo",     "😌", 6, local("calmo")));     add(item(2, "Carinho",  "🥰", 7, local("carinho")))

            // 3 · Dor e desconforto
            add(item(3, "Dói",          "🤕", 0, local("doi")));          add(item(3, "Cabeça",       "🧠", 1, local("cabeca")))
            add(item(3, "Barriga",      "🤢", 2, local("barriga")));      add(item(3, "Muito barulho","🔊", 3, local("muito_barulho")))
            add(item(3, "Muita luz",    "💡", 4, local("muita_luz")));    add(item(3, "Calor",        "🌡️",5, local("calor")))
            add(item(3, "Frio",         "🥶", 6, local("frio")))

            // 4 · Pessoas
            add(item(4, "Mamãe",      "👩",   0, local("mamae")));    add(item(4, "Papai",     "👨",   1, local("papai")))
            add(item(4, "Vovó",       "👵",   2, local("vovo")));     add(item(4, "Vovô",      "👴",   3))
            add(item(4, "Irmão",      "🧒",   4, local("irmao")));    add(item(4, "Amigo",     "🧑",   5, local("amigo")))
            add(item(4, "Professora", "👩‍🏫",  6, local("professora"))); add(item(4, "Terapeuta", "🧑‍⚕️", 7, local("terapeuta")))

            // 5 · Lugares
            add(item(5, "Casa",     "🏠", 0, local("casa")));    add(item(5, "Escola",  "🏫", 1, local("escola")))
            add(item(5, "Banheiro", "🚽", 2));                    add(item(5, "Quarto",  "🛏️", 3, local("quarto")))
            add(item(5, "Cozinha",  "🍳", 4, local("cozinha"))); add(item(5, "Parque",  "🌳", 5, local("parque")))
            add(item(5, "Hospital", "🏥", 6, local("hospital")))

            // 6 · Atividades
            add(item(6, "Brincar",     "🎮", 0, local("brincar")));     add(item(6, "Desenhar",    "✏️", 1, local("desenhar")))
            add(item(6, "Assistir TV", "📺", 2, local("assistir_tv"))); add(item(6, "Música",      "🎵", 3, local("musica")))
            add(item(6, "Ajuda",       "🆘", 4, local("ajuda")));       add(item(6, "Abraço",      "🤗", 5, local("abraco")))
            add(item(6, "Passear",     "🚶", 6, local("passear")))

            // 7 · Higiene
            add(item(7, "Banho",     "🛁", 0, local("banho"))); add(item(7, "Dente",    "🦷", 1, local("dente")))
            add(item(7, "Banheiro",  "🚽", 2));                  add(item(7, "Lavar mão","🙌", 3, local("lavar_mao")))

            // 8 · Escola
            add(item(8, "Escola",  "🎒", 0, local("escola"))); add(item(8, "Lição",  "📝", 1))
            add(item(8, "Recreio", "⚽", 2, local("recreio"))); add(item(8, "Lanche", "🥪", 3, local("lanche")))

            // 9 · Brincadeiras
            add(item(9, "Bola",      "⚽", 0, local("bola")));    add(item(9, "Boneca",    "🪆", 1, local("boneca")))
            add(item(9, "Puzzle",    "🧩", 2, local("puzzle")));  add(item(9, "Parquinho", "🛝", 3))
            add(item(9, "Tinta",     "🎨", 4, local("tinta")))

            // 10 · Descanso
            add(item(10, "Dormir",    "😴", 0, local("dormir")));    add(item(10, "Descansar","🛋️", 1, local("descansar")))
            add(item(10, "Silêncio",  "🤫", 2, local("silencio")))
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

    /** URI de drawable local — nunca faz request de rede. */
    private fun local(name: String): String =
        "android.resource://com.count.iautista/drawable/pict_$name"

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
