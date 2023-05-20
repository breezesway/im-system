package com.cgz.im.codec;

import com.cgz.im.codec.proto.Message;
import com.cgz.im.codec.utils.ByteBufToMessageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) {
        //请求头：
        //指令
        //clientType
        //消息解析类型
        //appId
        //imei长度
        //bodyLen
        //imei号
        //请求体
        if(byteBuf.readableBytes()<28){
            return;
        }
        Message message = ByteBufToMessageUtils.transition(byteBuf);
        if(message == null){
            return;
        }

        list.add(message);
    }
}
