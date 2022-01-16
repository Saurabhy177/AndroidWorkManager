package com.example.androidworkmanager.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.androidworkmanager.*
import com.example.androidworkmanager.utils.*
import java.io.FileNotFoundException

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
            // The worker accepts inputs and produces output with the Data object (inputData)
            // associated with key-value pairs.
            val outputData = createBlurredBitmap(appContext, resourceUri)
            makeStatusNotification("Output is $outputData", appContext)

            // Here, we are outputting the URI of the newly blurred image.
            Result.success(outputData)
        } catch (throwable: Throwable) {
            Log.d(TAG, "Error applying blur")
            throwable.printStackTrace()

            // If there were errors, return failure.
            // In some cases, we may want to return Result.retry() to indicate that we want
            // to retry running this work at a later time.
            Result.failure()
        }
    }

    @Throws(FileNotFoundException::class, java.lang.IllegalArgumentException::class)
    private fun createBlurredBitmap(
        appContext: Context,
        resourceUri: String?
    ): Data {

//            val picture = BitmapFactory.decodeResource(
//                appContext.resources,
//                R.drawable.android_cupcake
//            )

        if (resourceUri.isNullOrEmpty()) {
            Log.e(TAG, "Invalid input uri")
            throw IllegalArgumentException("Invalid input uri")
        }

        val resolver = appContext.contentResolver

        // Create a bitmap
        val picture = BitmapFactory
            .decodeStream(resolver.openInputStream(Uri.parse(resourceUri)))

        // Blur the bitmap
        val output = blurBitmap(picture, appContext)

        // Write bitmap to a temp file
        val outputUri = writeBitmapToFile(appContext, output)

        // Return the output of the temp file
        return workDataOf(KEY_IMAGE_URI to outputUri.toString())
    }
}