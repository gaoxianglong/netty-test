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
package com.github.test.netty02;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2022/5/28 16:58
 */
public class EchoClient {
    private String address;
    private int port;

    public EchoClient(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public void start() throws InterruptedException {
        var group = new NioEventLoopGroup();
        try {
            var bootstrap = new Bootstrap();
            bootstrap.
                    group(group).
                    remoteAddress(address, port).
                    channel(NioSocketChannel.class).
                    handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new EchoClientHandler());
                        }
                    });
            var cf = bootstrap.connect().sync();
            cf.channel().closeFuture().sync();
        } finally {
            // 释放线程资源
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) {
        try {
            new EchoClient("127.0.0.1", 1443).start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
