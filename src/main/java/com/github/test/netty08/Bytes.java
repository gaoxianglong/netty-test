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

/**
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2022/6/12 16:12
 */
public class Bytes {
    public static byte[] short2Bytes(short v) {
        byte[] result = new byte[2];
        result[1] = (byte) v;
        result[0] = (byte) (v >>> 8);
        return result;
    }

    public static byte[] long2Bytes(long v) {
        byte[] result = new byte[8];
        result[7] = (byte) v;
        result[6] = (byte) (v >>> 8);
        result[5] = (byte) (v >>> 16);
        result[4] = (byte) (v >>> 24);
        result[3] = (byte) (v >>> 32);
        result[2] = (byte) (v >>> 40);
        result[1] = (byte) (v >>> 48);
        result[0] = (byte) (v >>> 56);
        return result;
    }

    public static byte[] int2Bytes(int v) {
        byte[] result = new byte[4];
        result[3] = (byte) v;
        result[2] = (byte) (v >>> 8);
        result[1] = (byte) (v >>> 16);
        result[0] = (byte) (v >>> 24);
        return result;
    }

    public static int bytes2int(byte[] v) {
        return ((v[0] & 0xFF) << 24) + ((v[1] & 0xFF) << 16) + ((v[2] & 0xFF) << 8) + ((v[3] & 0xFF) << 0);
    }

    public static int bytes2short(byte[] v) {
        return ((v[0] & 0xFF) << 8) + ((v[1] & 0xFF) << 0);
    }

    public static long bytes2long(byte[] v) {
        long result = 0;
        int n = 56;
        for (int i = 0; i < v.length; n -= 8, i++) {
            result += (v[i] & 0xFF) << n;
        }
        return result;
    }
}
