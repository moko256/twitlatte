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

import org.ccil.cowan.tagsoup.Parser
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler
import twitter4j.HashtagEntity
import twitter4j.TweetEntity
import twitter4j.URLEntity
import javax.xml.parsers.SAXParserFactory

/**
 * Created by moko256 on 2018/02/09.
 *
 * @author moko256
 */
class MTUrlEntitiesConverter {
    companion object {
        fun convertToEntities(text: String): Pair<String, List<TweetEntity>>{
            val parser = Parser()
            val handler =  MastodonHandler()
            parser.contentHandler = handler
            return try {
                parser.parse(InputSource(text.byteInputStream()))
                Pair(handler.stringBuilder.toString(), handler.tweetEntities)
            } catch (e: Throwable){
                e.printStackTrace()
                Pair(text, emptyList())
            }
        }
    }
}

private class MastodonHandler: DefaultHandler() {
    val stringBuilder = StringBuilder()
    val tweetEntities = ArrayList<TweetEntity>()

    lateinit var localName : String
    lateinit var dealing : StringBuilder

    companion object {
        val TYPE_URL = 1
        val TYPE_TAG = 2
        val TYPE_USER = 3
    }

    var type = 0

    var isDisplable = false
    lateinit var contentUrl : String
    lateinit var tag : String
    var start = 0
    var end = 0

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
        super.startElement(uri, localName, qName, attributes)
        this.localName = localName?:""
        when (localName) {
            "br" -> {
                stringBuilder.append("\n")
            }
            "a" -> {
                dealing = StringBuilder()
                if (attributes?.getValue("rel") == "tag") {
                    type = TYPE_TAG
                } else {
                    type = TYPE_URL
                    contentUrl = attributes?.getValue("href") ?: ""
                }
                start = stringBuilder.length
            }
            "span" -> {
                isDisplable = attributes?.getValue("class") != "_invisible"
            }
            "p" -> {

            }
            "html" -> {

            }
            "body" -> {

            }
            else -> {
                stringBuilder.append("<").append(localName).append(">")
            }
        }
    }

    override fun characters(ch: CharArray?, start: Int, length: Int) {
        super.characters(ch, start, length)
        when (localName) {
            "p" -> {
                stringBuilder.append(ch, 0, length)
            }
            "a" -> {

            }
            "span" -> {
                if (type == TYPE_URL){
                    if (isDisplable){
                        dealing.append(ch, 0, length)
                    } else {
                        dealing.append("…")
                    }
                } else if (type == TYPE_TAG){
                    tag = if (ch != null){ String(ch, 0, length) } else {""}
                }
            }
            "br" -> {

            }
            else -> {

            }
        }
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        super.endElement(uri, localName, qName)
        when (localName) {
            "a" -> {
                end = start + dealing.length -1
                stringBuilder.append(dealing)
                if (type == TYPE_URL){
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
                } else if (type == TYPE_TAG){
                    tweetEntities.add(object: HashtagEntity{
                        val _start = this@MastodonHandler.start
                        val _end = this@MastodonHandler.end
                        val _tag = this@MastodonHandler.tag

                        override fun getStart(): Int = _start

                        override fun getEnd(): Int = _end

                        override fun getText(): String = _tag

                    })
                }
            }
            "br" -> {

            }
            "span" -> {

            }
            "p" -> {

            }
            "html" -> {

            }
            "body" -> {

            }
            else -> {
                stringBuilder.append("</ ").append(localName).append(">")
            }
        }
    }


}