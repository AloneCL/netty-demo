package com.cgl.netty.nio.c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.cgl.netty.nio.c1.ByteBufferUtil.debugAll;

/**
 * @author chenguanglei
 * @date 2023/3/30
 */
@Slf4j
public class MultiThreadServer {

    public static void main(String[] args) throws IOException {

        Thread.currentThread().setName("boss");
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(8080));
        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        Work work = new Work("worker");
        while (true) {
            selector.select();
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                if (key.isAcceptable()) {
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    log.debug("connected...{}", sc);
                    // 多线程信号传递关联selector
                    log.debug("before register...{}", sc.getRemoteAddress());
                    work.register(sc);
                    log.debug("after register...{}", sc.getRemoteAddress());
                }
            }
        }

    }

    static class Work implements Runnable {
        private Selector selector;
        private String name;
        private Thread thread;
        //使用队列存储任务 用于线程之间进行通信
        private ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();
        private volatile boolean start = false;

        public Work(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    //多线程可能阻塞 导致写任务进来时被select挡住无法注册成功
                    selector.select();
                    //从队列中拿出任务执行注册方法
                    Runnable task = queue.poll();
                    if (task != null) {
                        task.run();
                    }
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();
                        if (key.isReadable()) {
                            ByteBuffer buffer = ByteBuffer.allocate(16);
                            SocketChannel channel = (SocketChannel) key.channel();
                            int len = channel.read(buffer);
                            buffer.flip();
                            debugAll(buffer);
                            if (len == -1) {
                                key.cancel();
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void register(SocketChannel sc) throws IOException {
            if (!start) {
                thread = new Thread(this, name);
                selector = Selector.open();
                thread.start();
                start = true;
            }
            queue.add(() -> {
                try {
                    sc.register(selector, SelectionKey.OP_READ, null);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            });
            //唤醒selector监听事件的阻塞 让其继续执行
            selector.wakeup();
        }
    }
}
