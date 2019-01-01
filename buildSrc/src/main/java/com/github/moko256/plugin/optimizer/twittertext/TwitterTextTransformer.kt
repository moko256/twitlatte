/*
 * Copyright 2015-2019 The twitlatte authors
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

import com.android.build.api.transform.*
import com.android.build.gradle.BaseExtension
import com.twitter.twittertext.TwitterTextConfiguration
import com.twitter.twittertext.TwitterTextParser.*
import javassist.ClassClassPath
import javassist.ClassPool
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.nio.file.Files
import java.util.jar.JarEntry
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream
import kotlin.reflect.jvm.jvmName

/**
 * Created by moko256 on 2018/12/29.
 *
 * @author moko256
 */
private val twitterTextJarNameRegex = "jetified-twitter-text-[0-9]\\.[0-9]\\.[0-9]\\.jar".toRegex()

private enum class BuildStatus {
    CREATE,
    STAY,
    DELETE
}

class TwitterTextTransformer: Transform() {
    override fun getName(): String = "TwitterTextTransformer"

    override fun isIncremental(): Boolean = false
    override fun isCacheable(): Boolean = true

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> = mutableSetOf(
            QualifiedContent.DefaultContentType.CLASSES
    )

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> = mutableSetOf(
            QualifiedContent.Scope.EXTERNAL_LIBRARIES
    )

    override fun transform(transformInvocation: TransformInvocation) {
        val outputDir = transformInvocation
                .outputProvider
                .getContentLocation(name, inputTypes, scopes, Format.DIRECTORY)
        try {
            outputDir.deleteRecursively()
            outputDir.mkdirs()
        } catch (ignore: Throwable) {
        }

        transformInvocation.inputs
                .asSequence()
                .map { input ->
                    listOf(
                            input.jarInputs.map { mapOf(it.file to it.status) },
                            input.directoryInputs.map { it.changedFiles }
                    )
                }
                .flatten()
                .flatten()
                .map { it.entries }
                .flatten()
                .onEach {
                    println("LOGGING [" + it.value.name + "] " + it.key.absolutePath)
                }
                .filter { !transformInvocation.isIncremental || it.value != Status.NOTCHANGED }
                .forEachIndexed { index, jarInput ->
                    val buildStatus = if (transformInvocation.isIncremental) {
                        when(jarInput.value!!) {
                            Status.ADDED, Status.CHANGED -> {
                                BuildStatus.CREATE
                            }
                            Status.REMOVED -> {
                                BuildStatus.DELETE
                            }
                            Status.NOTCHANGED -> {
                                BuildStatus.STAY
                            }
                        }
                    } else {
                        BuildStatus.CREATE
                    }
                    val matches = jarInput.key.name.matches(twitterTextJarNameRegex)
                    if (matches) {
                        twitterTextJarsTransform(buildStatus, jarInput, "0", outputDir)
                    } else {
                        otherJarsTransform(buildStatus, jarInput, (index + 1).toString(), outputDir)
                    }
                }
    }

    private fun twitterTextJarsTransform(buildStatus: BuildStatus, twitterTextJar: Map.Entry<File, Status>, jarName: String, outputDir: File) {
        val convertedTwitterTextJar = outputDir.resolve("$jarName.jar")

        when (buildStatus) {
            BuildStatus.CREATE -> {
                val tempJarOutput = outputDir.resolve(jarName)
                tempJarOutput.mkdir()

                JarInputStream(twitterTextJar.key.inputStream()).use {
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

                ClassPool()
                        .apply {
                            appendSystemPath()
                            appendClassPath(
                                    ClassClassPath(String::class.java)
                            )
                            appendPathList(tempJarOutput.absolutePath)
                        }
                        .getCtClass(TwitterTextConfiguration::class.jvmName)
                        .apply {
                            getDeclaredMethod("configurationFromJson")
                                    .setBody(replaceCode())

                            writeFile(tempJarOutput.absolutePath)
                        }

                JarOutputStream(convertedTwitterTextJar.outputStream()).use { jarOut ->
                    tempJarOutput
                            .walk()
                            .filter { !it.isDirectory }
                            .forEach {
                                jarOut.putNextEntry(
                                        JarEntry(
                                                it.absolutePath
                                                        .removePrefix(tempJarOutput.absolutePath)
                                                        .removePrefix(File.separator)
                                        )
                                )
                                it.inputStream().use { fileIn ->
                                    fileIn.copyTo(jarOut)
                                }
                            }
                }
                tempJarOutput.deleteRecursively()
                twitterTextJar.key.delete()
            }
            BuildStatus.DELETE -> {
                convertedTwitterTextJar.delete()
            }
            BuildStatus.STAY -> {}
        }
    }

    private fun otherJarsTransform(buildStatus: BuildStatus, input: Map.Entry<File, Status>, jarName: String, outputDir: File) {
        when (buildStatus) {
            BuildStatus.CREATE -> {
                input.key.copyTo(
                        outputDir.resolve("$jarName.jar")
                )
            }
            BuildStatus.DELETE -> {
                outputDir
                        .listFiles { file ->
                            Files.isSameFile(input.key.toPath(), file.toPath())
                        }
                        .forEach { it.delete() }
            }
            BuildStatus.STAY -> {}
        }
    }

    private fun replaceCode(): String {
       return StringBuilder().also {
           appendCaseBlock(it,"v1.json", TWITTER_TEXT_CODE_POINT_COUNT_CONFIG)
           appendCaseBlock(it,"v2.json", TWITTER_TEXT_WEIGHTED_CHAR_COUNT_CONFIG)
           appendCaseBlock(it,"v3.json", TWITTER_TEXT_EMOJI_CHAR_COUNT_CONFIG)
           it.append("{ throw new IllegalStateException(\"Unknown json: \" + $1); }")
       }.toString()
    }

    private fun appendCaseBlock(builder: StringBuilder, jsonName: String, value: TwitterTextConfiguration) {
        builder.append("if ($1.equals(\"$jsonName\")) {")

                .append("return new ${TwitterTextConfiguration::class.jvmName}()")
                .append(".setVersion(${value.version})")
                .append(".setMaxWeightedTweetLength(${value.maxWeightedTweetLength})")
                .append(".setScale(${value.scale})")
                .append(".setDefaultWeight(${value.defaultWeight})")
                .append(".setTransformedURLLength(${value.version})")
                .append(".setEmojiParsingEnabled(${value.emojiParsingEnabled})")
                .append(".setRanges(")
                .append(
                        if (value.ranges.isNotEmpty()) {
                            "${TwitterTextConfiguration::class.jvmName}.DEFAULT_RANGES"
                        } else {
                            "java.util.Collections.EMPTY_LIST"
                        }
                )
                .append(");")
                .append("} else ")
    }
}

@Suppress("unused")
class TwitterTextTransformerPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        val android = project.extensions.getByName("android") as BaseExtension
        android.registerTransform(TwitterTextTransformer())
    }

}