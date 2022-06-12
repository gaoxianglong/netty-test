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
package com.github.test.netty06;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

import java.io.IOException;
import java.util.Scanner;

/**
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2022/6/4 22:44
 */
public class ClientStarter {
    String host;
    int port;

    ClientStarter(String host, int port) {
        this.host = host;
        this.port = port;
    }

    void start() throws InterruptedException {
        var group = new NioEventLoopGroup();
        try {
            var bootstrap = new Bootstrap();
            bootstrap.group(group).remoteAddress(host, port).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            var bf = (ByteBuf) msg;
                            System.out.printf("s->c:%s\n", ((ByteBuf) msg).toString(CharsetUtil.UTF_8));
                            ReferenceCountUtil.release(bf);
                            System.out.println(ctx.channel().attr(AttributeKey.valueOf("id")));
                        }

                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            //ctx.writeAndFlush(Unpooled.copiedBuffer("Hello", CharsetUtil.UTF_8));
                        }
                    });
                }
            });
            bootstrap.attr(AttributeKey.valueOf("id"), 123);
            var cf = bootstrap.connect().sync();
            while (true) {
                try {
                    System.in.read();
                    for (int i = 0; i < 1; i++) {
                        cf.channel().writeAndFlush(Unpooled.copiedBuffer(new byte[5]));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //cf.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) {
//        try {
//            new ClientStarter("127.0.0.1", 1443).start();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        for(int i = 0;i<200;i++){
            System.out.println(i % 100 < 5);
        }
    }
}
