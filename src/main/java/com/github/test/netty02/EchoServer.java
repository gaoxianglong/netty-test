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

import java.util.Objects;

/**
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2022/5/27 20:13
 */
public class EchoServer {
    private int port;
    private ServerBootstrap sbs;
    private NioEventLoopGroup group;

    public EchoServer(int port) throws Throwable {
        this.port = port;
        init();
    }

    private void init() throws Throwable {
        sbs = new ServerBootstrap();
        group = new NioEventLoopGroup();
        var esh = new EchoServerHandler();
        sbs.group(group).
                localAddress(port).
                channel(NioServerSocketChannel.class).
                childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(esh);
                    }
                });
    }

    public void start() throws Throwable {
        if (Objects.isNull(sbs)) {
            return;
        }
        try {
            var cf = sbs.bind().sync();
            System.out.println("server start success");
            cf.channel().closeFuture().sync();
        } finally {
            // 释放所有线程
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) {
        try {
            new EchoServer(1443).start();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
