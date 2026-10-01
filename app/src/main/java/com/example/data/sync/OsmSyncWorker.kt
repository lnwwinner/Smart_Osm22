package com.example.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.AppDatabase
import com.example.data.firestore.FirestoreManager
import kotlinx.coroutines.CancellationException

/**
 * Background WorkManager worker responsible for syncing Public Health Volunteer (OSM)
 * data between the local Room database and configured cloud/API sources.
 *
 * This worker deliberately does not seed hardcoded OSMRP00002/mock data.
 */
class OsmSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "OsmSyncWorker"
        const val WORK_NAME = "OsmPeriodicSyncWork"
        const val ONE_TIME_WORK_NAME = "OsmOneTimeSyncWork"
    }

    override suspend fun doWork(): Result {
        return try {
            if (isStopped) return Result.success()

            Log.d(TAG, "Starting background sync worker for OSM (Public Health Volunteer) data...")
            val appContext = applicationContext

            FirestoreManager.initialize(appContext)
            val firestore = FirestoreManager.getInstance()

            val db = AppDatabase.getInstance(appContext)
            val vhvMemberDao = db.vhvMemberDao()

            val syncHelper = VhvFirestoreSyncHelper(
                context = appContext,
                vhvMemberDao = vhvMemberDao,
                firestoreProvider = { firestore }
            )

            val syncResult = syncHelper.syncOsmData()
            if (syncResult.isSuccess) {
                val resultData = syncResult.getOrNull()
                Log.i(TAG, "OSM background sync finished successfully: " +
                    (resultData?.personsSynced ?: 0) + " records processed.")
                Result.success()
            } else {
                Log.w(TAG, "OSM background sync non-critical failure: " +
                    (syncResult.exceptionOrNull()?.message ?: "unknown"))
                Result.retry()
            }
        } catch (ce: CancellationException) {
            Log.d(TAG, "OsmSyncWorker cancelled normally.")
            throw ce
        } catch (e: Exception) {
            Log.e(TAG, "Exception in OsmSyncWorker execution", e)
            Result.retry()
        }
    }
}
