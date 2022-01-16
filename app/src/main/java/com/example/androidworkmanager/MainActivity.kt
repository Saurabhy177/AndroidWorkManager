package com.example.androidworkmanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import com.example.androidworkmanager.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    private val viewModel: BlurViewModel by viewModels {
        BlurViewModel.BlurViewModelFactory(application)
    }

    private lateinit var binding: ActivityMainBinding

    private val blurLevel: Int
        get() =
            when (binding.rgBlur.checkedRadioButtonId) {
                R.id.radioBlurLv1 -> 1
                R.id.radioBlurLv2 -> 2
                R.id.radioBlurLv3 -> 3
                else -> 1
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Observe work status, added in onCreate()
        viewModel.outputWorkInfo.observe(this, workInfoObserver())

        binding.btnGo.setOnClickListener {
            viewModel.applyBlur(blurLevel)
        }

        binding.btnCancel.setOnClickListener {
            viewModel.cancelWork()
        }

        binding.btnSeeFile.setOnClickListener {
            viewModel.outputUri?.let { currentUri ->
                val actionView = Intent(Intent.ACTION_VIEW, currentUri)
                actionView.resolveActivity(packageManager)?.run {
                    startActivity(actionView)
                }
            }
        }
    }

    // Define the observer function
    private fun workInfoObserver(): Observer<List<WorkInfo>> {
        return Observer { listOfWorkInfo ->
            // If there are no matching work info, do nothing
            if (listOfWorkInfo.isNullOrEmpty()) {
                return@Observer
            }

            // Every continuation has only one worker tagged TAG_OUTPUT
            val workInfo = listOfWorkInfo[0]

            if (workInfo.state.isFinished) {
                showWorkFinished()

                // If there is an output file show "See File" button
                val outputImageUri = workInfo.outputData.getString(KEY_IMAGE_URI)
                if (!outputImageUri.isNullOrEmpty()) {
                    viewModel.setOutputUri(outputImageUri)
                    binding.btnSeeFile.visibility = View.VISIBLE
                }
            } else {
                showWorkInProgress()
            }
        }
    }

    /**
     * Shows and hides views for when the Activity is processing an image
     */
    private fun showWorkInProgress() {
        with(binding) {
            progressBar.visibility = View.VISIBLE
            btnCancel.visibility = View.VISIBLE
            btnGo.visibility = View.GONE
            btnSeeFile.visibility = View.GONE
        }
    }

    /**
     * Shows and hides views for when the Activity is done processing an image
     */
    private fun showWorkFinished() {
        with(binding) {
            progressBar.visibility = View.GONE
            btnCancel.visibility = View.GONE
            btnGo.visibility = View.VISIBLE
        }
    }
}