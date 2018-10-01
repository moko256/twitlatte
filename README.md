twitlatte
====

This application is the SNS client for Android\.

<img src="app/src/main/ic_launcher-web.png"
      alt="Icon of this app"
      width="150"
      height="150">

* Supported Android version is Android 4\.4~9\.0
* Using REST API
* Supported multiple account
* Material design

## Available SNS

* Twitter
* Mastodon
* Pleroma

## Install

[<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png"
      alt="Get it on Google Play"
      height="100">](https://play.google.com/store/apps/details?id=com.github.moko256.twitlatte)
[<img src="https://f-droid.org/badge/get-it-on.png"
      alt="Get it on F-Droid"
      height="100">](https://f-droid.org/packages/com.github.moko256.twitlatte/)

## How to build

It require Java8 and Android Studio 3\.2 to build this project\.

1. Clone this project\.

```sh
# Clone this project and some submodules.
git clone https://github.com/moko256/twitlatte.git --recursive
```

or,

```sh
# Clone this project.
git clone https://github.com/moko256/twitlatte.git

# Clone or update some submodules.
git submodule update --init --recursive
```

2. Open in Android Studio\.
3. Build\.

## Dependencies

This application is built using these libraries\:

* Android Compatibility Library v4
* Android Compatibility Library v7
* Android Compatibility Library v14
* Android Design Support Library
* Android Custom Tabs Support Library
* Glide
* Glide Transformations
* google-gson
* PhotoView
* FlingLayout
* japng
* japng_android
* OkHttp
* mastodon4j
* Twitter4J
* twitter-text
* RxJava
* RxAndroid

and, built using these tools\:

* Android SDK
* Android Studio
* Open JDK
* Kotlin
* Gradle
* Proguard

## License of this project

~~~
Copyright 2015-2018 The twitlatte authors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
~~~
