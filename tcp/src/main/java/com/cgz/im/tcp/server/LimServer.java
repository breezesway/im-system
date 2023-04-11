package com.cgz.im.tcp.server;

import com.cgz.im.codec.MessageDecoder;
import com.cgz.im.codec.config.BootstrapConfig;
import com.cgz.im.tcp.handler.HeartBeatHandler;
import com.cgz.im.tcp.handler.NettyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
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

        //option()设置的是服务端用于接收进来的连接，也就是boosGroup线程。
        //
        //childOption()是提供给父管道接收到的连接，也就是workerGroup线程。
        server = new ServerBootstrap();
        server.group(mainGroup,subGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,10240) //服务端接收连接的队列长度
                .option(ChannelOption.SO_REUSEADDR,true) //表示允许重复使用本地地址和端口
                .childOption(ChannelOption.TCP_NODELAY,true) //是否禁用Nagle算法,即是否批量发送数据,true禁用,false开启，开启可减少网络开销，但影响消息实时性
                .childOption(ChannelOption.SO_KEEPALIVE,true) //保活开关,2h没有数据服务端会发送心跳包
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new MessageDecoder());
                        pipeline.addLast(new IdleStateHandler(0,0,1));//超时检测，会调用下一个handler的userenventtrigger
                        pipeline.addLast(new HeartBeatHandler(config.getHeartBeatTime()));
                        pipeline.addLast(new NettyServerHandler(config.getBrokerId()));
                    }
                });
    }

    public void start(){
        this.server.bind(config.getTcpPort());
    }
}
