package com.cgz.im.tcp.server;

import com.cgz.im.codec.config.BootstrapConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LimServer {

    private final static Logger logger = LoggerFactory.getLogger(LimServer.class);

    BootstrapConfig.TcpConfig config;

    EventLoopGroup mainGroup;
    EventLoopGroup subGroup;
    ServerBootstrap server;

    public LimServer(BootstrapConfig.TcpConfig config){
        this.config = config;
        mainGroup = new NioEventLoopGroup(config.getBossThreadSize());
        subGroup = new NioEventLoopGroup(config.getWorkThreadSize());

        server = new ServerBootstrap();
        server.group(mainGroup,subGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,10240) //服务端可连接队列大小
                .option(ChannelOption.SO_REUSEADDR,true) //表示允许重复使用本地地址和端口
                .childOption(ChannelOption.TCP_NODELAY,true) //是否禁用Nagle算法,即是否批量发送数据,true禁用,false开启，开启可减少网络开销，但影响消息实时性
                .childOption(ChannelOption.SO_KEEPALIVE,true) //保活开关,2h没有数据服务端会发送心跳包
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {

                    }
                });
    }

    public void start(){
        this.server.bind(config.getTcpPort());
    }
}
