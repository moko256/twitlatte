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

package com.github.moko256.plugin.optimizer.twittertext

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.BaseExtension
import com.twitter.twittertext.TwitterTextConfiguration
import com.twitter.twittertext.TwitterTextParser.*
import javassist.ClassClassPath
import javassist.ClassPool
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.io.IOException
import java.util.jar.JarEntry
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream

/**
 * Created by moko256 on 2018/12/29.
 *
 * @author moko256
 */
class TwitterTextTransformer: Transform() {
    private val twitterTextJarNameRegex = "jetified-twitter-text-[0-9]\\.[0-9]\\.[0-9]\\.jar".toRegex()

    override fun getName(): String {
        return "TwitterTextTransformer"
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return mutableSetOf(QualifiedContent.DefaultContentType.CLASSES)
    }

    override fun isIncremental(): Boolean {
        return false
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return mutableSetOf(QualifiedContent.Scope.EXTERNAL_LIBRARIES)
    }

    override fun transform(transformInvocation: TransformInvocation) {
        val outputProvider = transformInvocation.outputProvider

        try {
            outputProvider.deleteAll()
        } catch (ignore: IOException) {}

        val outputDir = outputProvider.getContentLocation(name, inputTypes, scopes, Format.DIRECTORY)
        outputDir.mkdirs()

        transformInvocation.inputs

        val inputJars = transformInvocation.inputs
                .map {
                    it.jarInputs
                }
                .flatten()
                .map { it.file }

        var created = false
        val (twitterTextJars, otherJars) = inputJars
                .asSequence()
                .partition {
                    val matches = it.name.matches(twitterTextJarNameRegex)
                    if (matches && !created) {
                        created = true
                        true
                    } else {
                        false
                    }
                }
        val twitterTextJar = twitterTextJars.first()

        otherJars
                .forEachIndexed { index, file ->
                    file.copyTo(outputDir.resolve((index + 1).toString() + ".jar"))
                }

        val tempJarOutput = outputDir.resolve("0")
        tempJarOutput.mkdir()

        JarInputStream(twitterTextJar.inputStream()).use {
            var entry = it.nextEntry
            while (entry != null) {
                val dst = tempJarOutput.resolve(entry.name)
                dst.parentFile.mkdirs()
                dst.outputStream().use { out ->
                    it.copyTo(out)
                }
                entry = it.nextEntry
            }
        }

        val ctClass = ClassPool()
                .apply {
                    appendSystemPath()
                    appendClassPath(
                            ClassClassPath(String::class.java)
                    )
                    appendPathList(tempJarOutput.absolutePath)
                }
                .getCtClass("com.twitter.twittertext.TwitterTextConfiguration")
        val replaceCode = replaceCode()
        ctClass.getDeclaredMethod("configurationFromJson").setBody(replaceCode)
        ctClass.writeFile(tempJarOutput.absolutePath)

        JarOutputStream(outputDir.resolve("0.jar").outputStream()).use { jarOut ->
            tempJarOutput.walk().filter { it.absolutePath != tempJarOutput.absolutePath && !it.isDirectory }.forEach {
                jarOut.putNextEntry(
                        JarEntry(it.absolutePath.removePrefix(tempJarOutput.absolutePath).removePrefix(File.separator))
                )
                it.inputStream().use { fileIn ->
                    fileIn.copyTo(jarOut)
                }
            }
        }
        tempJarOutput.deleteRecursively()
    }

    private fun replaceCode(): String {
       return StringBuilder().also {
           appendCaseBlock(it,"v1.json", TWITTER_TEXT_CODE_POINT_COUNT_CONFIG)
           appendCaseBlock(it,"v2.json", TWITTER_TEXT_WEIGHTED_CHAR_COUNT_CONFIG)
           appendCaseBlock(it,"v3.json", TWITTER_TEXT_EMOJI_CHAR_COUNT_CONFIG)
           it.append("{ throw new Exception(); }")
       }.toString()
    }

    private fun appendCaseBlock(builder: StringBuilder, jsonName: String, value: TwitterTextConfiguration) {
        builder.append("if ($1.equals(\"$jsonName\")) {")

                .append("return new com.twitter.twittertext.TwitterTextConfiguration()")
                .append(".setVersion(${value.version})")
                .append(".setMaxWeightedTweetLength(${value.maxWeightedTweetLength})")
                .append(".setScale(${value.scale})")
                .append(".setDefaultWeight(${value.defaultWeight})")
                .append(".setTransformedURLLength(${value.version})")
                .append(".setEmojiParsingEnabled(${value.emojiParsingEnabled})")
                .append(".setRanges(")
                .append(
                        if (jsonName == "v1.json") {
                            "com.twitter.twittertext.TwitterTextConfiguration.DEFAULT_RANGES"
                        } else {
                            "java.util.Collections.EMPTY_LIST"
                        }
                )
                .append(");")
                .append("} else ")
    }
}

class TwitterTextTransformerPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        val android = project.extensions.getByName("android") as BaseExtension
        android.registerTransform(TwitterTextTransformer())
    }

}