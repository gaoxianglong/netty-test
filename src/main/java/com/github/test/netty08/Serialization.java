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

import com.alibaba.fastjson.JSON;

/**
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2022/6/12 16:29
 */
public class Serialization {
    public static byte[] enSerialization(Object src) {
        return JSON.toJSONBytes(src);
    }

    public static Object deSerialization(byte[] src, Class cls) {
        return JSON.parseObject(src, cls);
    }

    public static byte getContentTypeId() {
        // 二进制位00000010
        return 0x2;
    }
}
