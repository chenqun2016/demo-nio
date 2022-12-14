package com.nio.demo;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

public class NioClient {

    /**
     * 启动
     */
    public void start(String nickName) throws Exception{
        //连接服务器端
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8000));
        System.out.println("客户端启动成功!昵称："+nickName);

        //接收服务器端响应
        //新开线程，专门负责来接收服务器端的响应数据
        Selector selector = Selector.open();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        new Thread(new NioClientHandler(selector)).start();

        //向服务器端发送数据
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()){
            String request = scanner.nextLine();
            if(null != request && request.length()>0){
                socketChannel.write(Charset.forName("UTF-8").encode(nickName+":"+request));
            }
        }
    }
}
