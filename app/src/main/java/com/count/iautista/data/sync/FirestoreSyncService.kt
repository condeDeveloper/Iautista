package com.count.iautista.data.sync

import com.count.iautista.data.local.dao.ChildProfileDao
import com.count.iautista.data.local.dao.CommunicationItemDao
import com.count.iautista.data.local.dao.PhraseHistoryDao
import com.count.iautista.data.local.dao.RoutineItemDao
import com.count.iautista.data.local.entity.ChildProfileEntity
import com.count.iautista.data.local.entity.CommunicationItemEntity
import com.count.iautista.data.local.entity.PhraseHistoryEntity
import com.count.iautista.data.local.entity.RoutineItemEntity
import com.count.iautista.data.preferences.UserPreferencesDataStore
import com.count.iautista.domain.model.AppMode
import com.count.iautista.domain.model.AppTheme
import com.count.iautista.domain.model.ButtonSize
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Serviço de sincronização offline-first com Firestore.
 *
 * Estrutura no Firestore:
 *   users/{uid}/profile        → perfil da criança
 *   users/{uid}/preferences    → preferências do app
 *   users/{uid}/customItems/   → subcoleção de itens customizados
 *   users/{uid}/routineItems/  → subcoleção de itens de rotina
 *   users/{uid}/phraseHistory/ → subcoleção do histórico (últimos 100)
 *
 * Regras de segurança a publicar no Console:
 *   rules_version = '2';
 *   service cloud.firestore {
 *     match /databases/{database}/documents {
 *       match /users/{uid}/{document=**} {
 *         allow read, write: if request.auth != null && request.auth.uid == uid;
 *       }
 *     }
 *   }
 */
@Singleton
class FirestoreSyncService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val itemDao: CommunicationItemDao,
    private val routineDao: RoutineItemDao,
    private val profileDao: ChildProfileDao,
    private val historyDao: PhraseHistoryDao,
    private val prefsDataStore: UserPreferencesDataStore,
) {
    // ── Raiz do usuário ──────────────────────────────────────────────────────

    private fun userRoot(uid: String) = firestore.collection("users").document(uid)
    private fun customItems(uid: String) = userRoot(uid).collection("customItems")
    private fun routineItems(uid: String) = userRoot(uid).collection("routineItems")
    private fun phraseHistory(uid: String) = userRoot(uid).collection("phraseHistory")

    // ── Push (local → nuvem) ─────────────────────────────────────────────────

    /**
     * Envia todos os dados locais para o Firestore.
     * Chamado antes do logout e quando o usuário solicita backup manual.
     */
    suspend fun pushAll(uid: String) {
        pushProfile(uid)
        pushPreferences(uid)
        pushCustomItems(uid)
        pushRoutineItems(uid)
        pushPhraseHistory(uid)
    }

    private suspend fun pushProfile(uid: String) {
        val profile = profileDao.getProfileOnce() ?: return
        userRoot(uid).collection("profile").document("data")
            .set(profile.toMap(), SetOptions.merge())
            .await()
    }

    private suspend fun pushPreferences(uid: String) {
        val prefs = prefsDataStore.preferences
        val snapshot = prefs.first()
        userRoot(uid).collection("preferences").document("data")
            .set(
                mapOf(
                    "buttonSize" to snapshot.buttonSize.name,
                    "appTheme"   to snapshot.appTheme.name,
                    "ttsEnabled" to snapshot.ttsEnabled,
                    "ttsRate"    to snapshot.ttsRate,
                    "appMode"    to snapshot.appMode.name,
                ),
                SetOptions.merge(),
            )
            .await()
    }

    private suspend fun pushCustomItems(uid: String) {
        val items = itemDao.getAllCustomItemsOnce()
        val batch = firestore.batch()
        items.forEach { item ->
            val doc = customItems(uid).document(item.id.toString())
            batch.set(doc, item.toMap(), SetOptions.merge())
        }
        batch.commit().await()
    }

    private suspend fun pushRoutineItems(uid: String) {
        val items = routineDao.getAllOnce()
        val batch = firestore.batch()
        items.forEach { item ->
            val doc = routineItems(uid).document(item.id.toString())
            batch.set(doc, item.toMap(), SetOptions.merge())
        }
        batch.commit().await()
    }

    private suspend fun pushPhraseHistory(uid: String) {
        val phrases = historyDao.getRecentOnce(100)
        val batch = firestore.batch()
        phrases.forEach { phrase ->
            val doc = phraseHistory(uid).document(phrase.id.toString())
            batch.set(doc, phrase.toMap(), SetOptions.merge())
        }
        batch.commit().await()
    }

    // ── Pull (nuvem → local) ─────────────────────────────────────────────────

    /**
     * Restaura dados da nuvem para o dispositivo.
     * Chamado após login bem-sucedido.
     * Estratégia: merge — não apaga dados locais, apenas adiciona/atualiza.
     */
    suspend fun pullAll(uid: String) {
        pullProfile(uid)
        pullPreferences(uid)
        pullCustomItems(uid)
        pullRoutineItems(uid)
        pullPhraseHistory(uid)
    }

    private suspend fun pullProfile(uid: String) {
        val doc = userRoot(uid).collection("profile").document("data").get().await()
        if (!doc.exists()) return
        val entity = doc.data?.toChildProfileEntity() ?: return
        profileDao.deleteAll()
        profileDao.insert(entity)
    }

    private suspend fun pullPreferences(uid: String) {
        val doc = userRoot(uid).collection("preferences").document("data").get().await()
        if (!doc.exists()) return
        val data = doc.data ?: return

        (data["buttonSize"] as? String)
            ?.let { runCatching { ButtonSize.valueOf(it) }.getOrNull() }
            ?.let { prefsDataStore.setButtonSize(it) }

        (data["appTheme"] as? String)
            ?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() }
            ?.let { prefsDataStore.setAppTheme(it) }

        (data["ttsEnabled"] as? Boolean)?.let { prefsDataStore.setTtsEnabled(it) }

        (data["ttsRate"] as? Double)?.let { prefsDataStore.setTtsRate(it.toFloat()) }

        (data["appMode"] as? String)
            ?.let { runCatching { AppMode.valueOf(it) }.getOrNull() }
            ?.let { prefsDataStore.setAppMode(it) }
    }

    private suspend fun pullCustomItems(uid: String) {
        val docs = customItems(uid).get().await()
        val items = docs.documents.mapNotNull { it.data?.toCommunicationItemEntity() }
        if (items.isNotEmpty()) itemDao.insertAll(items)
    }

    private suspend fun pullRoutineItems(uid: String) {
        val docs = routineItems(uid).get().await()
        val items = docs.documents.mapNotNull { it.data?.toRoutineItemEntity() }
        if (items.isNotEmpty()) routineDao.insertAll(items)
    }

    private suspend fun pullPhraseHistory(uid: String) {
        val docs = phraseHistory(uid).get().await()
        val phrases = docs.documents.mapNotNull { it.data?.toPhraseHistoryEntity() }
        if (phrases.isNotEmpty()) historyDao.insertAll(phrases)
    }

    // ── Mappers entity → Map ─────────────────────────────────────────────────

    private fun ChildProfileEntity.toMap() = mapOf(
        "localId"   to id,
        "name"      to name,
        "photoUri"  to photoUri,
        "createdAt" to createdAt,
    )

    private fun CommunicationItemEntity.toMap() = mapOf(
        "localId"    to id,
        "categoryId" to categoryId,
        "text"       to text,
        "emoji"      to emoji,
        "imageUri"   to imageUri,
        "audioUri"   to audioUri,
        "isFavorite" to isFavorite,
        "isDefault"  to false,
        "order"      to order,
        "usageCount" to usageCount,
        "createdAt"  to createdAt,
    )

    private fun RoutineItemEntity.toMap() = mapOf(
        "localId"       to id,
        "text"          to text,
        "emoji"         to emoji,
        "imageUri"      to imageUri,
        "status"        to status,
        "order"         to order,
        "suggestedHour" to suggestedHour,
        "completedAt"   to completedAt,
    )

    private fun PhraseHistoryEntity.toMap() = mapOf(
        "localId"    to id,
        "phraseText" to phraseText,
        "itemIds"    to itemIds,
        "createdAt"  to createdAt,
        "hourOfDay"  to hourOfDay,
        "appMode"    to appMode,
    )

    // ── Mappers Map → entity ─────────────────────────────────────────────────

    private fun Map<String, Any?>.toChildProfileEntity() = ChildProfileEntity(
        id        = (get("localId") as? Long) ?: 0L,
        name      = get("name") as? String ?: "",
        photoUri  = get("photoUri") as? String,
        createdAt = (get("createdAt") as? Long) ?: System.currentTimeMillis(),
    )

    private fun Map<String, Any?>.toCommunicationItemEntity() = CommunicationItemEntity(
        id          = (get("localId") as? Long) ?: 0L,
        categoryId  = (get("categoryId") as? Long) ?: 0L,
        text        = get("text") as? String ?: "",
        emoji       = get("emoji") as? String ?: "",
        imageUri    = get("imageUri") as? String,
        audioUri    = get("audioUri") as? String,
        isFavorite  = get("isFavorite") as? Boolean ?: false,
        isDefault   = false,
        order       = ((get("order") as? Long) ?: 0L).toInt(),
        usageCount  = ((get("usageCount") as? Long) ?: 0L).toInt(),
        imageRes    = null,
        createdAt   = (get("createdAt") as? Long) ?: System.currentTimeMillis(),
    )

    private fun Map<String, Any?>.toRoutineItemEntity() = RoutineItemEntity(
        id           = (get("localId") as? Long) ?: 0L,
        text         = get("text") as? String ?: "",
        emoji        = get("emoji") as? String ?: "",
        imageUri     = get("imageUri") as? String,
        status       = get("status") as? String ?: "LATER",
        order        = ((get("order") as? Long) ?: 0L).toInt(),
        suggestedHour = (get("suggestedHour") as? Long)?.toInt(),
        completedAt  = get("completedAt") as? Long,
    )

    @Suppress("UNCHECKED_CAST")
    private fun Map<String, Any?>.toPhraseHistoryEntity() = PhraseHistoryEntity(
        id         = (get("localId") as? Long) ?: 0L,
        phraseText = get("phraseText") as? String ?: "",
        itemIds    = (get("itemIds") as? List<Long>) ?: emptyList(),
        createdAt  = (get("createdAt") as? Long) ?: System.currentTimeMillis(),
        hourOfDay  = ((get("hourOfDay") as? Long) ?: 0L).toInt(),
        appMode    = get("appMode") as? String ?: "CASA",
    )
}
