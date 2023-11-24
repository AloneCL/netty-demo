package com.cgl.netty.nio.c1;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * @author chenguanglei
 * @date 2023/3/29
 */
public class TestFileChannelTransferTo {

    public static void main(String[] args) {

        try (FileChannel from = new FileInputStream("data.txt").getChannel();
             FileChannel to = new FileOutputStream("to.txt").getChannel();
        ) {
            long size = from.size();
            //多次传输
            for (long left = size; left > 0; ) {
                //零拷贝 一次最大只能传2g
                left -= from.transferTo((size - left), left, to);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
