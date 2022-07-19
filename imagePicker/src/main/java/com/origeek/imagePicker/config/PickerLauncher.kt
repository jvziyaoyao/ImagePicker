package com.origeek.imagePicker.config

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import com.origeek.imagePicker.activity.PickerActivity

const val PICKER_ARGUMENT_NAME = "PICKER_ARGUMENT_NAME"
const val PICKER_RESULT_NAME = "PICKER_RESULT_NAME"

class PickerContract: ActivityResultContract<ImagePickerConfig?,List<String>?>() {
    override fun createIntent(context: Context, input: ImagePickerConfig?): Intent {
        return Intent(context,PickerActivity::class.java).apply {
            putExtra(PICKER_ARGUMENT_NAME,input)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<String>? {
        val result = intent?.getStringArrayListExtra(PICKER_RESULT_NAME)
        return if (resultCode == Activity.RESULT_OK) {
            result
        } else {
            null
        }
    }
}

fun ComponentActivity.registerImagePicker(callback: (List<String>?) -> Unit): ActivityResultLauncher<ImagePickerConfig?> {
    return registerForActivityResult(PickerContract()) {
        callback(it)
    }
}

fun ActivityResultLauncher<ImagePickerConfig?>.launch() {
    this.launch(null)
}


