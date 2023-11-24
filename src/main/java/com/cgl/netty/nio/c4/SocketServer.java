package com.cgl.netty.nio.c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static com.cgl.netty.nio.c1.ByteBufferUtil.debugAll;

/**
 * @author chenguanglei
 * @date 2023/3/29
 */
@Slf4j
public class SocketServer {

    public static void main(String[] args) throws IOException {


        Selector selector = Selector.open();

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(8080));
        //设置为非阻塞
        serverSocketChannel.configureBlocking(false);
        SelectionKey selectionKey = serverSocketChannel.register(selector, 0, null);
        selectionKey.interestOps(SelectionKey.OP_ACCEPT);
        List<SocketChannel> channelList = new ArrayList<>();
        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
        while (true) {
            SocketChannel sc = serverSocketChannel.accept();
            if (sc != null) {
                log.debug("connected...{}", sc);
                //通道设为非阻塞 read方法变为非阻塞
                sc.configureBlocking(false);
                channelList.add(sc);
            }
            for (SocketChannel socketChannel : channelList) {
                int len = socketChannel.read(byteBuffer);
                if (len > 0) {
                    byteBuffer.flip();
                    debugAll(byteBuffer);
                    byteBuffer.clear();
                    log.debug("after read... {}", socketChannel);
                }
            }

        }
    }
}
