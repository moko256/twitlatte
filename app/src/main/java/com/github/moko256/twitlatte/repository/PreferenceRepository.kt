/*
 * Copyright 2015-2018 The twitlatte authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.moko256.twitlatte.repository

import android.content.SharedPreferences
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

/**
 * Created by moko256 on 2018/06/25.
 *
 * @author moko256
 */
const val KEY_ACCOUNT_KEY = "AccountKey"
const val KEY_NIGHT_MODE = "nightModeType"
const val KEY_USE_CHROME_CUSTOM_TAB = "useChromeCustomTab"
const val KEY_IS_PATTERN_TWEET_MUTE = "patternTweetMuteEnabled"
const val KEY_TWEET_MUTE_PATTERN = "tweetMutePattern"
const val KEY_IS_PATTERN_TWEET_MUTE_SHOW_ONLY_IMAGE = "patternTweetMuteShowOnlyImageEnabled"
const val KEY_TWEET_MUTE_SHOW_ONLY_IMAGE_PATTERN = "tweetMuteShowOnlyImagePattern"
const val KEY_IS_PATTERN_USER_SCREEN_NAME_MUTE = "patternUserScreenNameMuteEnabled"
const val KEY_USER_SCREEN_NAME_MUTE_PATTERN = "userScreenNameMutePattern"
const val KEY_IS_PATTERN_USER_NAME_MUTE = "patternUserNameMuteEnabled"
const val KEY_USER_NAME_MUTE_PATTERN = "userNameMutePattern"
const val KEY_IS_PATTERN_TWEET_SOURCE_MUTE = "patternTweetSourceMuteEnabled"
const val KEY_TWEET_SOURCE_MUTE_PATTERN = "tweetSourceMutePattern"
const val KEY_TIMELINE_IMAGE_LOAD_MODE = "timelineImageMode"

class PreferenceRepository(private val preferences: SharedPreferences) {

    fun getString(key: String, defaultValue: String): String
            = preferences.getString(key, defaultValue)!!

    fun getString(key: String): String?
            = preferences.getString(key, null)

    fun getBoolean(key: String, defaultValue: Boolean): Boolean
            = preferences.getBoolean(key, defaultValue)


    private val emptyPattern by lazy {
        Pattern.compile("")
    }
    private val patterns = HashMap<String, Pattern>(5)

    fun getPattern(key: String): Pattern {
        val cachedPattern = patterns[key]
        if (cachedPattern != null){
            return cachedPattern
        }

        val patternString = getString(key)
        return if (patternString != null && patternString.isNotEmpty()) {
            try {
                val compiledPattern = Pattern.compile(patternString)
                patterns[key] = compiledPattern
                compiledPattern
            } catch(e: PatternSyntaxException){
                e.printStackTrace()
                emptyPattern
            }
        } else {
            emptyPattern
        }
    }


    fun putString(key: String, value: String) {
        preferences.edit()
                .putString(key, value)
                .apply()
    }

    fun putBoolean(key: String, value: Boolean) {
        preferences.edit()
                .putBoolean(key, value)
                .apply()
    }

    @Throws(PatternSyntaxException::class)
    fun updateRegex(key: String, value: String) {
        if (value.isNotEmpty()) {
            try {
                patterns[key]= Pattern.compile(value)
            } catch(e: PatternSyntaxException){
                patterns.remove(key)
                throw e
            }
        } else {
            patterns.remove(key)
        }
    }

}