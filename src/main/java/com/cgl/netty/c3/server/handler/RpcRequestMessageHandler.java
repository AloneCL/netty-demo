package com.cgl.netty.c3.server.handler;

import com.cgl.netty.c3.message.RpcRequestMessage;
import com.cgl.netty.c3.message.RpcResponseMessage;
import com.cgl.netty.c3.server.service.HelloService;
import com.cgl.netty.c3.server.service.ServicesFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author chenguanglei
 * @date 2023/4/18
 */
@ChannelHandler.Sharable
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage rpcRequestMessage) {
        RpcResponseMessage responseMessage = new RpcResponseMessage();
        try {
            responseMessage.setSequenceId(rpcRequestMessage.getSequenceId());
            HelloService helloService = (HelloService) ServicesFactory.getInstance(Class.forName(rpcRequestMessage.getInterfaceName()));
            Method method = helloService.getClass().getMethod(rpcRequestMessage.getMethodName(), rpcRequestMessage.getParameterTypes());
            Object invoke = method.invoke(helloService, rpcRequestMessage.getParameterValue());
            responseMessage.setReturnValue(invoke);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            String message = e.getCause().getMessage();
            responseMessage.setExceptionValue(new Exception("远程调用异常,msg:" + message));
        }
        ctx.writeAndFlush(responseMessage);
    }
}
