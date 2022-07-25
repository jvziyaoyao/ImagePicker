package com.origeek.imagePicker.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.MaterialTheme
import androidx.core.view.WindowCompat
import com.origeek.imagePicker.config.ImagePickerConfig
import com.origeek.imagePicker.config.PICKER_ARGUMENT_NAME
import com.origeek.imagePicker.config.PICKER_RESULT_NAME
import com.origeek.imagePicker.ui.PickerBody
import com.origeek.imagePicker.vm.PickerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import java.util.stream.Collectors


class PickerActivity : ComponentActivity(), CoroutineScope by MainScope() {

    private val pickerViewModel by viewModels<PickerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val imagePickerConfig = intent.getParcelableExtra(PICKER_ARGUMENT_NAME) ?: ImagePickerConfig()
        pickerViewModel.pickerConfig = imagePickerConfig

        setContent {
            MaterialTheme {
                PickerBody(
                    viewModel = pickerViewModel,
                    config = imagePickerConfig,
                    onBack = {
                        finish()
                    },
                    commit = {
                        commitResult()
                    },
                )
            }
        }
    }

    private fun commitResult() {
        val paths = pickerViewModel.checkList.stream().map { it.path }.collect(Collectors.toList()) as ArrayList
        val intent = Intent()
        intent.putStringArrayListExtra(PICKER_RESULT_NAME, paths)
        intent.putExtra(PICKER_RESULT_NAME, paths)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

}