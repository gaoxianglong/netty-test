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
package com.github.test.netty01;

import java.io.*;
import java.net.ServerSocket;
import java.util.Objects;

/**
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2022/5/26 23:39
 */
public class EchoServer {
    public static void main(String[] args) {
        try {
            var ss = new ServerSocket(1443);
            while (true) {
                var socket = ss.accept();
                System.out.printf("client %s connection success...\n", socket.getRemoteSocketAddress());
                new Thread(() -> {
                    try (var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                         var writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
                        String msg = null;
                        while (Objects.nonNull(msg = reader.readLine())) {
                            System.out.printf("client->server:%s\n", msg);
                            writer.write("hi client");
                            writer.flush();
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
