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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2022/5/28 16:20
 */
@ChannelHandler.Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        var channel = ctx.channel();
        var address = channel.remoteAddress().toString();
        var el = channel.eventLoop().toString();
        System.out.printf("channel[%s]和EventLoop[%s]绑定\n", address, el);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        var address = ctx.channel().remoteAddress().toString();
        System.out.printf("channel[%s]连接成功\n", address);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        var address = ctx.channel().remoteAddress().toString();
        System.out.printf("channel[%s]断开连接\n", address);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        var channel = ctx.channel();
        var address = channel.remoteAddress().toString();
        var el = channel.eventLoop().toString();
        System.out.printf("channel[%s]和EventLoop[%s]解绑\n", address, el);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        var bb = (ByteBuf) msg;
        ctx.write(bb);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // 冲刷消息和关闭当前channel
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}