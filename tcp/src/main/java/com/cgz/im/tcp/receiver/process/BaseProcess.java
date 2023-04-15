package com.cgz.im.tcp.receiver.process;

import com.cgz.im.codec.proto.MessagePack;
import com.cgz.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.socket.nio.NioSocketChannel;

public abstract class BaseProcess {

    public abstract void processBefore();

    public void process(MessagePack messagePack){
        processBefore();
        NioSocketChannel nioSocketChannel = SessionSocketHolder.get(messagePack.getAppId(),
                messagePack.getToId(),
                messagePack.getClientType(),
                messagePack.getImei());
        System.out.println("BaseProcess.process()方法内部："+nioSocketChannel);
        if(nioSocketChannel!=null){
            nioSocketChannel.writeAndFlush(messagePack);
        }
        processAfter();
    }

    public abstract void processAfter();

}
