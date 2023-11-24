package com.cgl.netty.c1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

import java.net.InetSocketAddress;

/**
 * @author chenguanglei
 * @date 2023/3/30
 */
public class HelloServer {

    public static void main(String[] args) {

        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup();
        //1.声明启动器
        new ServerBootstrap()
                //2. 事件轮询组
                .group(boss, worker)
                // 3. 选择服务的channel实现
                .channel(NioServerSocketChannel.class)
                //4. child 负责读写，决定child进行哪些操作
                // ChannelInitializer仅执行一次
                //它的作用是客户端SocketChannel建立连接后 执行initChannel以添加更多的处理器
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        // 5. 增加解码器  用于处理接收的 ByteBuf
                        nioSocketChannel.pipeline().addLast(new StringDecoder());
                        // 6. SocketChanel的业务处理器 使用上一个处理器的返回结果
                        nioSocketChannel.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
                                System.out.println(s);
                            }
                        });
                    }
                })
                // 7. 绑定端口
                .bind(new InetSocketAddress(8080));

    }
}
