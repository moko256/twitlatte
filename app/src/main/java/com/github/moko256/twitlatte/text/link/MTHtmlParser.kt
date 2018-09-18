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

package com.github.moko256.twitlatte.text.link

import com.github.moko256.twitlatte.text.link.entity.Link
import org.ccil.cowan.tagsoup.Parser
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler

/**
 * Created by moko256 on 2018/02/09.
 *
 * @author moko256
 */
object MTHtmlParser {

    private val handler = MastodonHtmlHandler()
    private val parser = Parser().apply {
        contentHandler = handler
    }

    fun convertToContentAndLinks(text: String): Pair<String, Array<Link>> = try {
        parser.parse(InputSource(text.reader()))

        handler.stringBuilder.toString() to handler.linkList.toTypedArray()
    } catch (e: Throwable) {
        e.printStackTrace()
        text to emptyArray()
    }
}


private const val TYPE_URL = 1
private const val TYPE_TAG = 2
private const val TYPE_USER = 3

private class MastodonHtmlHandler: DefaultHandler() {
    lateinit var stringBuilder: StringBuilder
    val linkList = ArrayList<Link>(6)

    private var noBr = false

    private var type = 0

    private var isDisplayable = true
    private var isNextDots = false

    private lateinit var contentUrl : String
    private lateinit var tag : String
    private lateinit var userName : String

    private var linkStart : Int = -1

    override fun startDocument() {
        stringBuilder = StringBuilder(500)
        noBr = false
        isDisplayable = true
        linkList.clear()
    }

    override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
        when (localName){
            "a" -> {
                val classValue: String? = attributes.getValue("class")
                val linkHref = attributes.getValue("href")?:""
                when {
                    classValue?.contains("hashtag")?:false -> {
                        type = TYPE_TAG
                        tag = linkHref.substringAfterLast("/")
                    }
                    classValue?.contains("mention")?:false -> {
                        type = TYPE_USER
                        val list = linkHref.split("/")
                        userName = list[list.size - 1].replaceFirst("@", "") + "@" + list[list.size - 2]
                    }
                    else -> {
                        type = TYPE_URL
                        contentUrl = linkHref
                    }
                }
                linkStart = stringBuilder.length
            }
            "span" -> {
                isDisplayable = attributes.getValue("class") != "invisible"
            }
            "p" -> {
                if (noBr){
                    stringBuilder.append("\n\n")
                } else {
                    noBr = true
                }
            }
            "br" -> {
                stringBuilder.append("\n")
            }
        }
    }

    override fun characters(ch: CharArray, start: Int, length: Int) {
        if (type == TYPE_URL) {
            if (isNextDots) {
                stringBuilder.append("…")
                isNextDots = false
            } else if (isDisplayable) {
                stringBuilder.append(ch, start, length)
                isNextDots = true
            }
        } else {
            stringBuilder.append(ch, start, length)
        }
    }

    override fun endElement(uri: String, localName: String, qName: String) {
        if (localName == "a") {
            val link = when (type) {
                TYPE_TAG -> "twitlatte://tag/$tag"
                TYPE_USER -> "twitlatte://user/$userName"
                else -> {
                    isNextDots = false

                    contentUrl
                }
            }
            type = 0
            this.linkList.add(Link(link, linkStart, stringBuilder.length))
        }
    }
}