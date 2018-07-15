/*
 * Copyright 2018 The twicalico authors
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
import org.xml.sax.helpers.DefaultHandler
import javax.xml.parsers.SAXParserFactory

/**
 * Created by moko256 on 2018/02/09.
 *
 * @author moko256
 */
class UrlEntitiesConverter {
    companion object {
        fun convert(text: String): Pair<String, List<TweetEntity>>{
            val saxParser = SAXParserFactory
                    .newInstance()
                    .also {
                        it.isValidating = false
                    }
                    .newSAXParser()
            val handler =  MastodonHandler()
            saxParser.parse(text.byteInputStream(), handler)
        return Pair(handler.stringBuilder.toString(), handler.tweetEntities)
        }
    }
    private class MastodonHandler: DefaultHandler() {
        val stringBuilder = StringBuilder()
        val tweetEntities = ArrayList<TweetEntity>()

        var localName = ""
        lateinit var dealing : StringBuilder

        var isDisplable = false
        var contentUrl = ""
        var start = 0
        var end = 0

        override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
            super.startElement(uri, localName, qName, attributes)
            this.localName = localName?:""
            when (localName) {
                "r" -> {
                    stringBuilder.append("\n")
                }
                "a" -> {
                    dealing = StringBuilder()
                    contentUrl = attributes?.getValue("href")?:""
                    start = stringBuilder.length
                }
                "span" -> {
                    isDisplable = attributes?.getValue("class") != "_invisible"
                }
            }
        }

        override fun characters(ch: CharArray?, start: Int, length: Int) {
            super.characters(ch, start, length)
            when (localName) {
                "p" -> {
                    stringBuilder.append(ch)
                }
                "a" -> {

                }
                "span" -> {
                    if (isDisplable){
                        dealing.append(ch)
                    } else {
                        dealing.append("…")
                    }
                }
            }
        }

        override fun endElement(uri: String?, localName: String?, qName: String?) {
            super.endElement(uri, localName, qName)
            when (localName) {
                "a" -> {
                    end = start + dealing.length -1
                    stringBuilder.append(dealing)
                    tweetEntities.add(object: URLEntity{
                        val _start = this@MastodonHandler.start
                        val _end = this@MastodonHandler.end
                        val _url = this@MastodonHandler.contentUrl
                        val _displayUrl = this@MastodonHandler.dealing

                        override fun getStart(): Int = _start

                        override fun getURL(): String = _url

                        override fun getEnd(): Int = _end

                        override fun getDisplayURL(): String = _displayUrl.toString()

                        override fun getText(): String = _displayUrl.toString()

                        override fun getExpandedURL(): String = url

                    })
                }
                "span" -> {

                }
            }
        }


    }
}