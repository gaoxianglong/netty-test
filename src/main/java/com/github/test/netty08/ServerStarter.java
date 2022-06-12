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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.List;

/**
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2022/6/12 18:57
 */
public class ServerStarter {
    int port;

    ServerStarter(int port) {
        this.port = port;
    }

    void start() throws InterruptedException {
        var acceptor = new NioEventLoopGroup(1);
        var worker = new NioEventLoopGroup(8);
        try {
            var bootstrap = new ServerBootstrap();
            bootstrap.group(acceptor, worker).localAddress(port).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    // 添加解码器
                    socketChannel.pipeline().addLast(new DecodeHandler());
                    // 添加编码器
                    socketChannel.pipeline().addLast(new EncodeHandler());
                    socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            var body = (MultiMessage) msg;
                            body.getBodys().forEach(System.out::println);
                        }
                    });
                }
            });
            var cf = bootstrap.bind().sync();
            System.out.println("服务器启动");
            cf.channel().closeFuture().sync();
        } finally {
            acceptor.shutdownGracefully().sync();
            worker.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) {
        try {
            new ServerStarter(1443).start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
