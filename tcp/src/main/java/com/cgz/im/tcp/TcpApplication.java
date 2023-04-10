package com.cgz.im.tcp;

import com.cgz.im.codec.config.BootstrapConfig;
import com.cgz.im.tcp.redis.RedisManager;
import com.cgz.im.tcp.server.LimServer;
import com.cgz.im.tcp.server.LimWebSocketServer;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;

public class TcpApplication {

    public static void main(String[] args) {
        if (args.length > 0) {
            start(args[0]);
        }
    }

    private static void start(String path) {

        try {
            Yaml yaml = new Yaml();
            FileInputStream inputStream = new FileInputStream(path);
            BootstrapConfig bootstrapConfig = yaml.loadAs(inputStream, BootstrapConfig.class);

            new LimServer(bootstrapConfig.getLim()).start();
            new LimWebSocketServer(bootstrapConfig.getLim()).start();

            RedisManager.init(bootstrapConfig);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(500);
        }

    }

}
