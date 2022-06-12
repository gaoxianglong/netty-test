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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

import java.util.Objects;

/**
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2022/6/12 16:10
 */
public class ProtocolCodec {
    /**
     * 定长协议头
     */
    private final int HEADER_LENGTH = 16;
    /**
     * 魔术
     */
    private final short MAGIC = 0x1989;
    /**
     * 魔术高位
     */
    private final byte MAGIC_HIGH = Bytes.short2Bytes(MAGIC)[0];
    /**
     * 魔术低位
     */
    private final byte MAGIC_LOW = Bytes.short2Bytes(MAGIC)[1];
    /**
     * 表示消息请求
     */
    private final byte FLAG_REQUEST = 0b01000000;

    /**
     * 解码操作
     *
     * @param byteBuf
     * @return
     */
    protected Object decode(ByteBuf byteBuf) {
        int rb = byteBuf.readableBytes();
        if (rb < 2) {
            // 返回空表示需要读取更多数据
            return null;
        }
        byte[] header = new byte[Math.min(rb, HEADER_LENGTH)];
        byteBuf.readBytes(header);

        if (header[0] != MAGIC_HIGH || header[1] != MAGIC_LOW) {
            throw new RuntimeException("魔术错误");
        }

        if (rb < HEADER_LENGTH) {
            // 返回空表示需要读取更多数据
            return null;
        }

        // 获取消息总长度
        byte[] temp = new byte[4];
        System.arraycopy(header, 12, temp, 0, temp.length);
        int len = Bytes.bytes2int(temp) + HEADER_LENGTH;

        if (rb < len) {
            // 返回空表示需要读取更多数据
            return null;
        }
        byte[] body = new byte[len - HEADER_LENGTH];
        byteBuf.readBytes(body);
        return deSerialization(header, body);
    }

    /**
     * 编码操作
     *
     * @param body
     * @param context
     */
    protected void encode(ProtocolBody body, long requestId, ChannelHandlerContext context, ByteBuf byteBuf) {
        Objects.requireNonNull(body);
        byte[] header = new byte[HEADER_LENGTH];
        // 第1~2个bytes写入魔术
        header[0] = MAGIC_HIGH;
        header[1] = MAGIC_LOW;
        // 第3个bytes写入请求消息表示与系列化方式,结果为1000-0010
        byte temp = (byte) (FLAG_REQUEST | Serialization.getContentTypeId());
        header[2] = temp;
        // TODO 省略第4个字节
        // 第5~12个字节写入请求序列号
        header[4] = Bytes.long2Bytes(requestId)[0];
        header[5] = Bytes.long2Bytes(requestId)[1];
        header[6] = Bytes.long2Bytes(requestId)[2];
        header[7] = Bytes.long2Bytes(requestId)[3];
        header[8] = Bytes.long2Bytes(requestId)[4];
        header[9] = Bytes.long2Bytes(requestId)[5];
        header[10] = Bytes.long2Bytes(requestId)[6];
        header[11] = Bytes.long2Bytes(requestId)[7];

        // 写入body
        int len = enSerialization(body, byteBuf);

        // 第13~16个字节写入body长度
        header[12] = Bytes.int2Bytes(len)[0];
        header[13] = Bytes.int2Bytes(len)[1];
        header[14] = Bytes.int2Bytes(len)[2];
        header[15] = Bytes.int2Bytes(len)[3];
        byteBuf.writerIndex(0);
        // 写入header
        byteBuf.writeBytes(header);
        byteBuf.writerIndex(byteBuf.writerIndex() + len);
        context.writeAndFlush(byteBuf);
        byteBuf.retain();
    }

    /**
     * 序列化
     *
     * @param body
     * @param byteBuf
     * @return
     */
    private int enSerialization(ProtocolBody body, ByteBuf byteBuf) {
        byteBuf.writerIndex(16);
        byte[] result = Serialization.enSerialization(body.getMsg());
        byteBuf.writeBytes(result);
        return result.length;
    }

    /**
     * 反序列化
     *
     * @param header
     * @param body
     * @return
     */
    private ProtocolBody deSerialization(byte[] header, byte[] body) {
        byte temp = header[2];
        // 高4位是否是消息
        if ((temp & 0b11110000) != FLAG_REQUEST) {
            throw new RuntimeException("非消息");
        }
        if ((temp & 0b00001111) != Serialization.getContentTypeId()) {
            throw new RuntimeException("不存在的序列化类型");
        }
        ProtocolBody protocolBody = new ProtocolBody();
        protocolBody.setMsg((String) Serialization.deSerialization(body, String.class));
        return protocolBody;
    }
}
