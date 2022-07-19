# ImagePicker
🌆 ImagePicker for Jetpack Compose.

中文介绍 | [English](/README_en.md)

一款基于Jekpack Compose开发的图片选择库

[![](https://www.jitpack.io/v/jvziyaoyao/ImagePicker.svg)](https://www.jitpack.io/#jvziyaoyao/ImagePicker)

🚀 特性
--------
- 基于Jetpack Compose开发；
- 支持超大图片的加载；

🍟 预览
--------
<img src="doc/preview_02.gif" height="496" width="240"></img>
<img src="doc/preview_01.gif" height="496" width="240"></img>

🎯 引入
--------
在`settings.gradle`增加jitpack的地址
```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
在`build.gradle`增加依赖的引入
```gradle
// 从releases里面选一个版本
implementation 'com.github.jvziyaoyao:ImagePicker:VERSION'
```

🍤 示例
--------
### 👋 示例代码请参考[sample](https://github.com/jvziyaoyao/ImagePicker/tree/main/sample)

在你的`Activity`中声明一个`luancher`
```kotlin
val launcher = registerImagePicker { paths -> }
```
打开图片选择界面进行选择
```kotlin
launcher.launch()
```
设置选择器的参数
```kotlin
val config = ImagePickerConfig(
    limit = 9,
    navTitle = "好家伙",
)
launcher.launch(config)
```
🚲 可选参数
--------
| 名称 | 描述 | 默认值 |
| --- | --- | :---: |
| `filterMineType` | 需要过滤的mime-type | `emptyList()` |
| `limit` | 限制图片选择的数量 | `NO_LIMIT` |
| `navTitle` | picker页面标题文字 | `""` |
| `backgroundColor` | 背景颜色 | `Color(0xFFF4F4F4)` |
| `backgroundColorDark` | 深色背景颜色 | `Color(0xFF000000)` |
| `checkColorDefault` | 默认选中颜色 | `Color(0xCCFFFFFF)` |
| `loadingColor` | 加载标识的颜色 | `Color(0xCCFFFFFF)` |
| `surfaceColor` | 表面颜色 | `Color(0xFFFFFFFF)` |
| `previewSurfaceColor` | 预览表面颜色 | `Color(0xCCFFFFFF)` |
| `checkMaskerColor` | 方格中选中时的遮罩颜色 | `Color(0x8F000000)` |
| `uncheckMaskerColor` | 方格中未选中的遮罩颜色 | `Color(0x0F000000)` |
| `tabCheckColor` | tab选中颜色 | `Color(0x66000000)` |
| `tabImageCheckBorderColor` | tab图片方框颜色 | `Color(0x33000000)` |
| `tabImageMaskerColor` | tab图片选中遮罩颜色 | `Color(0x14000000)` |
| `tabImageMaskerUncheckedColor` | tab图片未选中遮罩颜色 | `Color(0x99CCCCCC)` |

🕵️‍♀️ 开源许可
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