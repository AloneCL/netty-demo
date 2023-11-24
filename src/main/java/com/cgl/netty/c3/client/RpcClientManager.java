package com.cgl.netty.c3.client;

import com.cgl.netty.c3.message.RpcRequestMessage;
import com.cgl.netty.c3.protocol.MessageCodecSharable;
import com.cgl.netty.c3.protocol.SequenceIdGenerator;
import com.cgl.netty.c3.server.handler.RpcResponseMessageHandler;
import com.cgl.netty.c3.server.service.HelloService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

/**
 * @author chenguanglei
 * @date 2023/4/19
 */
@Slf4j
public class RpcClientManager {

    private static Channel channel;

    private static Object LOCK = new Object();

    public static void main(String[] args) {
        HelloService service = getProxy(HelloService.class);
        System.out.println(service.sayHello("zhangsan"));
        System.out.println(service.sayHello("李四"));

    }

    public static <T> T getProxy(Class<?> clazz) {
        ClassLoader classLoader = clazz.getClassLoader();
        Class[] interfaces = new Class[]{clazz};
        int sequenceId = SequenceIdGenerator.nextId();
        Object o = Proxy.newProxyInstance(classLoader, interfaces, (proxy, method, args) -> {
            RpcRequestMessage msg = new RpcRequestMessage(
                    sequenceId,
                    clazz.getName(),
                    method.getName(),
                    method.getReturnType(),
                    method.getParameterTypes(),
                    args);
            getChannel().writeAndFlush(msg);
            DefaultPromise<Object> promise = new DefaultPromise<>(getChannel().eventLoop());
            RpcResponseMessageHandler.PROMISES.put(sequenceId, promise);
            promise.await();
            if (promise.isSuccess()) {
                return promise.getNow();
            } else {
                throw new RuntimeException(promise.cause());
            }
        });
        return (T) o;
    }

    public static Channel getChannel() {
        if (channel != null) {
            return channel;
        }
        synchronized (LOCK) {
            if (channel == null) {
                initChannel();
            }
        }
        return channel;
    }

    private static void initChannel() {
        NioEventLoopGroup work = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        LoggingHandler loggingHandler = new LoggingHandler();
        MessageCodecSharable messageCodecSharable = new MessageCodecSharable();
        RpcResponseMessageHandler rpcResponseMessageHandler = new RpcResponseMessageHandler();
        bootstrap.group(work);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(loggingHandler);
                ch.pipeline().addLast(messageCodecSharable);
                ch.pipeline().addLast(rpcResponseMessageHandler);
            }
        });
        try {
            channel = bootstrap.connect(new InetSocketAddress("127.0.0.1", 8080)).sync().channel();
            channel.closeFuture().addListener(promise -> {
                work.shutdownGracefully();
            });
        } catch (InterruptedException e) {
            log.error("client error", e);
        }
    }
}
