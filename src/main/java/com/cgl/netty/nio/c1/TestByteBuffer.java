package com.cgl.netty.nio.c1;


import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author chenguanglei
 * @date 2023/3/28
 */
@Slf4j
public class TestByteBuffer {

    public static void main(String[] args) {

        try {
            FileChannel fileChannel = new FileInputStream("data.txt").getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(10);
            int len;
            while ((len = fileChannel.read(byteBuffer)) != -1) {
                //切换读模式  修改bytebuffer内部的position和limit的两个指针的位置
                // 另一个方法为compact 起点是在上次未读取完的位置
                byteBuffer.flip();
                log.debug("读取的字节数:{}", len);
                log.debug("读取的内容为：{}", new String(byteBuffer.array(), "utf-8"));
                byteBuffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
