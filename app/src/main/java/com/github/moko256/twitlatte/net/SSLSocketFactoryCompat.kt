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

package com.github.moko256.twitlatte.net

import java.net.InetAddress
import java.net.Socket
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager

/**
 * Created by moko256 on 2018/06/23.
 *
 * @author moko256
 */
class SSLSocketFactoryCompat(trustManager: TrustManager) : SSLSocketFactory() {

    private val internal: SSLSocketFactory

    init {
        val context = SSLContext.getInstance("TLS")
        context.init(null, arrayOf(trustManager), null)
        internal = context.socketFactory
    }

    override fun getDefaultCipherSuites(): Array<String> = internal.defaultCipherSuites

    override fun createSocket(s: Socket?, host: String?, port: Int, autoClose: Boolean): Socket =
            internal.createSocket(s, host, port, autoClose).enableModernTls()

    override fun createSocket(host: String?, port: Int): Socket =
            internal.createSocket(host, port).enableModernTls()

    override fun createSocket(host: String?, port: Int, localHost: InetAddress?, localPort: Int): Socket =
            internal.createSocket(host, port, localHost, localPort).enableModernTls()

    override fun createSocket(host: InetAddress?, port: Int): Socket =
            internal.createSocket(host, port).enableModernTls()

    override fun createSocket(address: InetAddress?, port: Int, localAddress: InetAddress?, localPort: Int): Socket =
            internal.createSocket(address, port, localAddress, localPort).enableModernTls()

    override fun getSupportedCipherSuites(): Array<String> =
            internal.supportedCipherSuites

    private fun Socket.enableModernTls(): Socket {
        if (this is SSLSocket) {
            enabledProtocols = supportedProtocols
            enabledCipherSuites = supportedCipherSuites
        }
        return this
    }
}