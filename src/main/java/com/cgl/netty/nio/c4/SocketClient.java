package com.cgl.netty.nio.c4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * @author chenguanglei
 * @date 2023/3/29
 */
public class SocketClient {

    public static void main(String[] args) {
        try (SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.connect(new InetSocketAddress("127.0.0.1", 8080));
            SocketAddress socketAddress = socketChannel.getLocalAddress();
            socketChannel.write(Charset.defaultCharset().encode("023423"));
            System.out.println("waiting...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
