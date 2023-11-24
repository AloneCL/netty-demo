package com.cgl.netty.c1;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author chenguanglei
 * @date 2023/3/30
 */
@Slf4j
public class HelloClient {

    public static void main(String[] args) throws InterruptedException, IOException {

        NioEventLoopGroup group = new NioEventLoopGroup();
        //1.声明客户端启动器
        ChannelFuture channelFuture = new Bootstrap()
                //声明事件轮询组
                .group(group)
                //选择客户端的Socket实现类  NioSocketChannel代表 基于NIO的客户端实现
                .channel(NioSocketChannel.class)
                // channel初始化器
                // 用于建立连接时添加对应的处理器
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        //发送消息 用于将String 解码成ByteBuf
                        channel.pipeline().addLast(new StringEncoder());
                    }
                })
                //建立连接
                .connect(new InetSocketAddress("127.0.0.1", 8080));
        Channel channel = channelFuture.channel();
        //通过监听器异步处理发送
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                Channel channel = channelFuture.channel();
                log.debug("连接建立,{}", channel);
                channel.writeAndFlush("hello world!");
            }
        });
        ChannelFuture closeFuture = channel.closeFuture();
        closeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                log.debug("连接关闭");
                group.shutdownGracefully();
            }
        });
        System.in.read();
        //因为返回的是一个Future 是异步的 使用sync等待connect连接建立完成
//                .sync()
//                //获取通道对象
//                .channel()
//                //向通道中写入信息
//                .writeAndFlush("Hello world!");

    }
}
