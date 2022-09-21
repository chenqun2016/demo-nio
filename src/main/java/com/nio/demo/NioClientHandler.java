package com.nio.demo;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * 客户端线程类，专门接收服务器端响应信息
 */
public class NioClientHandler implements Runnable{

    private Selector selector;
    public NioClientHandler(Selector selector){
        this.selector = selector;
    }
    @Override
    public void run() {
        try{
            for (;;){
                int readyChannels = selector.select();
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
                    //如果是可读事件
                    if(selectionKey.isReadable()){
                        readHandler(selectionKey,selector);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 可读事件处理器
     */
    private void readHandler(SelectionKey selectionKey, Selector selector) throws Exception{
        //要从SelectionKey中获取到已经就绪的channel
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        //创建buffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        //循环读取服务器响应信息
        String response = "";
        while (socketChannel.read(byteBuffer)>1){
            //切换buffer为读模式
            byteBuffer.flip();
            //读取buffer中的内容
            response+= Charset.forName("UTF-8").decode(byteBuffer);
        }
        //将channel再次注册到selector上，监听他的可读事件
        socketChannel.register(selector,SelectionKey.OP_READ);
        //将信息打印
        if(response.length()>0){
            System.out.println(":: "+response);
        }
    }
}
