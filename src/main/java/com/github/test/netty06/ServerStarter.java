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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2022/6/4 22:29
 */
public class ServerStarter {
    int port;

    ServerStarter(int port) {
        this.port = port;
    }

    void start() throws InterruptedException {
        var acceptor = new NioEventLoopGroup(1, new DefaultThreadFactory("acceptor"));
        var worker = new NioEventLoopGroup(16, new DefaultThreadFactory("worker"));
        try {
            var bootstrap = new ServerBootstrap();
            bootstrap.group(acceptor, worker).localAddress(port).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            var bf = (ByteBuf) msg;
                            System.out.printf("c----->s:%s\n", bf.toString(CharsetUtil.UTF_8));
//                            ReferenceCountUtil.release(msg);
//                            ctx.writeAndFlush(Unpooled.copiedBuffer("Hello", CharsetUtil.UTF_8));
                        }

                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                            cause.printStackTrace();
                            ctx.close();
                        }
                    });
                }
            });
            // 允许重复使用本地地址和端口,服务器常见设置
            bootstrap.option(ChannelOption.SO_REUSEADDR, true).option(ChannelOption.TCP_NODELAY, false);
//            bootstrap.option(ChannelOption.SO_RCVBUF, 500);
            // 启动nagle
            var cf = bootstrap.bind().sync();
            System.out.println("启动服务器");
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
