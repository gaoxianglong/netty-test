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
package com.github.test.netty07;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.util.List;

/**
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2022/6/10 15:44
 */
public class ClientStarter {
    String host;
    int port;

    ClientStarter(String host, int port) {
        this.host = host;
        this.port = port;
    }

    void start() throws InterruptedException {
        var acceptor = new NioEventLoopGroup(1);
        try {
            var bootstrap = new Bootstrap();
            bootstrap.group(acceptor).remoteAddress(host, port).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    // handler
                    socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
//                            StringBuilder builder = new StringBuilder();
//                            for (int i = 0; i < 1000; i++) {
//                                builder.append(i).append(",");
//                            }
//                            var data = builder.substring(0, builder.toString().length() - 1) + "end";
//                            ByteBuf byteBuf = ctx.alloc().buffer();
//                            byteBuf.writerIndex(6);
//                            byteBuf.writeBytes("wobuhao".getBytes(CharsetUtil.UTF_8));
//                            byteBuf.writerIndex(0);
//                            byteBuf.writeBytes("nihao".getBytes(CharsetUtil.UTF_8));
//                            byteBuf.writerIndex(12);
//                            ctx.writeAndFlush(byteBuf);
//                            System.out.println("send");
                            //ctx.writeAndFlush(Unpooled.copiedBuffer("nihao".getBytes(CharsetUtil.UTF_8)));

                        }
                    });
                }
            });
            var cf = bootstrap.connect().sync();
            while (true){
                cf.channel().writeAndFlush(Unpooled.copiedBuffer("nihao".getBytes(CharsetUtil.UTF_8)));
                try {
                    System.in.read();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //cf.channel().closeFuture().sync();
        } finally {
            acceptor.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) {
        try {
            new ClientStarter("127.0.0.1", 1443).start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
