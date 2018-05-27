twitlatte
====

This application is the SNS client for Android\.

|![Icon](app/src/main/ic_launcher-web.png)|Timeline|Navigation drawer|User screen|Post screen|Trends screen|
|---|---|---|---|---|---|
|![Header](readme_image/twitlatte-header.png)|![Timeline](readme_image/home.png)|![Navigation drawer](readme_image/navbar.png)|![User screen](readme_image/user.png)|![Post screen](readme_image/post.png)|![Trends screen](readme_image/trends.png)|

* Supported Android api level is 19~27
* Using REST API
* Supported multiple account
* Material design

## Available SNS
* Twitter
* Mastodon
* Pleroma

## Install

[![Get it on Google Play](https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png)](https://play.google.com/store/apps/details?id=com.github.moko256.twitlatte&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1)

## How to build

1. Clone this project by this\.

This repository uses some submodules, so use this

```sh
git clone https://github.com/moko256/twitlatte.git --recursive
```

or, if you have already cloned without that, use this in it

```sh
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

## License

~~~~
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
~~~~
