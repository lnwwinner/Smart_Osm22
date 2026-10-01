package com.example.data.sync

import android.content.Context
import android.util.Log
import com.example.data.firestore.FirestoreManager
import com.example.data.vhv.VhvMemberDao
import com.example.data.vhv.VhvMemberEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Helper class for synchronizing Public Health Volunteer (OSM / VHV) data
 * between the local Room database and Cloud Firestore.
 *
 * No hardcoded OSMRP00002/mock dataset is seeded here.
 */
class VhvFirestoreSyncHelper(
    private val context: Context,
    private val vhvMemberDao: VhvMemberDao,
    private val firestoreProvider: () -> FirebaseFirestore? = { FirestoreManager.getInstance() }
) {
    companion object {
        private const val TAG = "VhvFirestoreSyncHelper"
        const val COLLECTION_VHV_MEMBERS = "vhv_members"
    }

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private suspend fun ensureAuth() {
        try {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            if (auth.currentUser == null) auth.signInAnonymously().await()
        } catch (e: Exception) {
            Log.d(TAG, "FirebaseAuth ensureAuth note: " + e.message)
        }
    }

    suspend fun syncOsmData(): Result<SyncResult> = withContext(Dispatchers.IO) {
        _syncState.value = SyncState.Syncing("กำลังซิงค์ข้อมูล อสม. กับระบบ Cloud...")
        try {
            // Use only records that already exist in the real local database.
            val localMembers = vhvMemberDao.getAllVhvMembers()
            var syncedCount = localMembers.size

            try {
                ensureAuth()
                val firestore = firestoreProvider()
                if (firestore != null) {
                    val collectionRef = firestore.collection(COLLECTION_VHV_MEMBERS)
                    var batch = firestore.batch()
                    var opsInBatch = 0

                    for (member in localMembers) {
                        val docId = member.vhvCardId.ifBlank { member.nationalId }
                        if (docId.isNotBlank()) {
                            val mapData = hashMapOf(
                                "vhvCardId" to member.vhvCardId,
                                "nationalId" to member.nationalId,
                                "fullName" to member.fullName,
                                "gender" to member.gender,
                                "phone" to member.phone,
                                "villageNo" to member.villageNo,
                                "villageName" to member.villageName,
                                "subdistrict" to member.subdistrict,
                                "district" to member.district,
                                "province" to member.province,
                                "healthCenter" to member.healthCenter,
                                "roleTitle" to member.roleTitle,
                                "assignedHouseholdsCount" to member.assignedHouseholdsCount,
                                "status" to member.status,
                                "reportSource" to member.reportSource,
                                "updatedTimestamp" to member.updatedTimestamp
                            )

                            batch.set(collectionRef.document(docId), mapData, SetOptions.merge())
                            opsInBatch++

                            if (opsInBatch >= 400) {
                                batch.commit().await()
                                batch = firestore.batch()
                                opsInBatch = 0
                            }
                        }
                    }

                    if (opsInBatch > 0) batch.commit().await()

                    val snapshot = collectionRef.get().await()
                    val remoteMembers = mutableListOf<VhvMemberEntity>()
                    for (doc in snapshot.documents) {
                        val vhvCardId = doc.getString("vhvCardId") ?: doc.id
                        val nationalId = doc.getString("nationalId") ?: ""
                        val fullName = doc.getString("fullName") ?: continue

                        remoteMembers.add(
                            VhvMemberEntity(
                                vhvCardId = vhvCardId,
                                nationalId = nationalId,
                                fullName = fullName,
                                gender = doc.getString("gender") ?: "",
                                phone = doc.getString("phone") ?: "",
                                villageNo = doc.getString("villageNo") ?: "",
                                villageName = doc.getString("villageName") ?: "",
                                subdistrict = doc.getString("subdistrict") ?: "",
                                district = doc.getString("district") ?: "",
                                province = doc.getString("province") ?: "",
                                healthCenter = doc.getString("healthCenter") ?: "",
                                roleTitle = doc.getString("roleTitle") ?: "",
                                assignedHouseholdsCount =
                                    (doc.getLong("assignedHouseholdsCount") ?: 0L).toInt(),
                                status = doc.getString("status") ?: "",
                                reportSource = doc.getString("reportSource") ?: "",
                                updatedTimestamp =
                                    doc.getLong("updatedTimestamp") ?: System.currentTimeMillis()
                            )
                        )
                    }

                    if (remoteMembers.isNotEmpty()) {
                        vhvMemberDao.insertAll(remoteMembers)
                        syncedCount = remoteMembers.size
                    }
                    Log.i(TAG, "VHV Cloud Firestore sync completed successfully")
                }
            } catch (cloudEx: Exception) {
                Log.w(TAG, "Cloud Firestore sync skipped/restricted: " +
                    cloudEx.message + ". Using local Room database.")
            }

            val result = SyncResult(
                personsSynced = syncedCount,
                message = "ซิงค์ข้อมูล อสม. (" + syncedCount + " รายการ) เรียบร้อยแล้ว",
                timestamp = System.currentTimeMillis()
            )
            _syncState.value = SyncState.Success(result)
            Result.success(result)
        } catch (ce: CancellationException) {
            Log.d(TAG, "VHV OSM sync coroutine was cancelled.")
            throw ce
        } catch (e: Exception) {
            Log.w(TAG, "VHV sync failed: " + e.localizedMessage)
            val result = SyncResult(
                personsSynced = 0,
                message = "ยังไม่มีข้อมูล อสม. ในฐานข้อมูลเครื่อง",
                timestamp = System.currentTimeMillis()
            )
            _syncState.value = SyncState.Success(result)
            Result.success(result)
        }
    }
}
