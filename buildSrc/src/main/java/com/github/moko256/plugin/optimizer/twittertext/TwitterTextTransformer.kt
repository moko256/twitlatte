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
import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.jar.JarInputStream

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
        val outputDir = outputProvider.getContentLocation(name, inputTypes, scopes, Format.DIRECTORY)
        outputDir.deleteRecursively()
        outputDir.mkdir()

        val inputJars = transformInvocation.inputs
                .map {
                    it.jarInputs
                }
                .flatten()
                .onEach { println("COPY  " + it.file.absolutePath) }
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

        val commonPath = otherJars
                .map {
                    it.toPath().toList()
                }
                .let {
                    val mostSmallSize = it.map { it.size }.min() ?: 0
                    val commonPath = it
                            .map {
                                it.takeLast(mostSmallSize)
                            }
                            .distinct()
                    commonPath.single()
                }.first()
        commonPath.toFile().copyRecursively(outputDir)

        val tempJarOutput = outputDir.resolve("twitter-text.jar")
        tempJarOutput.mkdir()

        JarInputStream(twitterTextJar.inputStream()).use {
            var entry = it.nextEntry
            while (entry != null) {
                val dst = tempJarOutput.resolve(entry.name)
                dst.parentFile.mkdirs()
                dst.writeBytes(it.readBytes())
                println("EXTRACT" + entry.name)
                entry = it.nextEntry
            }
        }

        val ctClass = ClassPool()
                .apply {
                    appendPathList(tempJarOutput.absolutePath)
                }
                .getCtClass("TwitterTextParser")

        replaceCode(ctClass, "TWITTER_TEXT_CODE_POINT_COUNT_CONFIG")
        replaceCode(ctClass, "TWITTER_TEXT_WEIGHTED_CHAR_COUNT_CONFIG")
        replaceCode(ctClass, "TWITTER_TEXT_EMOJI_CHAR_COUNT_CONFIG")

        ctClass.writeFile()

        println("FIN")
    }

    fun replaceCode(ctClass: CtClass, varName: String) {
        val value = TwitterTextConfiguration::class.java
                .getDeclaredField(varName)
                .get(null) as TwitterTextConfiguration
        val ranges = value.ranges.map {
            it.range.start.toString() + "|" + it.range.end.toString() + "|" + it.weight.toString()
        }.joinToString("$")

        val field = ctClass.getDeclaredField(varName)
        ctClass.removeField(field)
        ctClass.addField(field, CtField.Initializer.byExpr(
                "new com.twitter.twittertext.TwitterTextConfiguration()" +
                        ".setVersion(${value.version})" +
                        ".setMaxWeightedTweetLength(${value.maxWeightedTweetLength})" +
                        ".setScale(${value.scale})" +
                        ".setDefaultWeight(${value.defaultWeight})" +
                        ".setTransformedURLLength(${value.version})" +
                        ".setEmojiParsingEnabled(${value.version})" +
                        if (varName == "TWITTER_TEXT_CODE_POINT_COUNT_CONFIG") {
                            ".setRanges(com.twitter.twittertext.TwitterTextConfiguration.DEFAULT_RANGES)"
                        } else {
                            ""
                        }
        ))
    }
}

class TwitterTextTransformerPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        val android = project.extensions.getByName("android") as BaseExtension
        android.registerTransform(TwitterTextTransformer())
    }

}