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
package com.github.test.netty03;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import org.junit.Assert;

/**
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2022/5/28 17:15
 */
public class UseByteBuf {
    public static void main(String[] args) {
        var ubb = new UseByteBuf();
        ubb.heapBuf(Unpooled.buffer(5));
        ubb.heapBuf(Unpooled.directBuffer(5, 5));
    }

    /**
     * 堆缓冲区
     */
    public void heapBuf(ByteBuf bf) {
        // bytebuf对应的支撑数组为空数组，因此读写索引，以及可读字节应该都是0
        Assert.assertEquals(0, bf.readerIndex());
        Assert.assertEquals(0, bf.writerIndex());
        Assert.assertEquals(0, bf.readableBytes());
        bf.writeBytes("Hello".getBytes(CharsetUtil.UTF_8));
        // 写入5个字节后，读索引为0，写索引为4+1(加1的目的是指向下一次写的偏移位)
        Assert.assertEquals(0, bf.readerIndex());
        Assert.assertEquals(5, bf.writerIndex());
        // bf.readableBytes()为可读字节数
        var rbs = bf.readableBytes();
        Assert.assertEquals(5, rbs);
        for (int i = 0; i < rbs; i++) {
            // 读5次，改变读索引偏移位
            bf.readByte();
        }
        // 读5个字节后，读索引为4+1(加1的目的是指向下一次读的偏移位)，写索引为5
        Assert.assertEquals(5, bf.readerIndex());
        Assert.assertEquals(5, bf.writerIndex());

        // 将读写索引重置为0
        bf.resetReaderIndex();
        bf.resetWriterIndex();
        Assert.assertEquals(0, bf.readerIndex());
        Assert.assertEquals(0, bf.writerIndex());
        // 因为重置了读写索引，所以可读字节为0，readableBytes = writerIndex - readerIndex
        Assert.assertEquals(0, bf.readableBytes());
        //再次写入5个字节后，容量不变
        bf.writeBytes("Hello".getBytes(CharsetUtil.UTF_8));
        Assert.assertEquals(5, bf.capacity());

        // 跳过2个字节后，读索引为2，写索引为5，可读字节为3,可读数据为llo
        bf.skipBytes(2);
        Assert.assertEquals(2, bf.readerIndex());
        Assert.assertEquals(5, bf.writerIndex());
        Assert.assertEquals(3, bf.readableBytes());
        Assert.assertTrue("llo".equals(bf.toString(CharsetUtil.UTF_8)));

        // 读取剩余可读字节后，读写索引为5，可读字节为0
        var temp = new byte[bf.readableBytes()];
        bf.readBytes(temp);
        System.out.println(new String(temp, CharsetUtil.UTF_8));
        Assert.assertEquals(5, bf.readerIndex());
        Assert.assertEquals(5, bf.writerIndex());
        Assert.assertEquals(0, bf.readableBytes());
        bf.clear();

        bf.writeBytes("Hello".getBytes(CharsetUtil.UTF_8));
        // clear操作等价于readerIndex = writerIndex = 0;
        bf.clear();
        Assert.assertEquals(0, bf.readerIndex());
        Assert.assertEquals(0, bf.writerIndex());
        Assert.assertEquals(0, bf.readableBytes());
        bf.writeBytes("Hello".getBytes(CharsetUtil.UTF_8));
        // 显示修改读索引偏移位
        bf.readerIndex(5);
        Assert.assertEquals(5, bf.readerIndex());
        Assert.assertEquals(5, bf.writerIndex());
        Assert.assertEquals(0, bf.readableBytes());

        // 检查是否有支撑数组
        if (bf.hasArray()) {
            bf.clear();
            bf.writeBytes("Hello".getBytes(CharsetUtil.UTF_8));
            // 获取支撑数组引用
            temp = bf.array();
            temp[0] = 'h';
            // get操作索引位不变
            Assert.assertEquals(temp[0], bf.getByte(0));
            // get方法不改动读写索引偏移位
            Assert.assertEquals(0, bf.readerIndex());
        }

        bf.readerIndex(3);
        Assert.assertEquals(3, bf.readerIndex());
        Assert.assertEquals(5, bf.writerIndex());
        Assert.assertEquals(2, bf.readableBytes());
        // 具体实现为System.arraycopy(source,readerIndex,source,0,writerIndex-readerIndex);
        bf.discardReadBytes();
        Assert.assertEquals(0, bf.readerIndex());
        Assert.assertEquals(2, bf.writerIndex());
        Assert.assertEquals(2, bf.readableBytes());

        bf.clear();
        // 写入4bytes(int)
        bf.writeInt(123);
        Assert.assertEquals(0, bf.readerIndex());
        Assert.assertEquals(4, bf.writerIndex());
        Assert.assertEquals(4, bf.readableBytes());
        // 读取4bytes
        bf.readInt();
        Assert.assertEquals(4, bf.readerIndex());
        Assert.assertEquals(4, bf.writerIndex());
        Assert.assertEquals(0, bf.readableBytes());
        // 写入1byte
        bf.writeByte(0);
        Assert.assertEquals(4, bf.readerIndex());
        Assert.assertEquals(5, bf.writerIndex());
        Assert.assertEquals(1, bf.readableBytes());
        // 判断bytebuf是否可读
        Assert.assertTrue(bf.isReadable());

        bf.clear();
        bf.writeBytes("Hello".getBytes(CharsetUtil.UTF_8));
        // 跳过2个字节保存当前读索引
        bf.skipBytes(2);
        bf.markReaderIndex();
        // 再跳过2个字节重设为之前保存的索引位
        bf.skipBytes(2);
        // resetReaderIndex读取markReaderIndex的值
        bf.resetReaderIndex();
        Assert.assertEquals(2, bf.readerIndex());
        // 强制将索引设置为0
        bf.readerIndex(0);
        Assert.assertEquals(0, bf.readerIndex());
        Assert.assertEquals(5, bf.writerIndex());
        Assert.assertEquals(5, bf.readableBytes());

        if (bf.hasArray()) {
            bf.clear();
            bf.writeBytes("Hello".getBytes(CharsetUtil.UTF_8));
            // 复制一个新的bytebuf，但支撑数组是相同引用
            var nbf = bf.duplicate();
            Assert.assertFalse(nbf == bf);
            Assert.assertTrue(bf.array() == nbf.array());

            // 复制一个新的bytebuf，与支撑数组非相同引用，独立副本
            nbf = bf.copy();
            Assert.assertFalse(nbf == bf);
            Assert.assertFalse(bf.array() == nbf.array());

            // 哪怕无可读字节，也能够复制
            bf.readerIndex(5);
            bf.writerIndex(5);
            Assert.assertEquals(5, bf.readerIndex());
            Assert.assertEquals(5, bf.writerIndex());
            Assert.assertEquals(0, bf.readableBytes());
            nbf = bf.copy(0, bf.capacity());
            Assert.assertEquals(0, nbf.readerIndex());
            Assert.assertEquals(5, nbf.writerIndex());
            Assert.assertEquals(5, nbf.readableBytes());
        }
    }
}
