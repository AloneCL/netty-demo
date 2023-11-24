package com.cgl.netty.nio.c4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * @author chenguanglei
 * @date 2023/3/29
 */
public class WriteServer {

    public static void main(String[] args) throws IOException {

        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress(8080));
        ssc.configureBlocking(false);
        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {

            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                if (selectionKey.isAcceptable()) {
                    ServerSocketChannel channel = (ServerSocketChannel) selectionKey.channel();
                    SocketChannel sc = channel.accept();
                    sc.configureBlocking(false);
                    SelectionKey scKey = sc.register(selector, SelectionKey.OP_READ);
                    //建立连接后想客户端发送大量数据
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 500_0000; i++) {
                        sb.append("a");
                    }
                    ByteBuffer buffer = Charset.defaultCharset().encode(sb.toString());
                    int len = sc.write(buffer);
                    System.out.println(len);
                    //如果一次没写完 则像selector注册写事件同事将数据当作附件注入 用于下次继续写
                    if (buffer.hasRemaining()) {
                        scKey.interestOps(SelectionKey.OP_WRITE);
                        scKey.attach(buffer);
                    }
                } else if (selectionKey.isWritable()) {
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();
                    int write = channel.write(buffer);
                    System.out.println(write);
                    //写入完成 移除相关附加和事件
                    if (!buffer.hasRemaining()) {
                        selectionKey.attach(null);
                        selectionKey.interestOps(0);
                    }
                }
            }
        }

    }
}
