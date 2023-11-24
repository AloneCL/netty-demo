package com.cgl.netty.nio.c4;

import com.cgl.netty.nio.c1.ByteBufferUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @author chenguanglei
 * @date 2023/3/29
 */
@Slf4j
public class SelectServer {

    public static void main(String[] args) throws IOException {

        Selector selector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();

        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(8080));

        // 建立selector和channel的联系
        //SelectionKey 就是后面事件发送后，通过它可以知道事件和channel的联系
        SelectionKey sscKey = ssc.register(selector, 0, null);
        sscKey.interestOps(SelectionKey.OP_ACCEPT);
        log.debug("register key:{}", sscKey);
        while (true) {
            //select方法 当没有事件发生时会阻塞 事件未处理则不会阻塞
            // 事件发生后要么处理要么取消
            selector.select();
            //获取selector发生改变的监听事件
            Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
            System.out.println(selector.selectedKeys().size());
            while (selectionKeyIterator.hasNext()) {
                SelectionKey key = selectionKeyIterator.next();
                //接收完当前的key后要移除当前key 不然该事件只会标记为已完成但不会被移除 后续会继续进入if方法造成异常
                selectionKeyIterator.remove();
                log.debug("key:{}", key);
                if (key.isAcceptable()) {
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    SocketChannel sc = channel.accept();
                    sc.configureBlocking(false);
                    //将建立的新连接通道也注册到selector
                    //将缓冲区对象设为附件
                    ByteBuffer byteBuffer = ByteBuffer.allocate(16);
                    SelectionKey scKey = sc.register(selector, 0, byteBuffer);
                    scKey.interestOps(SelectionKey.OP_READ);
                    log.debug("{}", sc);
                    log.debug("scKey:{}", key);
                } else if (key.isReadable()) {
                    try {
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer byteBuffer = (ByteBuffer) key.attachment();
                        int len = channel.read(byteBuffer);
                        //客户端正常断开 事件不会关闭但是读取到的字节是-1
                        if (len == -1) {
                            key.cancel();
                        } else {
                            //采用分隔符判断的方式解决半包
                            split(byteBuffer);
                            //判断是否需要扩容
                            if (byteBuffer.position() == byteBuffer.limit()) {
                                ByteBuffer newByteBuffer = ByteBuffer.allocate(byteBuffer.capacity() * 2);
                                byteBuffer.flip();
                                newByteBuffer.put(byteBuffer);
                                //将新的buffer放入key中作为附件
                                key.attach(newByteBuffer);
                            }
                        }
                    } catch (IOException e) {  //客户端异常断开
                        e.printStackTrace();
                        key.cancel();
                    }
                }
            }
        }
    }

    private static void split(ByteBuffer buffer) {
        buffer.flip();
        for (int i = 0; i < buffer.limit(); i++) {
            // 遍历寻找分隔符
            // get(i)不会移动position
            if (buffer.get(i) == '\n') {
                // 缓冲区长度
                int length = i + 1 - buffer.position();
                ByteBuffer target = ByteBuffer.allocate(length);
                // 将前面的内容写入target缓冲区
                for (int j = 0; j < length; j++) {
                    // 将buffer中的数据写入target中
                    target.put(buffer.get());
                }
                // 打印结果
                ByteBufferUtil.debugAll(target);
            }
        }
        // 切换为写模式，但是缓冲区可能未读完，这里需要使用compact
        buffer.compact();
    }

}
