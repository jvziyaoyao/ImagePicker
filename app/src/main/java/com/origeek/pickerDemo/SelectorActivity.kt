package com.origeek.pickerDemo

import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.origeek.imagePicker.config.ImagePickerConfig
import com.origeek.imagePicker.config.registerImagePicker
import com.origeek.pickerDemo.base.BaseActivity
import com.origeek.pickerDemo.ui.theme.ImageViewerTheme

class SelectorActivity : BaseActivity() {

    private val launcher = registerImagePicker { paths ->
        paths?.forEach {
            Log.i("TAG", "registerImagePicker path $it")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
            ImageViewerTheme {
                SelectorBody {
                    launcher.launch(
                        ImagePickerConfig(
                            limit = 2,
                            navTitle = "好家伙",
//                            filterMineType = listOf("image/jpeg", "image/png"),
                        )
                    )
                }
            }
        }
    }

}

@Composable
fun SelectorBody(
    goPicker: () -> Unit,
) {
    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (add) = createRefs()
        FloatingActionButton(
            onClick = {
                goPicker()
            },
            backgroundColor = MaterialTheme.colors.primary,
            modifier = Modifier.constrainAs(add) {
                end.linkTo(parent.end, 18.dp)
                linkTo(parent.top, parent.bottom, 0.dp, 0.dp, 0.72f)
            }
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
        }
    }
}