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

package com.github.moko256.mastodon

import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler

/**
 * Created by moko256 on 2018/02/09.
 *
 * @author moko256
 */
class MTHtmlParser {
    companion object {
        val parser = Parser()
        val handler =  MastodonHtmlHandler()

        init {
            parser.contentHandler = handler
        }

        fun convertToEntities(text: String): String{
            return try {
                parser.parse(InputSource(text.byteInputStream()))
                handler.stringBuilder.toString()
            } catch (e: Throwable){
                e.printStackTrace()
                text
            }
        }

        private class MastodonHtmlHandler: DefaultHandler() {
            val stringBuilder = StringBuilder()

            var noBr = false
            companion object {
                val TYPE_URL = 1
                val TYPE_TAG = 2
                val TYPE_USER = 3
            }

            var type = 0

            var isLink = false
            var isDisplable = true
            var isNextDots = false

            lateinit var contentUrl : String
            lateinit var tag : String
            lateinit var userName : String

            override fun startDocument() {
                stringBuilder.setLength(500)
                type = 0
                isLink = false
                isDisplable = true
                isNextDots = false
                noBr = false
            }

            override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
                when (localName){
                    "a" -> {
                        if (attributes.getValue("rel") == "tag") {
                            type = TYPE_TAG
                            val list = attributes.getValue("href")!!.split("/")
                            tag = list[list.size - 1]
                        } else if (attributes.getValue("class") == "u-url mention"){
                            type = TYPE_USER
                            val list = attributes.getValue("href")!!.split("/")
                            userName = list[list.size - 1] + "@" + list[list.size - 2]
                        } else {
                            type = TYPE_URL
                            contentUrl = attributes.getValue("href")!!
                            isLink = true
                        }
                        stringBuilder.append("[")
                    }
                    "span" -> {
                        isDisplable = attributes.getValue("class") != "invisible"
                    }
                    "p" -> {
                        if (noBr){
                            stringBuilder.append("\\n\\n")
                        } else {
                            noBr = true
                        }
                    }
                    "html", "body" -> {}
                    "br" -> {
                        stringBuilder.append("\\n")
                    }
                    else -> {
                        stringBuilder.append("\\<").append(localName).append("\\>")
                    }
                }
            }

            override fun characters(ch: CharArray, start: Int, length: Int) {
                if (isLink) {
                    if (isNextDots) {
                        stringBuilder.append("…")
                    } else if (isDisplable) {
                        stringBuilder.appendEscaped(ch, start, length)
                        isNextDots = true
                    }
                } else {
                    stringBuilder.appendEscaped(ch, start, length)
                }
            }

            override fun endElement(uri: String, localName: String, qName: String) {
                when (localName) {
                    "a" -> {
                        stringBuilder.append("](")
                        if (type == TYPE_URL){
                            stringBuilder.append(contentUrl)
                            isLink = false
                        } else if (type == TYPE_TAG){
                            stringBuilder.append("twitlatte://tag/").append(tag)
                        } else {
                            stringBuilder.append("twitlatte://user/").append(userName)
                        }
                        stringBuilder.append(")")
                    }
                    "br", "p", "span", "html", "body" -> {}
                    else -> {
                        stringBuilder.append("\\<\\/ ").append(localName).append("\\>")
                    }
                }
            }

            val escapeStr = charArrayOf(
                    '\\',
                    '`',
                    '*',
                    '{',
                    '}',
                    '[',
                    ']',
                    '(',
                    ')',
                    '#',
                    '+',
                    '-',
                    '.',
                    '!'
            )

            private fun StringBuilder.appendEscaped(str: CharArray, offset: Int, len: Int) {
                var index = length
                append(str, offset, len)
                var nowIndex = length
                while (index != nowIndex) {
                    if (escapeStr.contains(get(index))) {
                        insert(index, '\\')
                        index++
                        nowIndex++
                    }

                    index++
                }
            }
        }
    }
}