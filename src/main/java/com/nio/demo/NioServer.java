package com.nio.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class NioServer {

    public void start(){
        /**
         * Nio 7步走
         * 1，创建Selector
         * 2,通过ServerSocketChannel创建Channel通道
         * 3，为channel通道绑定监听端口
         * 4，设置channel为非阻塞模式
         * 5，将channel注册到selector上，监听连接事件
         * 6，循环等待新接入的连接
         * 7，根据就绪状态调用对应方法处理业务逻辑
         */

        try {
            //1，创建Selector
            Selector selector = Selector.open();
            // 2,通过ServerSocketChannel创建Channel通道
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            // 3，为channel通道绑定监听端口
            serverSocketChannel.bind(new InetSocketAddress(8000));
            //4，设置channel为非阻塞模式
            serverSocketChannel.configureBlocking(false);
            //5，将channel注册到selector上，监听连接事件
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("服务器启动成功!");
            //6，循环等待新接入的连接
            for (;;){
                //TODO 获取可用channel数量
                int readyChannels = selector.select();
                //TODO Selector空轮询，导致 CPU 100% 问题
                if(readyChannels == 0) continue;
                // 获取可用channel集合
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()){
                    //SelectionKey 实例
                    SelectionKey selectionKey = iterator.next();
                    //移除Set中的当前selectionKey，
                    //因为selector每次监听到accept都会把SelectionKey放集合中，而不会删除之前的，需要我们自己手动删除
                    iterator.remove();

                    //7，根据就绪状态调用对应方法处理业务逻辑
                    //如果是接入事件
                    if(selectionKey.isAcceptable()){
                        acceptHandler(serverSocketChannel,selector);
                    }
                    //如果是可读事件
                    if(selectionKey.isReadable()){
                        readHandler(selectionKey,selector);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 接入事件处理器
     */
    private void acceptHandler(ServerSocketChannel serverSocketChannel,Selector selector) throws Exception{
        //如果是接入事件，创建socketChannel
        SocketChannel acceptChannel = serverSocketChannel.accept();
        //将socketChannel设置为非阻塞工作模式
        acceptChannel.configureBlocking(false);
        //将channel注册到selector上，监听可读事件
        acceptChannel.register(selector,SelectionKey.OP_READ);
        //回复客户端提示信息
        acceptChannel.write(Charset.forName("UTF-8").encode("你与聊天室其他人都不是朋友关系"));
    }
    /**
     * 可读事件处理器
     */
    private void readHandler(SelectionKey selectionKey,Selector selector) throws Exception{
        //要从SelectionKey中获取到已经就绪的channel
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        //创建buffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        //循环读取客户端请求信息
        String request = "";
        while (socketChannel.read(byteBuffer)>1){
            //切换buffer为读模式
            byteBuffer.flip();
            //读取buffer中的内容
            request+=Charset.forName("UTF-8").decode(byteBuffer);
        }
        //将channel再次注册到selector上，监听他的可读事件
        socketChannel.register(selector,SelectionKey.OP_READ);
        //将客户端发送的请求信息广播给其它客户端
        if(request.length()>0){
            System.out.println(":: "+request);
            broadCast(selector,socketChannel,request);
        }
    }

    /**
     * 广播给其它客户端
     */
    private void broadCast(Selector selector,SocketChannel sourceChannel,String request){
        //获取所有已接入的客户端channel
        //selector.keys()获取所有注册的集合;不同于selector.selectedKeys()获取所有就绪状态下的集合
        Set<SelectionKey> keys = selector.keys();
        keys.forEach(selectionKey -> {
            Channel targetChannel = selectionKey.channel();
            //剔除发消息的客户端
            if(targetChannel instanceof SocketChannel && targetChannel != sourceChannel){
                try {
                    //将信息发送到targetChannel客户端
                    ((SocketChannel) targetChannel).write(Charset.forName("UTF-8").encode(request));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        //循环向所有channel广播信息

    }

    public static void main(String[] args){
        NioServer nioServer = new NioServer();
        nioServer.start();
    }
}
