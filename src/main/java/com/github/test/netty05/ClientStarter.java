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

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

import java.util.concurrent.TimeUnit;

/**
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2022/5/30 21:24
 */
public class ClientStarter {
    private String address;
    private int port;

    public ClientStarter(String address, int port) {
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
                    handler(new ChannelInitializer<SocketChannel>() { // 处理心跳的handler
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new IdleStateHandler(10, 0, 0, TimeUnit.SECONDS));
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                    if (evt instanceof IdleStateEvent) {
                                        ctx.writeAndFlush(Unpooled.copiedBuffer("heartbeat", CharsetUtil.UTF_8)).addListener(new ChannelFutureListener() {
                                            @Override
                                            public void operationComplete(ChannelFuture future) throws Exception {
                                                if (future.isSuccess()) {
                                                    System.out.println("ok");
                                                } else {
                                                    System.out.println("处理重连");
                                                }
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    System.out.println("server->client:" + ((ByteBuf) msg).toString(CharsetUtil.UTF_8));
                                }
                            });
                        }
                    });
            var cf = bootstrap.connect().sync();
            cf.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
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
