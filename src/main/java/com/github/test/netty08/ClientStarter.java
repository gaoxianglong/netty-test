/*
 * Copyright 2019-2119 gao_xianglong@sina.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.test.netty08;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;

/**
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2022/6/12 21:00
 */
public class ClientStarter {
    String host;
    int port;

    ClientStarter(String host, int port) {
        this.port = port;
        this.host = host;
    }

    void start() throws InterruptedException, IOException {
        var acceptor = new NioEventLoopGroup();
        try {
            var bootstrap = new Bootstrap();
            bootstrap.group(acceptor).remoteAddress(host, port).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new DecodeHandler());
                    socketChannel.pipeline().addLast(new EncodeHandler());
                }
            });
            var cf = bootstrap.connect().sync();
            //while (true) {
            //System.in.read();
//                cf.channel().write(new ProtocolBody() {{
//                    String temp = "";
//                    for (int i = 0; i < 2000; i++) {
//                        temp += i;
//                    }
//                    this.setMsg(temp);
//                }});
            //}
            cf.channel().write(new ProtocolBody() {{
                this.setMsg("Hello");
            }});
            cf.channel().write(new ProtocolBody() {{
                this.setMsg("World");
            }});
            //cf.channel().closeFuture().sync();
        } finally {
            acceptor.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) {
        try {
            new ClientStarter("127.0.0.1", 1443).start();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
