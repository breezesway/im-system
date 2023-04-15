package com.cgz.im.tcp;

import com.cgz.im.codec.config.BootstrapConfig;
import com.cgz.im.tcp.receiver.MessageReceiver;
import com.cgz.im.tcp.redis.RedisManager;
import com.cgz.im.tcp.register.RegistryZk;
import com.cgz.im.tcp.register.ZKit;
import com.cgz.im.tcp.server.LimServer;
import com.cgz.im.tcp.server.LimWebSocketServer;
import com.cgz.im.tcp.utils.MQFactory;
import org.I0Itec.zkclient.ZkClient;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class TcpApplication {

    public static void main(String[] args) {
        if (args.length > 0) {
            start(args[0]);
        }
    }

    private static void start(String path) {
        try {
            //读取yml配置文件
            Yaml yaml = new Yaml();
            FileInputStream inputStream = new FileInputStream(path);
            BootstrapConfig bootstrapConfig = yaml.loadAs(inputStream, BootstrapConfig.class);

            //启动tcp服务和websocket服务
            new LimServer(bootstrapConfig.getLim()).start();
            new LimWebSocketServer(bootstrapConfig.getLim()).start();

            //redis、mq、zookeeper初始化
            RedisManager.init(bootstrapConfig);
            MQFactory.init(bootstrapConfig.getLim().getRabbitmq());
            MessageReceiver.init(bootstrapConfig.getLim().getBrokerId().toString());
            registerZk(bootstrapConfig);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(500);
        }
    }

    public static void registerZk(BootstrapConfig config) throws UnknownHostException {
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        ZkClient zkClient = new ZkClient(config.getLim().getZkConfig().getZkAddr(), config.getLim().getZkConfig().getZkConnectTimeOut());
        ZKit zKit = new ZKit(zkClient);
        RegistryZk registryZk = new RegistryZk(zKit, hostAddress, config.getLim());
        Thread thread = new Thread(registryZk);
        thread.start();
    }

}
