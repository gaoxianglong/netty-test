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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2022/6/10 15:44
 */
public class ServerStarter {
    int port;

    ServerStarter(int port) {
        this.port = port;
    }

    void start() throws InterruptedException {
        var acceptor = new NioEventLoopGroup(1);
        var worker = new NioEventLoopGroup(1);
        try {
            var bootstrap = new ServerBootstrap();
            bootstrap.group(acceptor, worker).localAddress(port).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    // 解码器
                    socketChannel.pipeline().addLast(new ByteToMessageDecoder() {
                        @Override
                        protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
                            if (byteBuf.readableBytes() < 1) {
                                return;
                            }
//                            String temp = "";
//                            byte[] value = new byte[byteBuf.readableBytes()];
//                            while (true) {
//                                if (temp.lastIndexOf("end") != 0) {
//                                    System.out.println("======");
//                                    break;
//                                }
//                            }
//                            list.add(temp);
//                            System.out.println("length:" + temp.length());

                            System.out.println(Thread.currentThread().getName() + ":" + byteBuf.readableBytes());
                        }
                    });
                    // handler
                    socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            var temp = (ByteBuf) msg;
                            System.out.println(((ByteBuf) msg).toString(CharsetUtil.UTF_8));
                            //System.out.println(String.format("size:%s,value:%s", temp.length(), temp));
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
