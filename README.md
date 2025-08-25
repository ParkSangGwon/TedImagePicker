# TedImagePicker [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-TedImagePicker-green.svg?style=flat)](https://android-arsenal.com/details/1/7697)

TedImagePicker is **simple/beautiful/smart** image picker

- Support Image/Video/Image&Video
- Support Single/Multi select with **drag selection**
- Support more configuration option

|       Image Select        |    Select Album    |          Scroller           |
| :-----------------------: | :----------------: | :-------------------------: |
| ![](art/multi_select.png) | ![](art/album.png) | ![](art/scroll_handler.png) |

</br></br>

## Demo

![](art/full.gif)

|       Image Select        |    Select Album    |          Scroller           |
| :-----------------------: | :----------------: | :-------------------------: |
| ![](art/multi_select.gif) | ![](art/album.gif) | ![](art/scroll_handler.gif) |

</br></br>

## Setup

### Gradle
[![Maven Central](https://img.shields.io/maven-central/v/io.github.parksanggwon/tedimagepicker.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.parksanggwon%22%20AND%20a:%tedimagepicker%22)

```gradle

repositories {
  google()
  mavenCentral()
}

dependencies {
    implementation 'io.github.parksanggwon:tedimagepicker:x.y.z'
    //implementation 'io.github.parksanggwon:tedimagepicker:1.7.0'
    // only lowercase!!
}

```

If you think this library is useful, please press star button at upside. </br>
<img src="https://phaser.io/content/news/2015/09/10000-stars.png" width="200">

</br></br>

## How to use

### 1.Enable databinding

- TedImagePicker use databinding
- Set enable databinding in your app `build.gradle`

```
dataBinding {
    enabled = true
}

or

buildFeatures {
    dataBinding = true
}
```

### 2.Start TedImagePicker/TedRxImagePicker

- TedImagePicker support `Listener` and `RxJava`style

#### Listener

##### Single image

- Kotlin

```kotlin
TedImagePicker.with(this)
    .start { uri -> showSingleImage(uri) }
```

- Java

```java
TedImagePicker.with(this)
        .start(new OnSelectedListener() {
            @Override
            public void onSelected(@NotNull Uri uri) {
                showSingleImage(uri);
            }
        });
TedImagePicker.with(this)
        .start(uri -> {
            showSingleImage(uri);
        });
```

##### Multi image

- Kotlin

```kotlin
TedImagePicker.with(this)
    .startMultiImage { uriList -> showMultiImage(uriList) }
```

- Java

```java
TedImagePicker.with(this)
        .startMultiImage(new OnMultiSelectedListener() {
            @Override
            public void onSelected(@NotNull List<? extends Uri> uriList) {
                showMultiImage(uriList);
            }
        });
TedImagePicker.with(this)
        .startMultiImage(uriList -> {
            showMultiImage(uriList);
        });
```

<br/>

#### RxJava

##### Single image

```kotlin
TedRxImagePicker.with(this)
    .start()
    .subscribe({ uri ->
    }, Throwable::printStackTrace)
```

##### Multi image

```kotlin
TedRxImagePicker.with(this)
    .startMultiImage()
    .subscribe({ uriList ->
    }, Throwable::printStackTrace)
```

</br></br>

## Customize

- You can customize what you want

### Function

#### Common

|      Function        |    Description    | 
| ----------------------- | ---------------- |
| `mediaType(MediaType)` | MediaType.IMAGE / MediaType.VIDEO / MediaType.IMAGE_AND_VIDEO |
| `cameraTileBackground(R.color.xxx)`| camera Tile Background Color |
| `cameraTileImage(R.drawable.xxx)` | camera tile image |
| `showCameraTile(Boolean)` default `true` | show camera tile |
| `scrollIndicatorDateFormat(String) (default: YYYY.MM)` | Format of date on scroll indicator |
| `showTitle(Boolean)(default: true)` | Show title |
| `title(String or R.string.xxx) (default: 'Select Image','사진 선택')` | title |
| `backButton(R.drawable.xxx)` | back button |
| `zoomIndicator(Boolean) (default: true)`| zoom indicator |
| `image()` | image |
| `video()` | video |
| `imageAndVideo()` | image and video |
| `imageCountTextFormat(String) (default: %s)`: `%s장`,  `Count: %s`| image count text format |
| `savedDirectoryName(String)` | saved directory name from take picture using camera |
| `startAnimation(Int, Int)` | start animation |
| `finishAnimation(Int, Int)` | finish animation |
| `errorListener()` | error listener for error |
| `cancelListener()` | cancel listener |




#### Multi Select

**🎯 Enhanced Selection Methods:**
- **Tap Selection**: Traditional single-tap to select/deselect images
- **Drag Selection**: **NEW!** Drag across multiple images to select them at once
- **Mixed Mode**: Both selection methods work together seamlessly

> **Note**: Drag selection is automatically enabled in multi-select mode and works alongside traditional tap selection.

|      Method        |    Description    | 
| ----------------------- | ---------------- |
| `selectedUri(List<Uri>)` | selected uri |
| `buttonGravity(ButtonGravity)` | You can change `done` button location top or bottom |
| `buttonText(String or R.string.xxx) (default: 'Done','완료')` | you can change `done` button text |
| `buttonBackground(R.drawable.xxx) (default: Blue Background)` | you can change `done` button background color |
| `buttonTextColor(R.color.xxx) (default: white)` | `done` button text color |
| `buttonDrawableOnly(R.drawable.xxx) (default: false)` | If you want show drawable button without text, use this method |
| `max(Int, String or R.string.xxx)` | **max content** should picked from user device |
| `min(Int, String or R.string.xxx)` | **min content** should picked from user device |
| `drawerAlbum() / dropDownAlbum() (default: Drawer)`| You can choice Drawer or DropDown album style |

  </br></br>

#### UI
- Change picker primary color
: override color name in your colors.xml
```xml
<color name="ted_image_picker_primary">#your_color_code</color>
<color name="ted_image_picker_primary_pressed">#your_color_code</color>
```

- Change textAppearance style
: override text style in your styles.xml

style list
- TextAppearance.TedImagePicker.Subhead
- TextAppearance.TedImagePicker.Body1
- TextAppearance.TedImagePicker.Caption

```xml
<style name="TextAppearance.MyApp.Body1" parent="@style/TextAppearance.AppCompat.Body1">
    <item name="android:textSize">...</item>
    <item name="android:fontFamily">...</item>
</style>
<style name="TextAppearance.TedImagePicker.Body1" parent="@style/TextAppearance.MyApp.Body1" />
```

## FAQ
### - Do not need to check permissions?

- Yes, `TedImagePicker` automatically check permission.
  : `TedImagePicker` use [TedPermission](https://github.com/ParkSangGwon/TedPermission)
- But If you need You can check permission before start `TedImagePicker`.

### - java.lang.NoClassDefFoundError: Failed resolution of: Landroidx/databinding/DataBinderMapperImpl;

- You have to enable databinding
- Read [this](https://github.com/ParkSangGwon/TedImagePicker/blob/master/README.md#1enable-databinding)

#### - `Duplicate class android.support.v4.xxx`: Execution failed for task ':app:checkDebugDuplicateClasses'
- Add `android.enableJetifier=true` in your gradle.properties file

### - I'm using targetSdkVersion less than 33 and it doesn't work
- You have to use `targetSdkVersion 33`
- If you use targetSdkVersion 32, you can not support SDK 33(Android OS 13) device.
- these day, there are so many android os 13 device.
- So you have to use targetSdkVersion 33

### - I'm using targetSdkVersion less than 34 and it doesn't work
- Starting with targetSdkVersion 34, you need to control the permission READ_MEDIA_VISUAL_USER_SELECTED.
: [Grant partial access to photos and videos](https://developer.android.com/about/versions/14/changes/partial-photo-video-access)
- If you still keep targetSdkVersion set to 33 to not control the READ_MEDIA_VISUAL_USER_SELECTED permission, you need to add the code below to your Manifest file.
```xml
<uses-permission
    android:name="android.permission.READ_MEDIA_VISUAL_USER_SELECTED"
    tools:node="remove" />
```

</br></br>

## License

````code
Copyright 2019 Ted Park

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.```
````
