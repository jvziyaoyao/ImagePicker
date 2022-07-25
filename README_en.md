# ImagePicker
🌆 ImagePicker for Jetpack Compose.

中文介绍 | [English](/README_en.md)

An ImagePicker library based on Jekpack Compose.

[![](https://www.jitpack.io/v/jvziyaoyao/ImagePicker.svg)](https://www.jitpack.io/#jvziyaoyao/ImagePicker)

🚀 Feature
--------
- Development based on Jetpack Compose;
- Support large image display;

🍟 Preview
--------
<img src="doc/preview_02.gif" height="496" width="240"></img>
<img src="doc/preview_01.gif" height="496" width="240"></img>

🎯 Install
--------
Add `jitpack` to `settings.gradle`.
```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
Add dependencies to your project's `build.gradle`.
```gradle
// Select the version you want to install.
implementation 'com.github.jvziyaoyao:ImagePicker:VERSION'
```

🍤 Samples
--------
### 👋 For example code, please refer to [sample](https://github.com/jvziyaoyao/ImagePicker/tree/main/sample).

Declare a `luancher` in your `Activity`.
```kotlin
val launcher = registerImagePicker { paths -> }
```
Launch the `imagePicker`
```kotlin
launcher.launch()
```
Set parameters for your launcher.
```kotlin
val config = ImagePickerConfig(
    limit = 9,
    navTitle = "Title",
)
launcher.launch(config)
```
🚲 Parameter
--------
| Name | Discription | Default |
| --- | --- | :---: |
| `filterMineType` | If you don't want some mime-type. | `emptyList()` |
| `limit` | Limit the number of pictures selected. | `NO_LIMIT` |
| `navTitle` | Set page title. | `""` |
| `backgroundColor` | Background color. | `Color(0xFFF4F4F4)` |
| `backgroundColorDark` | BG color on fullscreen mode. | `Color(0xFF000000)` |
| `checkColorDefault` | Default selected color. | `Color(0xCCFFFFFF)` |
| `loadingColor` | Loading circle color. | `Color(0xCCFFFFFF)` |
| `surfaceColor` | Color on surface. | `Color(0xFFFFFFFF)` |
| `previewSurfaceColor` | Color on surface when preview image. | `Color(0xCCFFFFFF)` |
| `checkMaskerColor` | Selected masker color on checked block. | `Color(0x8F000000)` |
| `uncheckMaskerColor` | Unselected masker color on checked block. | `Color(0x0F000000)` |
| `tabCheckColor` | Tab selected color. | `Color(0x66000000)` |
| `tabImageCheckBorderColor` | Tab selected border color. | `Color(0x33000000)` |
| `tabImageMaskerColor` | Tab selected image masker color. | `Color(0x14000000)` |
| `tabImageMaskerUncheckedColor` | Tab unselected image masker color. | `Color(0x99CCCCCC)` |

🕵️‍♀️ License
--------
MIT License

Copyright (c) 2022 JVZIYAOYAO

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.