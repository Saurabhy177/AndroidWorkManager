package com.example.androidworkmanager.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.androidworkmanager.*
import com.example.androidworkmanager.utils.*

/**
 * Worker class is where we define the work to be performed in background.
 * */
private const val TAG = "BlurWorker"
class BlurWorker(context: Context, params: WorkerParameters): Worker(context, params) {

    /**
     * Override doWork() to implement the work we want done.
     * Here, we're creating a blurred bitmap based on the image being passed in.
     * */
    override fun doWork(): Result {
        val appContext = applicationContext
        val resourceUri = inputData.getString(KEY_IMAGE_URI)

        // Makes a notification when the work starts and slows down the work so that
        // it's easier to see each WorkRequest start, even on emulated devices
        makeStatusNotification("Blurring image", appContext)
        sleep()

        return try {
            /*val picture = BitmapFactory.decodeResource(
                appContext.resources,
                R.drawable.android_cupcake
            )*/
            if (TextUtils.isEmpty(resourceUri)) {
                Log.e(TAG, "Invalid input uri")
                throw IllegalArgumentException("Invalid input uri")
            }

            val resolver = appContext.contentResolver
            val picture = BitmapFactory
                .decodeStream(resolver.openInputStream(Uri.parse(resourceUri)))
            val output = blurBitmap(picture, appContext)

            // Write bitmap to a temp file
            val outputUri = writeBitmapToFile(appContext, output)
            makeStatusNotification("Output is $outputUri", appContext)

            val outputData = workDataOf(KEY_IMAGE_URI to outputUri.toString())
            Result.success(outputData)
        } catch (throwable: Throwable) {
            Log.d(TAG, "Error applying blur")
            throwable.printStackTrace()
            Result.failure()
        }
    }
}