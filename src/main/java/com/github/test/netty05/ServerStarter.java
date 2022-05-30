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
package com.github.test.netty05;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.SystemPropertyUtil;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2022/5/30 14:33
 */
public class ServerStarter {
    private int port;

    public ServerStarter(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {

        Math.max(1, SystemPropertyUtil.getInt(
                "io.netty.eventLoopThreads", Runtime.getRuntime().availableProcessors() * 2));
        new NioEventLoopGroup();
        var acceptor = new NioEventLoopGroup(2, new DefaultThreadFactory("acceptor"));
        var workerGroup = new NioEventLoopGroup(2, new DefaultThreadFactory("worker"));
        try {
            var bootstrap = new ServerBootstrap();
            bootstrap.
                    group(acceptor, workerGroup).// 绑定acceptor线程组和worker线程组
                    localAddress(port).
                    channel(NioServerSocketChannel.class).
                    childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    var bf = (ByteBuf) msg;
                                    System.out.printf("c->s:%s\n", bf.toString(CharsetUtil.UTF_8));
                                    ReferenceCountUtil.release(bf);
//                                    ctx.channel().eventLoop().execute(new Thread() {
//                                        @Override
//                                        public void run() {
//                                            System.out.println(Thread.currentThread().getName());
//                                        }
//                                    });
                                    System.out.println(Thread.currentThread().getName());
//                                    ctx.executor().execute(() -> {
//                                        System.out.println(Thread.currentThread().getName());
//                                    });
                                    new Thread(() -> {
                                        // 如果当前线程不是Eventloop的支撑线程，则放进Eventloop的队列，再由Eventloop的支撑线程逐个执行。
                                        ctx.writeAndFlush(Unpooled.copiedBuffer("aa", CharsetUtil.UTF_8)).addListener(new ChannelFutureListener() {
                                            @Override
                                            public void operationComplete(ChannelFuture future) throws Exception {
                                                System.out.println(Thread.currentThread().getName());
                                            }
                                        });
                                        System.out.println(Thread.currentThread().getName());
                                    }).start();
                                }
                            });
                        }
                    });
            var cf_1 = bootstrap.bind().sync();
            var cf_2 = bootstrap.bind(1442).sync();
            System.out.println("服务器启动");
            cf_1.channel().closeFuture().sync();
            cf_2.channel().closeFuture().sync();
        } finally {
            acceptor.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
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
