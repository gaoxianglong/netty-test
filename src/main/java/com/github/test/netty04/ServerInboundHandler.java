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
package com.github.test.netty04;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

/**
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2022/5/28 21:11
 */
public class ServerInboundHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.printf("channelhandler[%s]被添加到pipeline\n",
                ServerInboundHandler.class.getSimpleName());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.printf("channelhandler[%s]从pipeline中移除\n",
                ServerInboundHandler.class.getSimpleName());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        var bf = (ByteBuf) msg;
        System.out.printf("refCnt:%s\n", ((ByteBuf) msg).refCnt());
        System.out.printf("c->s:%s", bf.toString(CharsetUtil.UTF_8));
        // 传递给下一个channelhandler
        ctx.fireChannelRead(msg);
//        // 释放资源
//        ReferenceCountUtil.release(msg);

        // 动态改变pipoline的布局,添加一个新的channelhandler在链表的末端
        ctx.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                System.out.printf("c->s:%s", ((ByteBuf) msg).toString(CharsetUtil.UTF_8));
                ReferenceCountUtil.release(msg);
                // 摘除自己
                ctx.pipeline().removeLast();
            }
        });

        // 移除pipoline中的handler
        //ctx.pipeline().removeLast();
        ctx.writeAndFlush(Unpooled.copiedBuffer("test...", CharsetUtil.UTF_8)).
                addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (!future.isSuccess()) {
                            future.cause().printStackTrace();
                            future.channel().close();
                        } else {
                            System.out.println("消息发送成功...");
                        }
                    }
                });
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("ServerInboundHandler:");
        var channel = ctx.channel();
        var remote = channel.remoteAddress().toString();
        System.out.printf("channel[%s]成功连接服务侧\n", remote);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("ServerInboundHandler:");
        var channel = ctx.channel();
        var remote = channel.remoteAddress().toString();
        System.out.printf("channel[%s]断开与服务侧的连接\n", remote);
    }
}
