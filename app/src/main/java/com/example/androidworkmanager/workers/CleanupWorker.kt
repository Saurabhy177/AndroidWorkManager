package com.example.androidworkmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.androidworkmanager.OUTPUT_PATH
import java.io.File
import java.lang.Exception
import com.example.androidworkmanager.utils.*

/**
 * Cleans up temporary files generated during blurring process
 */
private const val TAG = "CleanupWorker"
class CleanupWorker(context: Context, params: WorkerParameters): Worker(context, params) {

    override fun doWork(): Result {
        // Makes a notification when the work starts and slows down the work so that
        // it's easier to see each WorkRequest start, even on emulated devices
        makeStatusNotification("Cleaning up old temporary files", applicationContext)
        sleep()

        return try {
            val outputDirectory = File(applicationContext.filesDir, OUTPUT_PATH)
            if (outputDirectory.exists()) {
                val entries = outputDirectory.listFiles()
                entries?.forEach { entry ->
                    val name = entry.name
                    if (!name.isNullOrEmpty() && name.endsWith(".png")) {
                        val deleted = entry.delete()
                        Log.d(TAG, "Deleted $name - $deleted")
                    }
                }
            }
            Result.success()
        } catch (exception: Exception) {
            exception.printStackTrace()
            Result.failure()
        }
    }
}