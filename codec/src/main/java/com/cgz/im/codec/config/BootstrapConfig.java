package com.cgz.im.codec.config;

import lombok.Data;

@Data
public class BootstrapConfig {

    private TcpConfig lim;

    @Data
    public static class TcpConfig{

        private Integer tcpPort;

        private Integer webSocketPort;

        private Integer bossThreadSize;

        private Integer workThreadSize;
    }
}
