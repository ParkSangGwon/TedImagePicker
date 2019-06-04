 
# TedImagePicker
`TedImagePicker` is **simple/beautiful/smart** image picker
- Support Image/Video
- Support Single/Multi select
- Support more configuration option


| Image Select                    | Select Album                     | Scroller                         |
|:------------------------------:|:---------------------------------:|:--------------------------------:|
|![](art/multi_select.png) |![](art/album.png) |![](art/scroll_handler.png)|


</br></br>
## Demo
![](art/full.gif)

| Image Select                    | Select Album                     | Scroller                         |
|:------------------------------:|:---------------------------------:|:--------------------------------:|
|![](art/multi_select.gif) |![](art/album.gif) |![](art/scroll_handler.gif)|

</br></br>
## Setup


### Gradle
[ ![Download](https://api.bintray.com/packages/tkdrnjs0912/maven/tedimagepicker/images/download.svg) ](https://bintray.com/tkdrnjs0912/maven/tedimagepicker/_latestVersion)
```gradle
dependencies {
    implementation 'gun0912.ted:tedimagepicker:x.y.z'
    //implementation 'gun0912.ted:tedimagepicker:1.0.0-alpha2'
}

```
If you think this library is useful, please press star button at upside. </br>
<img src="https://phaser.io/content/news/2015/09/10000-stars.png" width="200">



</br></br>
## How to use
- TedImagePicker support `Listener` and `RxJava`style

### Listener
- Single image
```kotlin
TedImagePicker.with(this)
    .start(object : OnSelectedListener {
        override fun onSelected(uri: Uri) {
        }
    })

```

- Multi image
```kotlin
TedImagePicker.with(this)
    .start(object : OnMultiSelectedListener {
        override fun onSelected(uriList: List<Uri>) {
        }
    })
```
<br/>

### RxJava
- Single image
```kotlin
TedRxImagePicker.with(this)
    .start()
    .subscribe({ uri ->
    }, Throwable::printStackTrace)
```
- Multi image
```kotlin
TedRxImagePicker.with(this)
    .startMultiImage()
    .subscribe({ uriList ->
    }, Throwable::printStackTrace)
```

</br></br>
## FAQ
### Do not need to check permissions?
- Yes, `TedImagePicker` automatically check permission.
: `TedImagePicker` use [TedPermission](https://github.com/ParkSangGwon/TedPermission)
- But If you need You can check permission before start `TedImagePicker`.

</br></br>
## Customize
- You can customize what you want

### Function

#### Common
* `mediaType(MediaType)` : MediaType.IMAGE / MediaType.VIDEO
* `cameraTileBackground(R.color.xxx)`
* `cameraTileImage(R.drawable.xxx)`
* `showCameraTile(Boolean) (default: true)`
* `scrollIndicatorDateFormat(String)(default: YYYY.MM)`
* `title(String or R.string.xxx) (default: 'Select Image','사진 선택')`
* `backButton(R.drawable.xxx)`
* `zoomIndicator(Boolean) (default: true)`


#### Multi Select
* `selectedUri(List<Uri>)`
* `buttonGravity(ButtonGravity)`: You can change `done` button location top or bottom
* `buttonText(String or R.string.xxx) (default: 'Done','완료')`
* `max(Int, String or R.string.xxx)`
* `min(Int, String or R.string.xxx)`


</br></br>
## License 
 ```code
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
