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
import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

/**
 * Created by moko256 on 2018/12/29.
 *
 * @author moko256
 */
class TwitterTextTransformer: Transform() {
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
        val inputs = transformInvocation.inputs
        val classNames = inputs.map { transformInput ->
            transformInput.directoryInputs.map { directoryInput ->
                val path = directoryInput.file.absolutePath
                directoryInput
                        .file
                        .walk()
                        .filter { it.isFile && it.absolutePath.endsWith(".class") }
                        .map {
                            it.absolutePath
                                    .removePrefix(path + File.separatorChar)
                                    .removeSuffix(".class")
                        }
            }
        }
    }

}

class TwitterTextTransformerPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions
                .getByType(AppExtension::class.java)
                .registerTransform(TwitterTextTransformer())
    }

}