package com.cgl.netty.nio.c1;

import java.nio.ByteBuffer;

/**
 * @author chenguanglei
 * @date 2023/3/28
 */
public class TestByteBufferWrite {

    public static void main(String[] args) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);

        byteBuffer.put((byte) 0x61);
        ByteBufferUtil.debugAll(byteBuffer);

        byteBuffer.put(new byte[]{0x62, 0x63, 0x64});
        ByteBufferUtil.debugAll(byteBuffer);

        byteBuffer.flip();
        System.out.println(byteBuffer.get());
        ByteBufferUtil.debugAll(byteBuffer);

        byteBuffer.compact();
        ByteBufferUtil.debugAll(byteBuffer);

    }

}
