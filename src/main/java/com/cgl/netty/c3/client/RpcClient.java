package com.cgl.netty.c3.client;

import com.cgl.netty.c3.message.RpcRequestMessage;
import com.cgl.netty.c3.protocol.MessageCodecSharable;
import com.cgl.netty.c3.server.handler.RpcResponseMessageHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author chenguanglei
 * @date 2023/4/19
 */
@Slf4j
public class RpcClient {

    public static void main(String[] args) {

        NioEventLoopGroup work = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        LoggingHandler loggingHandler = new LoggingHandler();
        MessageCodecSharable messageCodecSharable = new MessageCodecSharable();
        RpcResponseMessageHandler rpcResponseMessageHandler = new RpcResponseMessageHandler();

        try {
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
            Channel channel = bootstrap.connect(new InetSocketAddress("127.0.0.1", 8080)).sync().channel();
            ChannelFuture future = channel.writeAndFlush(new RpcRequestMessage(
                    1,
                    "com.cgl.netty.c3.server.service.HelloService",
                    "sayHello",
                    String.class,
                    new Class[]{String.class},
                    new String[]{"张三"}
            )).sync().addListener(promise -> {
                if (!promise.isSuccess()) {
                    throw new RuntimeException(promise.cause());
                }
            });
            channel.closeFuture().addListener(promise -> {
                channel.close();
            });
        } catch (InterruptedException e) {
            log.error("client error", e);
        } finally {
            work.shutdownGracefully();
        }
    }
}
