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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2022/5/28 16:30
 */
public class EchoServer {
    private int port;

    public EchoServer(int port) {
        this.port = port;
    }

    /**
     * 启动server侧
     *
     * @throws InterruptedException
     */
    private void start() throws InterruptedException {
        var group = new NioEventLoopGroup();
        var handler = new EchoServerHandler();
        try {
            var bootstarp = new ServerBootstrap();
            bootstarp.
                    group(group).
                    localAddress(port).
                    channel(NioServerSocketChannel.class).
                    childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(handler);
                        }
                    });
            var cf = bootstarp.bind().sync();
            System.out.println("服务器启动...");
            cf.channel().closeFuture().sync();
        } finally {
            // 释放所有线程资源
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) {
        try {
            new EchoServer(1443).start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
